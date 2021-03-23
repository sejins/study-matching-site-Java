package com.sejin.account;

import com.sejin.domain.Account;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;
import java.util.List;

// 스프링 시큐리티에서 필요로 하는 account와 내가 생성한 도메인 account와 연동을 하기 위한 클래스  -> Principal로 객체로 사용할 클래스!
@Getter
public class UserAccount extends User {
    private Account account;

    public UserAccount(Account account) {
        super(account.getNickname(), account.getPassword(), List.of(new SimpleGrantedAuthority("ROLE_USER")));
        this.account = account;
    }
}


