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

import static hudson.init.InitMilestone.COMPLETED;
import static hudson.init.InitMilestone.STARTED;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import hudson.Extension;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import org.jvnet.hudson.annotation_indexer.Indexed;
import org.jvnet.hudson.reactor.Task;

/**
 * Placed on methods to indicate that this method is to be run during the Jenkins start up to perform
 * some sort of initialization tasks, for example:
 *
 * <pre>
   &#64;Initializer(after=JOB_LOADED)
   public static void init() throws IOException {
       ....
   }
 * </pre>
 *
 * <p>
 * The method in question can be either {@code static} or an instance method. When used with instance
 * methods, those methods have to be on a class annotated with {@link Extension} and marked as
 * {@link #after()} {@link InitMilestone#PLUGINS_PREPARED}.
 *
 * <p>실행 흐름 관점에서 보면 이 애노테이션은 "이 메서드를 Jenkins 부팅 Reactor 안의 태스크로 취급하라"는
 * 선언 계약 역할이다. InitMilestone은 초기화 단계 경계표 역할이고,
 * 이 애노테이션은 특정 메서드가 어떤 milestone 이후에 실행 가능하며 어떤 milestone 달성에 기여하는지를
 * 메타데이터 형태로 기술하는 역할이다.
 *
 * <p>즉 실제 실행 순서를 직접 구현 코드로 적는 구조가 아니라,
 * 메서드에 부착된 선언 정보인 after/before/requires/attains/fatal을
 * InitializerFinder가 읽어 Reactor Task로 변환하는 연결 구조이다.
 *
 * @author Kohsuke Kawaguchi
 */
@Indexed
@Documented
@Retention(RUNTIME)
@Target(METHOD)
public @interface Initializer {
    /**
     * Indicates that the specified milestone is necessary before executing this initializer.
     *
     * <p>
     * This has the identical purpose as {@link #requires()}, but it's separated to allow better type-safety
     * when using {@link InitMilestone} as a requirement (since enum member definitions need to be constant).
     */
    // 이 초기화 메서드가 실행되기 전에 반드시 도달해야 하는 대표 milestone 선언 슬롯
    InitMilestone after() default STARTED;

    /**
     * Indicates that this initializer is a necessary step before achieving the specified milestone.
     *
     * <p>
     * This has the identical purpose as {@link #attains()}. See {@link #after()} for why there are two things
     * to achieve the same goal.
     */
    // 이 초기화 메서드 완료가 특정 milestone 달성의 일부 조건임을 나타내는 대표 milestone 선언 슬롯
    InitMilestone before() default COMPLETED;

    /**
     * Indicates the milestones necessary before executing this initializer.
     */
    // 문자열 기반 추가 선행 milestone 선언 슬롯
    // enum에 없는 사용자 정의 milestone 이름까지 표현할 수 있도록 열어 둔 확장 지점
    String[] requires() default {};

    /**
     * Indicates the milestones that this initializer contributes to.
     *
     * A milestone is considered attained if all the initializers that attains the given milestone
     * completes. So it works as a kind of join.
     */
    // 문자열 기반 추가 기여 milestone 선언 슬롯
    // 여러 초기화 메서드가 같은 milestone 달성에 합류하는 조인 지점 표현 역할
    String[] attains() default {};

    /**
     * Key in {@code Messages.properties} that represents what this task is about. Used for rendering the progress.
     * Defaults to "${short class name}.${method Name}".
     */
    // 초기화 진행 화면과 로그에 표시될 태스크 이름 커스터마이징 슬롯
    String displayName() default "";

    /**
     * Should the failure in this task prevent Hudson from starting up?
     *
     * @see Task#failureIsFatal()
     */
    // 이 초기화 태스크 실패를 부팅 중단 사유로 승격할지 여부를 정하는 치명도 선언 슬롯
    boolean fatal() default true;
}
