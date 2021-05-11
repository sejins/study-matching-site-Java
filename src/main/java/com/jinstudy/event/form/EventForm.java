package com.jinstudy.event.form;

import com.jinstudy.domain.EventType;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

import java.time.LocalDateTime;

@Data
public class EventForm {

    @NotBlank
    @Length(max = 50)
    private String title;

    private EventType eventType = EventType.FCFS; // 폼에 선착순을 기본값으로 보여준다.

    private String description;

    @Min(2)
    private Integer limitOfEnrollments = 2;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) // html에서 기본으로 제공하는 포맷을 LocalDateTime에 매핑하기 위한 설정
    private LocalDateTime endEnrollmentDateTime;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime startDateTime;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime endDateTime;
}
