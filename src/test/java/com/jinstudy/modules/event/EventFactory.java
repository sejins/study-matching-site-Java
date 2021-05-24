package com.jinstudy.modules.event;

import com.jinstudy.modules.account.Account;
import com.jinstudy.modules.study.Study;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class EventFactory {

    @Autowired EventService eventService;

    public Event createEvent(String eventTitle, EventType eventType, int limit, Study study, Account account) {
        // 테스트 Event 객체를 생성하는 ObjectMother
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
