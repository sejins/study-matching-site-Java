package com.jinstudy.modules.event;

import com.jinstudy.modules.account.WithAccount;
import com.jinstudy.modules.account.Account;
import com.jinstudy.modules.study.Study;
import com.jinstudy.modules.study.StudyControllerTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;


import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


class EventControllerTest extends StudyControllerTest {

    @Autowired EventService eventService;
    @Autowired EnrollmentRepository enrollmentRepository;

    @WithAccount("jjinse")
    @DisplayName("스터디 생성폼 요청 - 성공")
    @Test
    void createEventForm() throws Exception {
        Account jjinse = accountRepository.findByNickname("jjinse");
        Study study = createStudy("test-study",jjinse);

        mockMvc.perform(get("/study/"+study.getPath()+"/new-event"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("study"))
                .andExpect(model().attributeExists("eventForm"));
    }

    @WithAccount("jjinse")
    @DisplayName("선착순 모임에 참가 신청 - 자동 수락")
    @Test
    void newEnrollment_FCFS_event_accepted() throws Exception {
        Account sejin = createAccount("sejin");
        Study study = createStudy("test-study",sejin);
        Event event = createEvent("test-event", EventType.FCFS, 2, study, sejin);

        mockMvc.perform(post("/study/"+study.getPath()+"/event/"+event.getId()+"/enroll")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/study/"+study.getPath()+"/event/"+event.getId()));

        Account jjinse = accountRepository.findByNickname("jjinse");
        //assertTrue(enrollmentRepository.findByEventAndAccount(event,jjinse).isAccepted());
        isAccepted(event,jjinse);
    }

    @WithAccount("jjinse")
    @DisplayName("선착순 모임에 참가 신청 - 대기상태( 이미 인원이 다 차서)")
    @Test
    void  newEnrollment_FCFS_event_not_accepted() throws Exception {
        Account sejin = createAccount("sejin");
        Study study = createStudy("test-study",sejin);
        Event event = createEvent("test-event",EventType.FCFS,2,study,sejin);

        Account haverz = createAccount("haverz");
        Account kante = createAccount("kante");
        eventService.newEnrollment(event,haverz);
        eventService.newEnrollment(event,kante);

        mockMvc.perform(post("/study/"+study.getPath()+"/event/"+event.getId()+"/enroll")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/study/"+study.getPath()+"/event/"+event.getId()));

        Account jjinse = accountRepository.findByNickname("jjinse");
        //assertFalse(enrollmentRepository.findByEventAndAccount(event,jjinse).isAccepted());
        isNotAccepted(event,jjinse);
    }

    @WithAccount("jjinse")
    @DisplayName("확정 신청자가 참여를 취소하게 되면 비 확정자중에서 한명이 확정상태가 된다.")
    @Test
    void cancelEnrollment_FCFS_event_change_to_accepted() throws Exception {
        Account sejin = createAccount("sejin");
        Study study = createStudy("test-study",sejin);
        Event event = createEvent("test-event",EventType.FCFS,2,study,sejin);

        Account haverz = createAccount("haverz");
        Account kante = createAccount("kante");
        eventService.newEnrollment(event,haverz);
        eventService.newEnrollment(event,kante);

        mockMvc.perform(post("/study/"+study.getPath()+"/event/"+event.getId()+"/enroll")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/study/"+study.getPath()+"/event/"+event.getId()));

        Account jjinse = accountRepository.findByNickname("jjinse");

        isNotAccepted(event,jjinse);
        eventService.cancelEnrollment(event,haverz);
        isAccepted(event,jjinse);

    }

    @WithAccount("jjinse")
    @DisplayName("비확정 신청자가 참여를 취소하게 되면 이미 확정 신청자들에게는 아무런 변화도 없다.")
    @Test
    void cancelEnrollment_FCFS_event_noOne_change_to_accepted() throws Exception {

        Account sejin = createAccount("sejin");
        Study study = createStudy("test-study",sejin);
        Event event = createEvent("test-event",EventType.FCFS,2,study,sejin);

        Account haverz = createAccount("haverz");
        Account kante = createAccount("kante");
        Account jjinse = accountRepository.findByNickname("jjinse");
        eventService.newEnrollment(event,haverz);
        eventService.newEnrollment(event,kante);
        eventService.newEnrollment(event,jjinse);

        mockMvc.perform(post("/study/"+study.getPath()+"/event/"+event.getId()+"/disenroll")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/study/"+study.getPath()+"/event/"+event.getId()));

        isAccepted(event,haverz);
        isAccepted(event,kante);
        assertNull(enrollmentRepository.findByEventAndAccount(event,jjinse));
    }

    @WithAccount("jjinse")
    @DisplayName("관리자 확인 모임 - 가입해도 확정상태가 아님")
    @Test
    void createEnroll_CONFIRMATIVE_event_no_accepted() throws Exception {
        Account sejin = createAccount("sejin");
        Study study = createStudy("test-study",sejin);
        Event event = createEvent("test-event",EventType.CONFIRMATIVE,2,study,sejin);

        mockMvc.perform(post("/study/"+study.getPath()+"/event/"+event.getId()+"/enroll")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/study/"+study.getPath()+"/event/"+event.getId()));

        Account jjinse = accountRepository.findByNickname("jjinse");
        isNotAccepted(event, jjinse);
    }

    private void isAccepted(Event event, Account account) { // 리팩토링한 메서드
        assertTrue(enrollmentRepository.findByEventAndAccount(event,account).isAccepted());
    }

    private void isNotAccepted(Event event, Account account) { // 리팩토링한 메서드
        assertFalse(enrollmentRepository.findByEventAndAccount(event,account).isAccepted());
    }

    private Event createEvent(String eventTitle, EventType eventType, int limit, Study study, Account account) {
        Event event = new Event();
        event.setEventType(eventType);
        event.setLimitOfEnrollment(limit);
        event.setTitle(eventTitle);
        event.setCreatedDateTime(LocalDateTime.now());
        event.setEndEnrollmentDateTime(LocalDateTime.now().plusDays(1));
        event.setStartDateTime(LocalDateTime.now().plusDays(1).plusHours(5));
        event.setEndDateTime(LocalDateTime.now().plusDays(1).plusHours(7));
        return eventService.createEvent(event,study,account);
    }


}