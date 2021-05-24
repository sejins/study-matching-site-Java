package com.jinstudy.modules.event.validator;

import com.jinstudy.modules.event.Event;
import com.jinstudy.modules.event.form.EventForm;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.time.LocalDateTime;

@Component
public class EventValidator implements Validator {

    @Override
    public boolean supports(Class<?> aClass) {
        return EventForm.class.isAssignableFrom(aClass);
    }

    @Override
    public void validate(Object target, Errors errors) {
        EventForm eventForm = (EventForm)target;

        if(isNotValidEndEnrollDateTime(eventForm)){
            errors.rejectValue("endEnrollmentDateTime","wrong.datetime","모임 접수 종료 일시를 정확히 입력하세요!");
        }

        if(isNotValidStartDateTime(eventForm)){
            errors.rejectValue("startDateTime","wrong.datetime","모임 시작 일시를 정확히 입력하세요!");
        }

        if(isNotValidEndDateTime(eventForm)){
            errors.rejectValue("endDateTime","wrong.datetime","모임 종료 일시를 정확히 입력하세요!");
        }
    }

    // 리팩토링 해낸 메서드들
    private boolean isNotValidEndEnrollDateTime(EventForm eventForm) {
        return eventForm.getEndEnrollmentDateTime().isBefore(LocalDateTime.now());
    }

    private boolean isNotValidStartDateTime(EventForm eventForm) {
        return eventForm.getStartDateTime().isBefore(eventForm.getEndEnrollmentDateTime());
    }

    private boolean isNotValidEndDateTime(EventForm eventForm) {
        return eventForm.getEndDateTime().isBefore(eventForm.getStartDateTime());
    }
    // 코드를 읽었을때 적절한 이름의 메소드로 리팩토링을 해놓으면, 다음에 코드를 읽을때 이해하는데 있어서 수월하다.
    // 메서드를 읽고 대략적으로 어떤 기능을 하는지 보고, 실제 내용은 메서드의 구현부를 보면된다.

    public void validateUpdateForm(EventForm eventForm, Event event, Errors errors) {
        if(eventForm.getLimitOfEnrollment() < event.getAcceptedEnrollments()){
            errors.rejectValue("limitOfEnrollment","wrong.value","확인된 참가 신청보다 모집 인원 수가 커야 합니다.");
        }
    }
}





