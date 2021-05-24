package com.jinstudy.modules.study;

import com.jinstudy.infra.MockMvcTest;
import com.jinstudy.modules.account.AccountFactory;
import com.jinstudy.modules.account.AccountRepository;
import com.jinstudy.modules.account.WithAccount;
import com.jinstudy.modules.account.Account;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@MockMvcTest
class StudySettingsControllerTest{

    @Autowired private MockMvc mockMvc;
    @Autowired private AccountRepository accountRepository;
    @Autowired private StudyRepository studyRepository;
    @Autowired private StudyService studyService;
    @Autowired private StudyFactory studyFactory;
    @Autowired private AccountFactory accountFactory;

    @WithAccount("jjinse")
    @DisplayName("스터디 수정 폼 조회 실패 - 권한 없는 유저의 접근")
    @Test
    void updateDescriptionForm_fail() throws Exception {
        Account sejin = accountFactory.createAccount("sejin");
        Study study = studyFactory.createStudy("test-study",sejin);

        mockMvc.perform(get("/study/"+study.getPath()+"/settings/description"))
                .andExpect(status().isForbidden());
    }

    @WithAccount("jjinse")
    @DisplayName("스터디 수정 폼 조회 성공")
    @Test
    void updateDescriptionForm_success() throws Exception {
        Account jjinse = accountRepository.findByNickname("jjinse");
        Study study = studyFactory.createStudy("test-study",jjinse);

        mockMvc.perform(get("/study/"+study.getPath()+"/settings/description"))
                .andExpect(view().name("study/settings/description"))
                .andExpect(model().attributeExists("studyDescriptionForm"))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("study"));
    }

    @WithAccount("jjinse")
    @DisplayName("스터디 소개 수정 - 성공")
    @Test
    void updateDescription_success() throws Exception {
        Account jjinse = accountRepository.findByNickname("jjinse");
        Study study = studyFactory.createStudy("test-study",jjinse);

        String settingsDescriptionUrl = "/study/"+study.getPath()+"/settings/description";

        mockMvc.perform(post(settingsDescriptionUrl)
                .param("shortDescription","short description")
                .param("fullDescription","full description")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(settingsDescriptionUrl))
                .andExpect(flash().attributeExists("message"));

        assertEquals("short description", study.getShortDescription());
        assertEquals("full description", study.getFullDescription());
    }

    @WithAccount("jjinse")
    @DisplayName("스터디 소개 수정 - 실패")
    @Test
    void updateDescription_fail() throws Exception {
        Account jjinse = accountRepository.findByNickname("jjinse");
        Study study = studyFactory.createStudy("test-study",jjinse);
        String settingsDescriptionUrl = "/study/"+study.getPath()+"/settings/description";

        mockMvc.perform(post(settingsDescriptionUrl)
                .param("shortDescription","")
                .param("fullDescription","full description")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().hasErrors())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("studyDescriptionForm"))
                .andExpect(model().attributeExists("study"));
    }
}