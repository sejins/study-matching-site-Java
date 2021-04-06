package com.sejin.config;

import com.sejin.account.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;

import javax.sql.DataSource;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final AccountService accountService;
    private final DataSource dataSource;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .mvcMatchers("/","/login","/sign-up","/check-email-token",
                        "/email-login","/check-email-login","/login-link","/login-by-email").permitAll()
                .mvcMatchers(HttpMethod.GET, "/profile/*").permitAll()
                .anyRequest().authenticated();
        // 인증을 허가할 요청을 결정하고, 특정 요청 메서드에 대해서만 허가를 할 수도 있고,
        // 나머지 요청들을 모두 인증을 통해서 접근이 가능하다.

        http.formLogin().loginPage("/login").permitAll();
        // form을 통한 로그인을 활성화 하고, 커스텀 로그인 페이지를 띄워줄 핸들러의 url을 지정한다.

        http.logout().logoutSuccessUrl("/");

        http.rememberMe()
                .userDetailsService(accountService)
                .tokenRepository(tokenRepository());

    }

    @Bean
    public PersistentTokenRepository tokenRepository(){ // username, rnadom token, series 를 조합한 토큰 값을 DB에 저장하기 위해서
        JdbcTokenRepositoryImpl jdbcTokenRepository = new JdbcTokenRepositoryImpl();
        jdbcTokenRepository.setDataSource(dataSource);
        return jdbcTokenRepository;
        // jpa를 사용하기 때문에 jdbc의 table 스키마에 해당하는 엔티티가 존재해야한다.
        // JdbcTokenRepositoryImpl 클래스를 보면 테이블을 생성하는 메서드에서 테이블의 구성을 알 수 있다.
        // 그것을 바탕으로 엔티티 생성.
    }

    @Override
    public void configure(WebSecurity web) throws Exception {  // 정적 파일에 대해서는 인증 절차를 무시
        web.ignoring()
                .mvcMatchers("/node_modules/**")
                .requestMatchers(PathRequest.toStaticResources().atCommonLocations());
    }
}
