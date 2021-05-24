package com.jinstudy.modules.study;

import com.jinstudy.modules.account.Account;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StudyFactory { // 테스트 Study 객체를 생성하는 ObjectMother

    @Autowired StudyService studyService;
    public Study createStudy(String path, Account account) {
        Study study = new Study();
        study.setPath(path);
        return studyService.createNewStudy(account,study);
    }
}
