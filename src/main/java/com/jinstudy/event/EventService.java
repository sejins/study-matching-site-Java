package com.jinstudy.event;

import com.jinstudy.domain.*;
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
    private final EnrollmentRepository enrollmentRepository;

    public Event createEvent(Event event, Study study, Account account) {
        event.setStudy(study);
        event.setCreatedBy(account);
        event.setCreatedDateTime(LocalDateTime.now());
        return eventRepository.save(event);
    }

    public void updateEvent(Event event, EventForm eventForm) {
        modelMapper.map(eventForm,event); // event 객체는 persist 객체이기 때문!

        event.acceptWaitingList(); // 선착순 모임의 경우 모집 인원을 늘리면 대기상태의 인원들을 그만큼 확정상태로 변경해주어야한다.
    }

    public void deleteEvent(Event event) {
        eventRepository.delete(event);
    }

    public void newEnrollment(Event event, Account account) {
        if(!enrollmentRepository.existsByEventAndAccount(event,account)){ // 기존의 신청이 존재하면 이번 신청은 무시
            Enrollment newEnrollment = new Enrollment();
            newEnrollment.setEnrolledAt(LocalDateTime.now());
            newEnrollment.setAccount(account);
            newEnrollment.setAccepted(event.isAbleToAcceptWaitingEnrollment()); // Event가 선착순 참여인지 관리자 확인 참여인지 판단 후 accepted를 설정.
            event.addEnrollment(newEnrollment);
            enrollmentRepository.save(newEnrollment);
        }
    }

    public void cancelEnrollment(Event event, Account account) {
        // Enrollment 삭제 -> 연관관계 끊고, Repository를 통해서 해당 Enrollment 삭제.
        Enrollment enrollment = enrollmentRepository.findByEventAndAccount(event,account);
        event.removeEnrollment(enrollment); //연관관계 제거
        enrollmentRepository.delete(enrollment);
        event.acceptNextWaitingEnrollment(); // 만약 확정상태인 참여자가 빠져나간것이면, 대기상태에 있는 사람 중 한명을 확정상태로 변경해줘야함.
    }

    public void acceptEnrollment(Event event, Enrollment enrollment) {
        if(event.getEventType() == EventType.CONFIRMATIVE && event.getLimitOfEnrollment() > event.getAcceptedEnrollments()){
            enrollment.setAccepted(true);
        }
    }

    public void rejectEnrollment(Event event, Enrollment enrollment) {
        if(event.getEventType()==EventType.CONFIRMATIVE){
            enrollment.setAccepted(false);
        }
    }

    public void checkInEnrollment(Enrollment enrollment) {
        enrollment.setAttended(true);
    }

    public void cancelCheckInEnrollment(Enrollment enrollment) {
        enrollment.setAttended(false);
    }
}
