/*
 * The MIT License
 *
 * Copyright (c) 2004-2009, Sun Microsystems, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package hudson.init;

import org.jvnet.hudson.reactor.Executable;
import org.jvnet.hudson.reactor.Milestone;
import org.jvnet.hudson.reactor.TaskBuilder;
import org.jvnet.hudson.reactor.TaskGraphBuilder;

/**
 * Various key milestone in the initialization process of Hudson.
 *
 * <p>
 * Plugins can use these milestones to execute their initialization at the right moment
 * (in addition to defining their own milestones by implementing {@link Milestone}.
 *
 * <p>
 * These milestones are achieve in this order:
 * <ol>
 *  <li>STARTED
 *  <li>PLUGINS_LISTED
 *  <li>PLUGINS_PREPARED
 *  <li>PLUGINS_STARTED
 *  <li>EXTENSIONS_AUGMENTED
 *  <li>SYSTEM_CONFIG_LOADED</li>
 *  <li>SYSTEM_CONFIG_ADAPTED</li>
 *  <li>JOB_LOADED
 *  <li>JOB_CONFIG_ADAPTED</li>
 *  <li>COMPLETED
 * </ol>
 *
 * <p>실행 흐름 관점에서 보면 이 enum은 Jenkins Init Reactor 전체의 단계 경계표 역할이다.
 * WebAppMain이 Jenkins 생성자를 호출하고, Jenkins가 executeReactor(...)를 실행할 때,
 * 개별 초기화 태스크와 {@link Initializer} 메서드들은 이 milestone 전후 제약을 기준으로
 * 선후 관계를 형성한다.
 *
 * <p>즉 이 값들은 단순 상태 표시값이 아니라,
 * 플러그인 준비, 확장 로딩, 시스템 설정 로드, job 로드, 최종 개시까지의 순서를
 * Reactor 그래프 안에 강제하는 초기화 순서 축 역할이다.
 *
 * @author Kohsuke Kawaguchi
 */
public enum InitMilestone implements Milestone {
    /**
     * The very first milestone that gets achieved without doing anything.
     *
     * This is used in {@link Initializer#after()} since annotations cannot have null as the default value.
     */
    // 실제 작업 수행 전 Reactor 그래프의 시작점을 표현하는 기준 milestone
    STARTED("Started initialization"),

    /**
     * By this milestone, all plugins metadata are inspected and their dependencies figured out.
     */
    PLUGINS_LISTED("Listed all plugins"),

    /**
     * By this milestone, all plugin metadata are loaded and its classloader set up.
     */
    // 플러그인 클래스 로더 준비 완료 경계 milestone
    // 이후 단계부터는 다른 플러그인 클래스까지 포함한 확장 검색이 가능한 전제 구간
    PLUGINS_PREPARED("Prepared all plugins"),

    /**
     * By this milestone, all plugins start executing, all extension points loaded, descriptors instantiated
     * and loaded.
     *
     * <p>
     * This is a separate milestone from {@link #PLUGINS_PREPARED} since the execution
     * of a plugin often involves finding extension point implementations, which in turn
     * require all the classes from all the plugins to be loadable.
     */
    PLUGINS_STARTED("Started all plugins"),

    /**
     * By this milestone, all programmatically constructed extension point implementations
     * should be added.
     */
    // 정적 발견 외에 코드로 보강된 extension까지 반영되어 이후 설정 로드가 확장 관점에서 안정화되는 경계 milestone
    EXTENSIONS_AUGMENTED("Augmented all extensions"),

    /**
     * By this milestone, all the system configurations are loaded from file system
     * @since 2.220
     */
    // Jenkins 전역 config.xml과 노드 관련 설정이 메모리로 복원된 이후 경계 milestone
    SYSTEM_CONFIG_LOADED("System config loaded"),

    /**
     * By this milestone, the system configuration is adapted just in case any plugin (CasC might be an example) needs
     * to update configuration files
     * @since 2.220
     */
    SYSTEM_CONFIG_ADAPTED("System config adapted"),

    /**
     * By this milestone, all jobs and their build records are loaded from disk.
     */
    // jobs 디렉터리와 item 설정이 메모리 객체로 복원되어 개별 job 존재가 확정된 이후 경계 milestone
    JOB_LOADED("Loaded all jobs"),

    /**
     * By this milestone, any job configuration is adapted or updated just in case any plugin needs to update former/old configurations.
     * It does not include {@link hudson.init.impl.GroovyInitScript}s which get executed later
     * @since 2.220
     */
    JOB_CONFIG_ADAPTED("Configuration for all jobs updated"),

    /**
     * The very last milestone.
     * All executions should be completed by it, including {@link hudson.init.impl.GroovyInitScript}s.
     * This is used in {@link Initializer#before()} since annotations cannot have null as the default value.
     */
    // 모든 초기화 태스크와 보정 스크립트가 끝나고 Jenkins가 정상 운영 상태로 넘어갈 수 있음을 뜻하는 최종 milestone
    COMPLETED("Completed initialization");

    private final String message;

    InitMilestone(String message) {
        this.message = message;
    }

    /**
     * Creates a set of dummy tasks to enforce ordering among {@link InitMilestone}s.
     */
    public static TaskBuilder ordering() {
        /*
         * milestone 자체는 값 정의만으로는 Reactor 그래프에 선후 관계를 만들지 못하므로,
         * 각 milestone 사이를 잇는 NOOP 태스크를 삽입하여 STARTED -> ... -> COMPLETED 순서를 강제하는 메서드
         * Jenkins.executeReactor(...)가 이 TaskBuilder를 함께 넘김으로써 개별 초기화 태스크들이 milestone 제약을 신뢰할 수 있는 구조
         */
        TaskGraphBuilder b = new TaskGraphBuilder();
        InitMilestone[] v = values();
        for (int i = 0; i < v.length - 1; i++)
            // 앞 milestone 달성 이후 다음 milestone 달성이 가능하도록 더미 간선 역할을 추가하는 연결 단계
            b.add(null, Executable.NOOP).requires(v[i]).attains(v[i + 1]);
        return b;
    }


    @Override
    public String toString() {
        return message;
    }
}
