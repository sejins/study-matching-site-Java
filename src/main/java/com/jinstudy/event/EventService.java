package com.jinstudy.event;

import com.jinstudy.domain.Account;
import com.jinstudy.domain.Event;
import com.jinstudy.domain.Study;
import com.jinstudy.event.form.EventForm;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final ModelMapper modelMapper;

    public Event createEvent(Event event, Study study, Account account) {
        event.setStudy(study);
        event.setCreatedBy(account);
        event.setCreatedDateTime(LocalDateTime.now());
        return eventRepository.save(event);
    }

    public void updateEvent(Event event, EventForm eventForm) {
        modelMapper.map(eventForm,event); // event 객체는 persist 객체이기 때문!
        //TODO 선착순 모임의 경우, 모집 인원을 늘리면 자동으로 추가 인원의 참가 신텅을 확정 상태로 변경해야한다. (나중에 해야할 일)
    }

    public void deleteEvent(Event event) {
        eventRepository.delete(event);
    }
}
