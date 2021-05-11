package com.jinstudy.domain;

import com.jinstudy.account.UserAccount;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StudyTest {

    Study study;
    Account account;
    UserAccount userAccount;

    @BeforeEach
    void beforeEach(){
        study = new Study();
        account = new Account();
        account.setNickname("jjinse");
        account.setPassword("12345");
        userAccount = new UserAccount(account);
    }

    @DisplayName("스터디를 공개했고, 인원 모집 중이고, 이미 멤버나 스터디 관리자가 아니라면 스터디에 가입이 가능.")
    @Test
    void isJoinable(){
        study.setPublished(true);
        study.setRecruiting(true);

        assertTrue(study.isJoinable(userAccount));
    }

    @DisplayName("스터디가 공개 상태이고, 인원도 모집 중이지만 관리자의 경우에는 가입이 불가능함.")
    @Test
    void isJoinable_false_for_manager(){
        study.setPublished(true);
        study.setRecruiting(true);
        study.addManager(account);

        assertFalse(study.isJoinable(userAccount));
    }

    @DisplayName("스터디가 공개 상태이고, 인원도 모집 중이지만 이미 스터디의 멤버인 경우에는 가입이 불가능함.")
    @Test
    void isJoinable_false_for_member(){
        study.setPublished(true);
        study.setRecruiting(true);
        study.addMember(account);

        assertFalse(study.isJoinable(userAccount));
    }

    @DisplayName("스터디가 비공개이거나, 인원을 모집중이지 않으면 참여가 불가능함.")
    @Test
    void isJoinable_false_for_non_recruiting_study(){
        study.setPublished(true);
        study.setRecruiting(false);
        assertFalse(study.isJoinable(userAccount));

        study.setPublished(false);
        study.setRecruiting(true);
        assertFalse(study.isJoinable(userAccount));
    }

    @DisplayName("스터디 관리자인지 확인.")
    @Test
    void isManager(){
        study.addManager(account);
        assertTrue(study.isManager(userAccount));
    }

    @DisplayName("스터디 멤버인지 확인.")
    @Test
    void isMember(){
        study.addMember(account);
        assertTrue(study.isMember(userAccount));
    }
}