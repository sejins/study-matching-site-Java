package com.jinstudy.modules.event;


import com.jinstudy.modules.account.Account;
import lombok.EqualsAndHashCode;
import lombok.Setter;
import lombok.Getter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import java.time.LocalDateTime;

@Entity
@Getter @Setter @EqualsAndHashCode(of = "id")
public class Enrollment {

    @Id @GeneratedValue
    private Long id;

    @ManyToOne
    private Event event; // Event 클래스(테이블)과 다대일 관계. Event 클래스와 양방향 관계를 설정했다.

    @ManyToOne
    private Account account; // 마찬가지.

    private LocalDateTime enrolledAt;

    private boolean accepted; // 승인

    private boolean attended; // 참여중

}
