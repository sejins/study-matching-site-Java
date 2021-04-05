package com.sejin.settings;

import com.sejin.WithAccount;
import com.sejin.account.AccountRepository;
import com.sejin.domain.Account;
import org.hibernate.hql.internal.ast.tree.DotNode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class SettingsControllerTest {
    // 프로필 수정 테스트는 인증된 사용자에 대해서만 이루어져야하고 그렇기 때문에, 사용자를 생성하고 인증을 하는 절차가 필요하다!
    // 스프링 시큐리티에서 테스트 코드를 작성할 때 시큐리티 컨텍스트를 설정할 수 있는 기능을 제공한다.

    @Autowired MockMvc mockMvc;

    @Autowired AccountRepository accountRepository;

    @Autowired PasswordEncoder passwordEncoder;

    @AfterEach
    void afterEach(){
        accountRepository.deleteAll();
    }

    @WithAccount("jjinse")
    @DisplayName("프로필 폼 요청")
    @Test
    void updateProfileForm() throws Exception{
        mockMvc.perform(get("/settings/profile")) // get요청일지라도 인증된 사용자에 대해서 접근할 수 있기 때문에 인증된 사용자를 생성해줘야한다.
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("profile"));

        Account jjinse = accountRepository.findByNickname("jjinse");
        assertNull(jjinse.getBio());
        //Validation에서 걸리기 때문에 값이 들어가지 않게 된다.
    }

    @WithAccount("jjinse") // 사용할때마다 사용자에 대한 정보가 실제로 만들어지기 때문에 매 테스트마다 이를 지워줘야한다.
    @DisplayName("프로필 수정하기 - 입력값이 정상인 경우")
    @Test
    void updateProfile() throws Exception{
        String bio = "짧은 소개를 수정하는 경우";

        mockMvc.perform(post("/settings/profile")
            .param("bio",bio)
            .with(csrf()))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/settings/profile"))
            .andExpect(flash().attributeExists("message"));

        Account jjinse = accountRepository.findByNickname("jjinse");
        assertEquals(bio, jjinse.getBio());
    }

    @WithAccount("jjinse")
    @DisplayName("프로필 수정하기 - 입력값 에러")
    @Test
    void updateProfile_error() throws Exception{
        String bio = "긴 소개를 수정하는 경우, 긴 소개를 수정하는 경우, 긴 소개를 수정하는 경우, 긴 소개를 수정하는 경우"; // @Valid를 사용해서 35자 넘으면 검증에서 걸리게 해놨었음.

        mockMvc.perform(post("/settings/profile")
                .param("bio",bio)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("settings/profile"))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("profile"))
                .andExpect(model().hasErrors());

        Account jjinse = accountRepository.findByNickname("jjinse");
        assertNull(jjinse.getBio());
        //Validation에서 걸리기 때문에 값이 들어가지 않게 된다.
    }

    @WithAccount("jjinse")
    @DisplayName("패스워드 수정 폼")
    @Test
    void updatePassword_form() throws Exception{

        mockMvc.perform(get("/settings/password"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("passwordForm"));
    }

    @WithAccount("jjinse")
    @DisplayName("패스워드 수정 - 입력값 정상")
    @Test
    void updatePassword_success() throws Exception{

        mockMvc.perform(post("/settings/password")
                .param("newPassword","12345678")
                .param("newPasswordConfirm","12345678")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/settings/password"))
                .andExpect(flash().attributeExists("message"));

        //실제로 바뀌었는지 확인도 해주기
        Account jjinse = accountRepository.findByNickname("jjinse");
        assertTrue(passwordEncoder.matches("12345678",jjinse.getPassword()));
        // assertEquals(passwordEncoder.encode("12345678"),jjinse.getPassword());   // 이거 왜 안되지..?!
    }

    @WithAccount("jjinse")
    @DisplayName("패스워드 수정 - 입력값 오류")
    @Test
    void updatePassword_error() throws Exception{

        mockMvc.perform(post("/settings/password")
                .param("newPassword","12345678")
                .param("newPasswordConfirm","11111111")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("settings/password"))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("passwordForm"));
    }

    @WithAccount("jjinse")
    @DisplayName("알림 설정 폼")
    @Test
    void updateNotifications_form() throws Exception{

        mockMvc.perform(get("/settings/notifications"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("notifications"));
    }

    @WithAccount("jjinse")
    @DisplayName("알림 설정")
    @Test
    void updateNotifications_success() throws Exception{

        mockMvc.perform(post("/settings/notifications")
                .param("studyCreatedByEmail","true")
                .param("studyCreatedByWeb","true")
                .param("studyEnrollmentByEmail","true")
                .param("studyEnrollmentByWeb","true")
                .param("studyUpdatedByEmail","true")
                .param("studyUpdatedByWeb","true")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/settings/notifications"))
                .andExpect(flash().attributeExists("message"));

        // 제대로 값들이 저장이 됐는지 검사
        Account jjinse = accountRepository.findByNickname("jjinse");
        assertTrue(jjinse.isStudyCreatedByEmail());
        assertTrue(jjinse.isStudyCreatedByWeb());
        assertTrue(jjinse.isStudyEnrollmentByEmail());
        assertTrue(jjinse.isStudyCreatedByWeb());
        assertTrue(jjinse.isStudyUpdatedByEmail());
        assertTrue(jjinse.isStudyCreatedByWeb());
    }

    @WithAccount("jjinse")
    @DisplayName("알림 설정 - 오류값 전송")
    @Test
    void updateNotifications_error() throws Exception{

        mockMvc.perform(post("/settings/notifications")
                .param("studyCreatedByEmail","hellohihihi")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("settings/notifications"))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("notifications"));
    }
}