package com.jinstudy.modules.account;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AccountFactory {

    @Autowired private AccountRepository accountRepository;
    public Account createAccount(String name) { // 테스트 Account 객체를 생성하는 ObjectMother
        Account sejin = new Account();
        sejin.setNickname(name);
        sejin.setEmail(name + "@email.com");
        return accountRepository.save(sejin);
    }
}
