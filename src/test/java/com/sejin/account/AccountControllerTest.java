package com.sejin.account;

import com.sejin.domain.Account;
import com.sejin.mail.EmailMessage;
import com.sejin.mail.EmailService;
import org.jboss.jandex.JandexAntTask;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc; 
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AccountRepository accountRepository;

    @MockBean
    EmailService emailService; // mocking

    @DisplayName("인증 메일 확인 - 입력값 오류")
    @Test
    void checkEmailToken_with_wrong_input() throws Exception {
        mockMvc.perform(get("/check-email-token")
                .param("token","asdkjflaksdjlkj")
                .param("email","email@email.com")) // 여기에서는 csrf 토큰을 넣어주지 않아도 된다.  이유는 한번 생각해봤는데, 이 주석을 다시 볼 때 생각이 날까 의문이다.
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("error"))
                .andExpect(view().name("account/checked-email"))
                .andExpect(unauthenticated());
    }

    @DisplayName("인증 메일 확인 - 입력값 정상")
    @Test
    void checkEmailToken() throws Exception {

        Account account = Account.builder()
                .email("test123@email.com")
                .nickname("testBoy")
                .password("123456789").build();

        account.generateEmailCheckToken();
        Account newAccount = accountRepository.save(account);


        mockMvc.perform(get("/check-email-token")
                .param("token",newAccount.getEmailCheckToken())
                .param("email",newAccount.getEmail())) // 여기에서는 csrf 토큰을 넣어주지 않아도 된다.  이유는 한번 생각해봤는데, 이 주석을 다시 볼 때 생각이 날까 의문이다.
                .andExpect(status().isOk())
                .andExpect(model().attributeDoesNotExist("error"))
                .andExpect(model().attributeExists("numberOfUser"))
                .andExpect(model().attributeExists("nickname"))
                .andExpect(view().name("account/checked-email"))
                .andExpect(authenticated());
    }

    @DisplayName("회원 가입 화면 보이는지 테스트")
    @Test
    void signUpForm() throws Exception{
        mockMvc.perform(get("/sign-up"))
                .andExpect(status().isOk())
                .andExpect(view().name("account/sign-up"))
                .andExpect(model().attributeExists("signUpForm")) // model의 해당 이름의("signUpForm") 애트리뷰트가 존재하는지만 확인하면 된다. -> 없으면 뷰 페이지에 에러가 나니까
                .andExpect(unauthenticated());
    }

    @DisplayName("회원 가입 처리 - 입력값 오류")
    @Test
    void signUpSubmit_with_wrong_input() throws Exception {
        mockMvc.perform(post("/sign-up")
            .param("nickname","jjinse")
            .param("eamil","email...")
            .param("password","12345")
            .with(csrf()))   // csrf 토큰을 넣어서 테스트 폼을 만들어야 스프링 시큐리티에 의해서 403 에러가 나지 않는다.
            .andExpect(status().isOk())
            .andExpect(view().name("account/sign-up"))
            .andExpect(unauthenticated());
    }

    @DisplayName("회원 가입 처리 - 입력값 정상")
    @Test
    void signUpSubmit_with_correct_input() throws Exception {
        mockMvc.perform(post("/sign-up")
                .param("nickname","jjinse")
                .param("email","pengang1011@naver.com")
                .param("password","12345789524")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/"))
                .andExpect(authenticated());

        Account account = accountRepository.findByEmail("pengang1011@naver.com"); // 이메일로 찾기... 뭐냐 findByEmail도 알아서 만들어주냐..?
        assertNotNull(account); // 비어있는 객체가 아닌지 테스트
        assertNotEquals(account.getPassword(), "12345789524"); // 입력한 패스워드와 인코딩한 패스워드가 같지 않은지 테스트
        assertNotNull(account.getEmailCheckToken());


        then(emailService).should().sendEmail(any(EmailMessage.class)); // 메일을 보내는지!  -> 메일 보내는 코드 없으면 테스트 깨진다.  컨트롤러에 javaMailSender.send 메서드 부분.
    }
}