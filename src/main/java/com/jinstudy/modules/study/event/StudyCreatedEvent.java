package com.jinstudy.modules.study.event;

import com.jinstudy.modules.study.Study;
import lombok.Getter;

@Getter
public class StudyCreatedEvent {
    private Study study;

    public StudyCreatedEvent(Study study){ // 스터디가 생성됐을 때 발생하는 이벤트
        this.study = study;
    }
}
