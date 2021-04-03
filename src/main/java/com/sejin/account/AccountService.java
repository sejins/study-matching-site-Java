package com.sejin.account;

import com.sejin.domain.Account;
import com.sejin.settings.Profile;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.Valid;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AccountService implements UserDetailsService {

    private final AccountRepository accountRepository;
    private final JavaMailSender javaMailSender;
    private final PasswordEncoder passwordEncoder;


    public Account processNewAccount(SignUpForm signUpForm) {
        Account newAccount = saveNewAccount(signUpForm);
        newAccount.generateEmailCheckToken();
        sendSignUpConfirmEmail(newAccount);
        return newAccount;
    }

    private Account saveNewAccount(@Valid SignUpForm signUpForm) {
        Account account = Account.builder()
                .email(signUpForm.getEmail())
                .nickname(signUpForm.getNickname())
                .password(passwordEncoder.encode(signUpForm.getPassword()))
                .studyCreatedByWeb(true)
                .studyEnrollmentByWeb(true)
                .studyUpdatedByWeb(true)
                .build();

        return accountRepository.save(account);
    }

    public void sendSignUpConfirmEmail(Account newAccount) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(newAccount.getEmail());
        mailMessage.setSubject("진스터디, 회원가입 성공");
        mailMessage.setText("/check-email-token?token=" + newAccount.getEmailCheckToken() + "&email=" + newAccount.getEmail());

        javaMailSender.send(mailMessage);
    }


    public void login(Account account) {
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                new UserAccount(account), // Principal
                account.getPassword(),
                List.of(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContext securityContext = SecurityContextHolder.getContext();
        securityContext.setAuthentication(token);
    }

    @Transactional(readOnly = true)   // 읽기 전용 트랜잭션을 지정해줘서 write lock을 사용하지 않아서 성능에 더 유리하다.
    @Override
    public UserDetails loadUserByUsername(String emailOrNickname) throws UsernameNotFoundException {
        // 데이터 베이스에 있는 정보를 통해서 인증을 하기 위해서 사용 USerDetailsService 인터페이스 구현
        Account account = accountRepository.findByEmail(emailOrNickname);
        if(account==null){
            account = accountRepository.findByNickname(emailOrNickname);
        }
        if(account==null){
            throw new UsernameNotFoundException(emailOrNickname);
        }
        return new UserAccount(account);  // Principal을 리턴해야한다.
    }

    public void completeSignUp(Account account) {
        account.completeSignUp();
    }

    public void updateProfile(Account account, Profile profile) {
        account.setUrl(profile.getUrl());
        account.setOccupation(profile.getOccupation());
        account.setLocation(profile.getLocation());
        account.setBio(profile.getBio());
        account.setProfileImage(profile.getProfileImage()); // TODO 프로필 수정과 프로필 창에서는 변경된 프로필이 나올텐데, 네비게이션 바에도 반영이 되게 수정 해야함.
        // 여기까지만 해서 될 것 같지만, 문제가 존재한다.  Spring MVC적인 문제 , Spring JPA적인 문제.
        // 1. Spring MVC 문제는 컨트롤러 주석으로.
        // 2. Spring JPA 문제, 이대로 했으면 DB에 반영이 안된다.  -> AccountService 클래스에서 Transaction 처리를 해줬는데 왜?
        // 위의 completeSignUp 메서드에서 account 객체를 따라 가보면 account 객체는 persistant 객체이다. 컨트롤러 메서드가 시작할때 생긴 영속성 컨텍스트가 관리하는 상황에서 accountRepository를 통해서 가져온 account 객체이기 때문이다.
        // 하지만 이 메서드에서 account를 따라가보면 @CurrentUser를 통해서 세션에서 받아온 유저 정보에 해당하기 때문에, 영속성 컨텍스트가 관리하는 객체가 아니고, 이전에 트랜잭션이 끝나버린 detached 객체이다.
        accountRepository.save(account); // save를 통해서 해당 정보를 DB에 업로드를 시켜 줘야한다.

    }
}
