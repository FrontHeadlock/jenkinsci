package jenkins;

import static java.util.logging.Level.SEVERE;

import hudson.init.InitMilestone;
import hudson.init.InitReactorListener;
import hudson.security.ACL;
import hudson.util.DaemonThreadFactory;
import hudson.util.NamingThreadFactory;
import java.io.IOException;
import java.util.List;
import java.util.ServiceLoader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import jenkins.model.Jenkins;
import jenkins.security.ImpersonatingExecutorService;
import jenkins.util.SystemProperties;
import org.jvnet.hudson.reactor.Milestone;
import org.jvnet.hudson.reactor.Reactor;
import org.jvnet.hudson.reactor.ReactorException;
import org.jvnet.hudson.reactor.ReactorListener;
import org.jvnet.hudson.reactor.Task;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;

/**
 * Executes the {@link Reactor} for the purpose of bootup.
 *
 * <p>실행 흐름 관점에서 보면 이 클래스는 InitializerFinder와 Jenkins.loadTasks()가 구성한
 * 초기화 태스크 그래프를 실제로 실행하는 런타임 실행기 역할이다.
 * InitMilestone은 단계 경계표 역할이고,
 * 이 클래스는 그 milestone 순서 제약을 Reactor에 주입한 뒤,
 * 스레드풀, 권한 컨텍스트, 리스너, 로그 출력을 결합하여 실제 부팅 태스크 실행을 개시하는 역할이다.
 *
 * <p>즉 Initializer/InitializerFinder가 "무엇을 실행할지"를 정의하는 쪽이라면,
 * 이 클래스는 "그 그래프를 어떤 실행 환경과 어떤 관측 방식으로 돌릴지"를 담당하는 실행기라는 의미이다.
 *
 * @author Kohsuke Kawaguchi
 */
public class InitReactorRunner {
    public void run(Reactor reactor) throws InterruptedException, ReactorException, IOException {
        /*
         * 이미 구성된 Reactor에 InitMilestone 순서 제약 태스크를 주입하고 실제 실행을 시작하는 최상위 메서드
         * milestone enum 값만으로는 선후 관계가 생기지 않으므로 ordering()이 만든 더미 태스크를 먼저 합성하는 단계
         * 이후 병렬 또는 단일 스레드 실행기, SYSTEM 권한 위임 실행기, ReactorListener 집합을 결합하여 초기화 태스크 전체를 실행하는 구조
         */
        reactor.addAll(InitMilestone.ordering().discoverTasks(reactor));

        ExecutorService es;
        if (Jenkins.PARALLEL_LOAD)
            // 플러그인 로드와 item 로드를 병렬화할 수 있을 때 사용하는 병렬 초기화 실행기 구성 단계
            es = new ThreadPoolExecutor(
                TWICE_CPU_NUM, TWICE_CPU_NUM, 5L, TimeUnit.SECONDS, new LinkedBlockingQueue<>(), new DaemonThreadFactory());
        else
            // 디버깅 또는 직렬 초기화 상황을 위한 단일 스레드 실행기 구성 단계
            es = Executors.newSingleThreadExecutor(new NamingThreadFactory(new DaemonThreadFactory(), "InitReactorRunner"));
        try {
            // 초기화 태스크를 SYSTEM 권한으로 실행하고, milestone 및 태스크 이벤트를 리스너로 관측하는 실제 개시 단계
            reactor.execute(new ImpersonatingExecutorService(es, ACL.SYSTEM2), buildReactorListener());
        } finally {
            // 성공 시 잔여 큐가 비어 있어야 하고 실패 시 보류 태스크를 즉시 정리해야 하므로 강제 종료하는 후처리 단계
            es.shutdownNow();   // upon a successful return the executor queue should be empty. Upon an exception, we want to cancel all pending tasks
        }

    }

    /**
     * Aggregates all the listeners into one and returns it.
     *
     * <p>
     * At this point plugins are not loaded yet, so we fall back to the META-INF/services look up to discover implementations.
     * As such there's no way for plugins to participate into this process.
     */
    private ReactorListener buildReactorListener() throws IOException {
        /*
         * 초기화 태스크와 milestone 진척을 외부로 관측 가능하게 만드는 리스너 집합 구성 메서드
         * 아직 플러그인 확장이 준비되지 않은 시점이므로 ExtensionList가 아니라 ServiceLoader 기반으로 InitReactorListener를 수집하는 구조
         * 여기에 기본 로깅 리스너를 추가하여 task start/completed/failed와 milestone attained를 로그로 남기는 역할
         */
        List<ReactorListener> r = StreamSupport.stream(ServiceLoader.load(InitReactorListener.class, Thread.currentThread().getContextClassLoader()).spliterator(), false).collect(Collectors.toList());
        r.add(new ReactorListener() {
            final Level level = Level.parse(SystemProperties.getString(Jenkins.class.getName() + "." + "initLogLevel", "FINE"));
            @Override
            public void onTaskStarted(Task t) {
                LOGGER.log(level, "Started {0}", getDisplayName(t));
            }

            @Override
            public void onTaskCompleted(Task t) {
                LOGGER.log(level, "Completed {0}", getDisplayName(t));
            }

            @Override
            public void onTaskFailed(Task t, Throwable err, boolean fatal) {
                LOGGER.log(SEVERE, "Failed " + getDisplayName(t), err);
            }

            @Override
            public void onAttained(Milestone milestone) {
                // 일반 milestone과 InitMilestone을 구분하여 로그 레벨과 후속 훅 호출을 다르게 처리하는 milestone 도달 통지 단계
                Level lv = level;
                String s = "Attained " + milestone.toString();
                if (milestone instanceof InitMilestone) {
                    lv = Level.INFO; // noteworthy milestones --- at least while we debug problems further
                    // Jenkins 생성자 쪽 override가 이 훅을 통해 initLevel 갱신과 타임아웃 연장을 수행하는 연결 지점
                    onInitMilestoneAttained((InitMilestone) milestone);
                    s = milestone.toString();
                }
                LOGGER.log(lv, s);
            }
        });
        return new ReactorListener.Aggregator(r);
    }

    /** Like {@link Task#getDisplayName} but more robust. */
    @Restricted(NoExternalUse.class)
    public static String getDisplayName(Task t) {
        try {
            return t.getDisplayName();
        } catch (RuntimeException | Error x) {
            LOGGER.log(Level.WARNING, "failed to find displayName of " + t, x);
            return t.toString();
        }
    }

    /**
     * Called when the init milestone is attained.
     */
    protected void onInitMilestoneAttained(InitMilestone milestone) {
        // Jenkins 쪽에서 override하여 현재 initLevel 반영과 라이프사이클 후속 처리로 연결하는 확장 훅 지점
    }

    private static final int TWICE_CPU_NUM = SystemProperties.getInteger(
            InitReactorRunner.class.getName() + ".concurrency",
            Runtime.getRuntime().availableProcessors() * 2);

    private static final Logger LOGGER = Logger.getLogger(InitReactorRunner.class.getName());
}
