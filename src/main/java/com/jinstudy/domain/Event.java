package com.jinstudy.domain;

import com.jinstudy.account.UserAccount;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@NamedEntityGraph(
        name="Event.withEnrollments",
        attributeNodes = @NamedAttributeNode("enrollments")
)
@Entity
@Getter @Setter @EqualsAndHashCode(of = "id")
public class Event {

    @Id @GeneratedValue
    private Long id;

    @Column(nullable = false)
    private String title;

    @Lob
    private String description;

    @Column(nullable = false)
    private LocalDateTime createdDateTime;

    @Column(nullable = false)
    private LocalDateTime endEnrollmentDateTime;

    @Column(nullable = false)
    private LocalDateTime startDateTime;

    @Column(nullable = false)
    private LocalDateTime endDateTime;

    private Integer limitOfEnrollment; // null을 의미있는 값으로 사용하기 위해서 기본형이 아닌 래퍼 클래스를 사용

    @Enumerated(EnumType.STRING) // Enumerated를 통해서 타입을 STRING으로 지정해줘야 DB에 문자열에 해당하는 값이 저장이 된다. 안그러면 ORDINAL 값이 저장이 되는데 이는 좋지않음.
    private EventType eventType;

    // 연관 관계를 위한 설정

    @ManyToOne
    private Study study; // Study 클래스(테이블)과 다대일 관계 + 현재 클래스에서만 Study 클래스를 참조한다.(단방향) -> 연관관계 테이블이 생성되지 않고, 외래키로 Study 테이블을 참조.

    @ManyToOne
    private Account createdBy; // Account 클래스(테이블)과 다대일 관계 + 현재 클래스에서만 Account 클래스를 참조한다.(단방향)

    @OneToMany(mappedBy = "event")
    private List<Enrollment> enrollments; // Enrollment 클래스(테이블)과 일대다 관계 + 양방향 관계
    // 단순하게 OneToMany를 하게 되면 별도의 조인 테이블을 생성해버린다. mappedBy를 통해서 Enrollment 클래스(테이블)과 양방향 관계라는 것을 명시해줘야함.
    // 그래야 별도의 조인 테이블의 생성 없이 Enrollment 테이블에서 Event 테이블을 외래키로 참조하는 일반적인 형태의 관계가 형성이 됨.

    public boolean isEnrollableFor(UserAccount userAccount){
        return isNotClosed() && !isAlreadyEnrolled(userAccount);
    }

    public boolean isDisenrollableFor(UserAccount userAccount){
        return isNotClosed() && isAlreadyEnrolled(userAccount);
    }

    public boolean isAttended(UserAccount userAccount){
        Account account = userAccount.getAccount();
        for(Enrollment e : this.enrollments){
            if(e.getAccount().equals(account) && e.isAttended()){
                return true;
            }
        }
        return false;
    }

    private boolean isAlreadyEnrolled(UserAccount userAccount) {
        Account account = userAccount.getAccount();
        for(Enrollment e : this.enrollments){
            if(e.getAccount().equals(account) && e.isAttended()){
                return true;
            }
        }
        return false;
    }

    private boolean isNotClosed() {
        return endEnrollmentDateTime.isAfter(LocalDateTime.now());
    }

    public int numberOfRemainSpots(){
        return this.limitOfEnrollment - (int)this.enrollments.stream().filter(Enrollment::isAccepted).count();
    }

}
