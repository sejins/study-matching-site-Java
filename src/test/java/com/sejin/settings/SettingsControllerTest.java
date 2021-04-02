package com.sejin.settings;

import com.sejin.WithAccount;
import com.sejin.account.AccountRepository;
import com.sejin.domain.Account;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
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
}