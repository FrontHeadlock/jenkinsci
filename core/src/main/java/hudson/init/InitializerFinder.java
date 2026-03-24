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

import org.jvnet.hudson.reactor.Milestone;

/**
 * Discovers initialization tasks from {@link Initializer}.
 *
 * <p>실행 흐름 관점에서 보면 이 클래스는 {@link Initializer} 애노테이션을 실제 Reactor 태스크 정의로
 * 번역하는 어댑터 역할이다. InitMilestone 자체는 단계 경계표이고,
 * {@link Initializer}는 선언 메타데이터이며,
 * 이 클래스가 TaskMethodFinder의 훅 메서드들을 통해 애노테이션 값을
 * requires/attains/after/before/fatal 정보로 꺼내어 초기화 태스크 그래프에 연결한다.
 *
 * <p>즉 "애노테이션 선언"과 "Reactor 실행 태스크" 사이를 이어 주는 변환 지점이라는 의미이다.
 *
 * @author Kohsuke Kawaguchi
 */
public class InitializerFinder extends TaskMethodFinder<Initializer> {

    public InitializerFinder(ClassLoader cl) {
        // Initializer 애노테이션과 InitMilestone enum을 TaskMethodFinder에 결합하는 타입 바인딩 단계
        super(Initializer.class, InitMilestone.class, cl);
    }

    public InitializerFinder() {
        this(Thread.currentThread().getContextClassLoader());
    }

    @Override
    protected String displayNameOf(Initializer i) {
        // 진행 표시용 태스크 이름이 애노테이션에서 어떻게 추출되는지 연결하는 매핑 단계
        return i.displayName();
    }

    @Override
    protected String[] requiresOf(Initializer i) {
        // 문자열 기반 선행 milestone 선언을 TaskMethodFinder로 전달하는 매핑 단계
        return i.requires();
    }

    @Override
    protected String[] attainsOf(Initializer i) {
        // 문자열 기반 기여 milestone 선언을 TaskMethodFinder로 전달하는 매핑 단계
        return i.attains();
    }

    @Override
    protected Milestone afterOf(Initializer i) {
        // 대표 선행 milestone 선언을 Reactor requires 관계로 연결하는 매핑 단계
        return i.after();
    }

    @Override
    protected Milestone beforeOf(Initializer i) {
        // 대표 후행 milestone 선언을 Reactor attains 관계로 연결하는 매핑 단계
        return i.before();
    }

    @Override
    protected boolean fatalOf(Initializer i) {
        // 초기화 태스크 실패가 부팅 실패로 승격되는지 여부를 전달하는 치명도 매핑 단계
        return i.fatal();
    }
}
