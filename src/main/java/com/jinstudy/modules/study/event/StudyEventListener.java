package com.jinstudy.modules.study.event;

import com.jinstudy.modules.study.Study;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Async
@Component
public class StudyEventListener {

    @EventListener
    public void handleStudyCreatedEvent(StudyCreatedEvent studyCreatedEvent){ // 메서드 이름은 아무거나 해도 상관없다.
        Study study = studyCreatedEvent.getStudy();
        log.info(study.getTitle()+" is created!!!");
        //TODO 실제로 이메일 알림을 보내거나, Notification 정보를 수정하는 로직을 수행해야한다.
        throw new RuntimeException();
    }
}
