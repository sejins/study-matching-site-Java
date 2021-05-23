package com.jinstudy.study;

import com.jinstudy.WithAccount;
import com.jinstudy.account.AccountRepository;
import com.jinstudy.domain.Account;
import com.jinstudy.domain.Study;
import org.junit.jupiter.api.AfterEach;
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

@Transactional
@AutoConfigureMockMvc
@SpringBootTest
public class StudyControllerTest {

    @Autowired protected MockMvc mockMvc;
    @Autowired protected AccountRepository accountRepository;
    @Autowired protected StudyRepository studyRepository;
    @Autowired protected StudyService studyService;

//    @AfterEach
//    void afterEach(){
//        accountRepository.deleteAll();
//    }

    @WithAccount("jjinse")
    @DisplayName("스터디 개설 폼 조회")
    @Test
    void createStudyForm() throws Exception {
        mockMvc.perform(get("/new-study"))
                .andExpect(status().isOk())
                .andExpect(view().name("study/form"))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("studyForm"));
    }

    @WithAccount("jjinse")
    @DisplayName("스터디 생성 - 성공")
    @Test
    void createStudy_success() throws Exception {
        mockMvc.perform(post("/new-study")
                .with(csrf())
                .param("title","test title")
                .param("path","test-path")
                .param("shortDescription","test short description")
                .param("fullDescription","test full description"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/study/test-path"));

        Study newStudy = studyRepository.findByPath("test-path");
        assertNotNull(newStudy);
        Account jjinse = accountRepository.findByNickname("jjinse");
        assertTrue(newStudy.getManagers().contains(jjinse));
    }

    @WithAccount("jjinse")
    @DisplayName("스터디 개설 - 실패")
    @Test
    void createStudy_fail() throws Exception {
        mockMvc.perform(post("/new-study")
                .param("path", "wrong path")
                .param("title", "study title")
                .param("shortDescription", "short description of a study")
                .param("fullDescription", "full description of a study")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("study/form"))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeExists("studyForm"))
                .andExpect(model().attributeExists("account"));

        Study newStudy = studyRepository.findByPath("wrong path");
        assertNull(newStudy);
    }

    @WithAccount("jjinse")
    @DisplayName("스터디 조회")
    @Test
    void viewStudy() throws Exception{

//        Study study = Study.builder()
//                .path("test-path")
//                .title("test study")
//                .shortDescription("test short description")
//                .fullDescription("<p>test full description</p>")
//                .build();

        Study study = new Study();
        study.setPath("test-path");
        study.setTitle("test study");
        study.setShortDescription("short description");
        study.setFullDescription("<p>full description</p>");
        // 빌더 쓰니까 managers에서 널이 뜬다. 또 JPA에서 내가 모르는 무언가가 있나보다.. 빨리 공부해보고싶다. 일단 이렇다는걸 알고만 넘어가자.

        Account jjinse = accountRepository.findByNickname("jjinse");
        studyService.createNewStudy(jjinse, study);

        mockMvc.perform(get("/study/test-path"))
                .andExpect(status().isOk())
                .andExpect(view().name("study/view"))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("study"));
    }

    @WithAccount("jjinse")
    @DisplayName("스터디 가입")
    @Test
    void joinStudy() throws Exception {
        Account sejin = createAccount("sejin");

        Study study = createStudy("test-study",sejin);

        mockMvc.perform(get("/study/"+study.getPath()+"/join"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/study/"+study.getPath()+"/members"));

        Account jjinse = accountRepository.findByNickname("jjinse");
        assertTrue(study.getMembers().contains(jjinse));
    }

    @WithAccount("jjinse")
    @DisplayName("스터디 탈퇴")
    @Test
    void leaveStudy() throws Exception {
        Account sejin = createAccount("sejin");
        Study study = createStudy("test-study",sejin);

        Account jjinse = accountRepository.findByNickname("jjinse");
        study.addMember(jjinse);

        mockMvc.perform(get("/study/"+study.getPath()+"/leave"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/study/"+study.getPath()+"/members"));

        assertFalse(study.getMembers().contains("jjinse"));
    }

     protected Study createStudy(String path, Account account) {
        Study study = new Study();
        study.setPath(path);
        return studyService.createNewStudy(account,study);
    }

    protected Account createAccount(String name) {
        Account sejin = new Account();
        sejin.setNickname(name);
        sejin.setEmail(name + "@email.com");
        return accountRepository.save(sejin);
    }
}