package com.jinstudy.modules.main;

import com.jinstudy.infra.MockMvcTest;
import com.jinstudy.modules.account.AccountRepository;
import com.jinstudy.modules.account.AccountService;
import com.jinstudy.modules.account.form.SignUpForm;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@MockMvcTest
class MainControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired AccountService accountService;
    @Autowired AccountRepository accountRepository;

    @AfterEach
    void afterEach(){
        accountRepository.deleteAll();
    }

    @DisplayName("이메일로 로그인 성공")
    @Test
    void login_with_email() throws Exception { // 이메일로 로그인 하는 경우의 테스트

        SignUpForm signUpForm = new SignUpForm();
        signUpForm.setNickname("jjinse");
        signUpForm.setEmail("pengang1011@naver.com");
        signUpForm.setPassword("12345678");
        accountService.processNewAccount(signUpForm); // DB에 테스트 데이터 저장.

        mockMvc.perform(post("/login")
                .param("username","pengang1011@naver.com")
                .param("password","12345678")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/")) // 스프링 시큐리티가 알아서 루트로 리다이렉션한다.
                .andExpect(authenticated().withUsername("jjinse")); // 이메일인증을 했지만 인증을 nickname으로 하는이유!!
                //  스프링 시큐리티의 인자로 들어가는 username과 password가  어떻게 Principal과 연동되어 인증 절차가 이루어 지는지 알 필요가 있음!
        // 테스트 form 에서 username과 password에 해당하는 인자를 전달했을 때 확인
        // 코드 구현상 DB의 데이터와 비교가 필요하기 때문에 테스트에서도 DB에 데이터를 넣어주고 테스트해야한다.
        // processNewAccount 메서드를 통해서 DB에 새로운 데이터 저장.
    }


    // signUpForm 부분과 DB에 저장하는 부분은 중복이 되는데, 이부분은 BeforeEach, AfterEach로 리팩토링이 가능함.
    @DisplayName("닉네임으로 로그인 성공")
    @Test
    void login_with_nickname() throws Exception { // 이메일로 로그인 하는 경우의 테스트

        SignUpForm signUpForm = new SignUpForm();
        signUpForm.setNickname("jjinse");
        signUpForm.setEmail("pengang1011@naver.com");
        signUpForm.setPassword("12345678");
        accountService.processNewAccount(signUpForm); // DB에 테스트 데이터 저장.

        mockMvc.perform(post("/login")
                .param("username","pengang1011@naver.com")
                .param("password","12345678")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/")) // 스프링 시큐리티가 알아서 루트로 리다이렉션한다.
                .andExpect(authenticated().withUsername("jjinse")); // 이메일인증을 했지만 인증을 nickname으로 하는이유!!

    }

    @DisplayName("로그인 실패")
    @Test
    void login_fail() throws Exception {
        mockMvc.perform(post("/login")
        .param("username","failtestname")
        .param("password","11118888")
        .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/login?error"))
        .andExpect(unauthenticated());
    }

    @DisplayName("로그아웃")
    @Test
    void logout() throws Exception {
        mockMvc.perform(post("/logout")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/")) // 시큐리티 설정에서 루트로 리다이렉션하게 설정 했음.
                .andExpect(unauthenticated());
    }
}