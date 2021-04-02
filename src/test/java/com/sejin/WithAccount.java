package com.sejin;

import org.springframework.security.test.context.support.WithSecurityContext;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithAccountSecurityContextFactory.class) // 구현한 WithAccountSecurityContextFactory 클래스에서 시큐리티 컨텍스트를 만들고 설정하는 기능을 만듦.
public @interface WithAccount {
    String value();
}
// WithAccountSecurityContextFactory 클래스 내부 기능보면 여기 애노테이션을 통해서 받은 nickname으로 유저를 생성하고 시큐리티 컨텍스트를 통해서 인증 정보를 만들게 된다.