package com.jinstudy.modules.account;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.jinstudy.infra.MockMvcTest;
import com.jinstudy.modules.tag.Tag;
import com.jinstudy.modules.zone.Zone;
import com.jinstudy.modules.tag.TagForm;
import com.jinstudy.modules.zone.ZoneForm;
import com.jinstudy.modules.tag.TagRepository;
import com.jinstudy.modules.zone.ZoneRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static com.jinstudy.modules.account.SettingsController.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@MockMvcTest
class SettingsControllerTest {
    // 프로필 수정 테스트는 인증된 사용자에 대해서만 이루어져야하고 그렇기 때문에, 사용자를 생성하고 인증을 하는 절차가 필요하다!
    // 스프링 시큐리티에서 테스트 코드를 작성할 때 시큐리티 컨텍스트를 설정할 수 있는 기능을 제공한다.

    @Autowired MockMvc mockMvc;
    @Autowired AccountRepository accountRepository;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired ObjectMapper objectMapper;
    @Autowired TagRepository tagRepository;
    @Autowired AccountService accountService;
    @Autowired ZoneRepository zoneRepository;

    private Zone testZone = Zone.builder().city("test").localNameOfCity("테스트시").province("테스트도").build();

    @BeforeEach
    void beforeEach(){
        zoneRepository.save(testZone);
    }

    @AfterEach
    void afterEach(){
        accountRepository.deleteAll();
        zoneRepository.deleteAll();
    }

    @WithAccount("jjinse")
    @DisplayName("프로필 폼 요청")
    @Test
    void updateProfileForm() throws Exception{
        mockMvc.perform(get(ROOT + SETTINGS + PROFILE)) // get요청일지라도 인증된 사용자에 대해서 접근할 수 있기 때문에 인증된 사용자를 생성해줘야한다.
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

        mockMvc.perform(post(ROOT + SETTINGS + PROFILE)
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

        mockMvc.perform(post(ROOT + SETTINGS + PROFILE)
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

    mockMvc.perform(get(ROOT + SETTINGS + PASSWORD))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("passwordForm"));
    }

    @WithAccount("jjinse")
    @DisplayName("패스워드 수정 - 입력값 정상")
    @Test
    void updatePassword_success() throws Exception{

        mockMvc.perform(post(ROOT + SETTINGS + PASSWORD)
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

        mockMvc.perform(post(ROOT + SETTINGS + PASSWORD)
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

        mockMvc.perform(get(ROOT + SETTINGS + NOTIFICATIONS))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("notifications"));
    }

    @WithAccount("jjinse")
    @DisplayName("알림 설정")
    @Test
    void updateNotifications_success() throws Exception{

        mockMvc.perform(post(ROOT + SETTINGS + NOTIFICATIONS)
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

        mockMvc.perform(post(ROOT + SETTINGS + NOTIFICATIONS)
                .param("studyCreatedByEmail","hellohihihi")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("settings/notifications"))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("notifications"));
    }

    @WithAccount("jjinse")
    @DisplayName("계정의 태그 수정 폼")
    @Test
    void updateTagsForm() throws Exception {
        mockMvc.perform(get(ROOT + SETTINGS + TAGS))
                .andExpect(view().name("settings/tags"))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("tags"))
                .andExpect(model().attributeExists("whitelist"));
    }

    @WithAccount("jjinse")
    @DisplayName("계정에 태그 추가")
    @Test
    void addTag() throws Exception{ // TODO 테스트 코드 수정 필요

        TagForm tagForm = new TagForm();
        tagForm.setTagTitle("newTag");

        mockMvc.perform(post(ROOT + SETTINGS + TAGS + "/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tagForm))
                .with(csrf()))
                .andExpect(status().isOk()); // 추가 요청
        // JSON의 형태로 post 요청의 body부분에 데이터를 넣어서 전송.
        Tag tag = tagRepository.findByTitle("newTag");
        assertNotNull(tag);
        assertTrue(accountRepository.findByNickname("jjinse").getTags().contains(tag));
        // org.hibernate.LazyInitializationException: failed to lazily initialize a collection of role: com.sejin.domain.Account.tags, could not initialize proxy - no Session
        // accountRepository.findByNickname("jjinse")까지는 트랜잭션이 적용이 되지만, 이 메서드가 호출되고 난 이후에는 트랜잭션이 적용이 되지 않는다.
        // 그래서 관계에 대한 tags 정보를 찾지 못하는 것이다.
        // 이번 테스트에서는 이러한 일이 많이 발생할 것이기 때문에 테스트 클래스에서 Transactional을 지정해준다.
    }

    @WithAccount("jjinse")
    @DisplayName("계정에 태그 삭제")
    @Test
    void removeTag() throws Exception { // TODO 테스트 코드 수정 필요
        Account jjinse = accountRepository.findByNickname("jjinse");
        Tag newTag = tagRepository.save(Tag.builder().title("newTag").build());
        accountService.addTag(jjinse,newTag);

        assertTrue(jjinse.getTags().contains(newTag)); // 일단 먼저 account에 태그 정보를 저장하기

        TagForm tagForm = new TagForm();
        tagForm.setTagTitle("newTag");  // 삭제 AJAX 요청을 보낼 태그 정보를 생성

        mockMvc.perform(post(ROOT + SETTINGS + TAGS + "/remove")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tagForm))
                .with(csrf()))
                .andExpect(status().isOk()); // 삭제 요청
        // 제대로 동작했다면 newTag에 해당하는 태그가 account 객체와 연결이 끊겼을 것임.
        assertFalse(jjinse.getTags().contains(newTag));

    }

    @WithAccount("jjinse")
    @DisplayName("계정의 지역 정보 수정 폼")
    @Test
    void updateZoneForm() throws Exception {
    mockMvc.perform(get(ROOT + SETTINGS + ZONES))
            .andExpect(view().name(SETTINGS + ZONES))
            .andExpect(model().attributeExists("account"))
            .andExpect(model().attributeExists("whitelist"))
            .andExpect(model().attributeExists("zones"));
    }

    @WithAccount("jjinse")
    @DisplayName("계정에 지역 정보 추가")
    @Test
    void addZone() throws Exception {
        ZoneForm zoneForm = new ZoneForm();
        zoneForm.setZoneName(testZone.toString());

        mockMvc.perform(post(ROOT + SETTINGS + ZONES + "/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(zoneForm))
                .with(csrf()))
                .andExpect(status().isOk()); // 지역 추가 요청

        Account jjinse = accountRepository.findByNickname("jjinse");
        Zone zone = zoneRepository.findByCityAndProvince(testZone.getCity(), testZone.getProvince());
        assertTrue(jjinse.getZones().contains(zone));
    }

    @WithAccount("jjinse")
    @DisplayName("계정의 지역정보 삭제")
    @Test
    void removeZone() throws Exception {
        Account jjinse = accountRepository.findByNickname("jjinse");
        Zone zone = zoneRepository.findByCityAndProvince(testZone.getCity(), testZone.getProvince());

        accountService.addZone(jjinse,zone);

        ZoneForm zoneForm = new ZoneForm();
        zoneForm.setZoneName(testZone.toString());

        mockMvc.perform(post(ROOT + SETTINGS + ZONES + "/remove")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(zoneForm))
                .with(csrf()))
                .andExpect(status().isOk());

        assertFalse(jjinse.getZones().contains(zone));
    }
}