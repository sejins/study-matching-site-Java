package com.sejin.config;

import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .mvcMatchers("/","/login","/sign-up","/check-email-token",
                        "/email-login","/check-email-login","/login-link").permitAll()
                .mvcMatchers(HttpMethod.GET, "/profile/*").permitAll()
                .anyRequest().authenticated();
        // 인증을 허가할 요청을 결정하고, 특정 요청 메서드에 대해서만 허가를 할 수도 있고,
        // 나머지 요청들을 모두 인증을 통해서 접근이 가능하다.

        http.formLogin().loginPage("/login").permitAll();
        // form을 통한 로그인을 활성화 하고, 커스텀 로그인 페이지를 띄워줄 핸들러의 url을 지정한다.

        http.logout().logoutSuccessUrl("/");

    }

    @Override
    public void configure(WebSecurity web) throws Exception {  // 정적 파일에 대해서는 인증 절차를 무시
        web.ignoring()
                .mvcMatchers("/node_modules/**")
                .requestMatchers(PathRequest.toStaticResources().atCommonLocations());
    }
}
