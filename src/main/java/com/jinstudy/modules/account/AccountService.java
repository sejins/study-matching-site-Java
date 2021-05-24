package com.jinstudy.modules.account;

import com.jinstudy.infra.config.AppProperties;
import com.jinstudy.modules.account.form.SignUpForm;
import com.jinstudy.modules.tag.Tag;
import com.jinstudy.modules.zone.Zone;
import com.jinstudy.infra.mail.EmailMessage;
import com.jinstudy.infra.mail.EmailService;
import com.jinstudy.modules.account.form.Notifications;
import com.jinstudy.modules.account.form.Profile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
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
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;


import javax.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Transactional
@Service
@RequiredArgsConstructor
@Slf4j
public class AccountService implements UserDetailsService {

    private final AccountRepository accountRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;
    private final TemplateEngine templateEngine;
    private final AppProperties appProperties;


    public Account processNewAccount(SignUpForm signUpForm) {
        Account newAccount = saveNewAccount(signUpForm); // persist한 account 객체. 아직 트랜잭션의 내부에 존재하는 것.
        sendSignUpConfirmEmail(newAccount); // 하지만 만약 sendSignUpConfirmEmail 메서드를 수행하다가 에러가 발생한다면..???!  트랜잭션의 원자성원리에 따라서 Rollback
        // 롤백이 발생하는 경우에는 새로운 유저 정보가 DB에 저장되지 않는다.
        return newAccount;
    }

    private Account saveNewAccount(@Valid SignUpForm signUpForm) {
        signUpForm.setPassword(passwordEncoder.encode(signUpForm.getPassword()));
        Account account = modelMapper.map(signUpForm,Account.class);
        /*
        // 리팩토링
        Account account = Account.builder()
                .email(signUpForm.getEmail())
                .nickname(signUpForm.getNickname())
                .password(passwordEncoder.encode(signUpForm.getPassword()))
                .studyCreatedByWeb(true)
                .studyEnrollmentByWeb(true)
                .studyUpdatedByWeb(true)
                .build();
         */
        account.generateEmailCheckToken();
        return accountRepository.save(account);
    }

    public void sendSignUpConfirmEmail(Account newAccount) { // 이랗게 사용하면 local 환경, dev 환경에서 모두 동일한 코드로 사용할 수 있다. --> 추상화를 통해서 재사용이 가능해짐.
        Context context = new Context();
        context.setVariable("link","/check-email-token?token=" + newAccount.getEmailCheckToken() + "&email=" + newAccount.getEmail());
        context.setVariable("nickname",newAccount.getNickname());
        context.setVariable("linkName","이메일 인증하기");
        context.setVariable("message","진 스터디를 사용하려면 링크를 클릭하세요 😺");
//        context.setVariable("host","http://localhost:8080"); // 이렇게 하드코딩하면 안된다.
        context.setVariable("host",appProperties.getHost()); // application.properties 에 지정한 값을 바인딩 받아서 가져온다.

        String message = templateEngine.process("mail/simple-link", context); // view resolver가 알아서 prefix로 templates를 postfix로 .html을 붙혀준다.

        // EmailService로 추상화
        EmailMessage emailMessage = EmailMessage.builder().
                        to(newAccount.getEmail())
                        .subject("진스터디, 회원가입 인증")
                        .message(message).build();

        emailService.sendEmail(emailMessage);
    }


    public void login(Account account) {
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                new UserAccount(account), // Principal  -> 이메일 또는 닉네임을 통해서 로그인을 할 수 있게 하기 위해!!
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

        modelMapper.map(profile,account); //  source, destination

        //ModelMapper를 통해 간결한 코드로 객체의 매핑이 가능함.
        //account.setUrl(profile.getUrl());
        //account.setOccupation(profile.getOccupation());
        //account.setLocation(profile.getLocation());
        //account.setBio(profile.getBio());
        //account.setProfileImage(profile.getProfileImage());

        // 여기까지만 해서 될 것 같지만, 문제가 존재한다.  Spring MVC적인 문제 , Spring JPA적인 문제.
        // 1. Spring MVC 문제는 컨트롤러 주석으로.
        // 2. Spring JPA 문제, 이대로 했으면 DB에 반영이 안된다.  -> AccountService 클래스에서 Transaction 처리를 해줬는데 왜?
        // 위의 completeSignUp 메서드에서 account 객체를 따라 가보면 account 객체는 persistant 객체이다. 컨트롤러 메서드가 시작할때 생긴 영속성 컨텍스트가 관리하는 상황에서 accountRepository를 통해서 가져온 account 객체이기 때문이다.
        // 하지만 이 메서드에서 account를 따라가보면 @CurrentUser를 통해서 세션에서 받아온 유저 정보에 해당하기 때문에, 영속성 컨텍스트가 관리하는 객체가 아니고, 이전에 트랜잭션이 끝나버린 detached 객체이다.
        accountRepository.save(account); // save를 통해서 해당 정보를 DB에 업로드를 시켜 줘야한다.
    }

    public void updatePassword(Account account, String newPassword) {
        account.setPassword(passwordEncoder.encode(newPassword)); // 다시 새로운 패스워드를 저장할때도 인코딩을 해줘야한다.
        accountRepository.save(account);
    }

    public void updateNotifications(Account account, Notifications notifications) {

        modelMapper.map(notifications,account);

        //account.setStudyCreatedByEmail(notifications.isStudyCreatedByEmail());
        //account.setStudyCreatedByWeb(notifications.isStudyCreatedByWeb());
        //account.setStudyEnrollmentByEmail(notifications.isStudyEnrollmentByEmail());
        //account.setStudyEnrollmentByWeb(notifications.isStudyEnrollmentByWeb());
        //account.setStudyUpdatedByEmail(notifications.isStudyUpdatedByEmail());
        //account.setStudyUpdatedByWeb(notifications.isStudyUpdatedByWeb());
        accountRepository.save(account);
    }

    public void updateNickname(Account account, String nickname) {
        account.setNickname(nickname);
        accountRepository.save(account);
        login(account);
    }

    public void sendLoginLink(Account account) {
        // account에 대한 영속성 컨텍스트가 존재하고, account는 persist한 객체가 된다.

        Context context = new Context();
        context.setVariable("link","/login-by-email?toekn=" + account.getEmailCheckToken() + "&email=" + account.getEmail());
        context.setVariable("nickname",account.getNickname());
        context.setVariable("linkName","이메일로 로그인하기");
        context.setVariable("messge","진 스터디 로그인을 하시려면 링크를 클릭하세요 😺");
        context.setVariable("host",appProperties.getHost());

        //TODO sendLoginLink 메서드 호출할 때마다 이메일 인증 토큰값을 새로 생성해줘야하지 않나?? -->  안그러면 이 이메일로 전송된 링크로 언제든지 로그인이 가능한데!

        //EmailService로 추상화
        EmailMessage emailMessage = EmailMessage.builder()
                .to(account.getEmail())
                .subject("진스터디, 로그인 링크")
                .message("/login-by-email?toekn=" + account.getEmailCheckToken() + "&email=" + account.getEmail())
                .build();
        emailService.sendEmail(emailMessage);
    }

    public void addTag(Account account, Tag tag) {

        // detached한 account의 객체에 대해서는 @ManyToMany로 지정해놓은 부분이 인식이 안되어서 null이기 때문에 사용하면 nullPointException이 발생할 것이다.
        // 영속성 컨텍스트가 관리하는 account에 대해서만 @ManyToMany 관계에 대해서 인식?? 하고 있을 것 이라고 생각함.  --> 매우 중요한 부분이 될 듯!

        Optional<Account> byId = accountRepository.findById(account.getId()); //Optional 래퍼 클래스로
        byId.ifPresent(a->a.getTags().add(tag));
        // 여기서 살펴보면 매개변수로 받은 tag랑 tagRepository로 Tag DB에 저장한 객체가 동일하다.
        // 동일한 객체이기 때문에 JPA와 데이터베이스를 통해서 서로 연동이 되는 것 같음!.

    }

    public Set<Tag> getTags(Account account) {
        Optional<Account> byId = accountRepository.findById(account.getId());
        return byId.orElseThrow().getTags();
    }

    public void removeTag(Account account, Tag tag) {
        Optional<Account> byId = accountRepository.findById(account.getId());
        byId.ifPresent(a->a.getTags().remove(tag));
    }

    public Set<Zone> getZones(Account account) {
        Optional<Account> byId = accountRepository.findById(account.getId());
        return byId.orElseThrow().getZones();
    }

    public void addZone(Account account, Zone zone) {
        Optional<Account> byId = accountRepository.findById(account.getId());
        byId.ifPresent(a -> a.getZones().add(zone));
    }

    public void removeZone(Account account, Zone zone) {
        Optional<Account> byId = accountRepository.findById(account.getId());
        byId.ifPresent(a -> a.getZones().remove(zone));
    }

    public Account getAccount(String nickname) {
        Account byNickname = accountRepository.findByNickname(nickname);
        if(byNickname == null){
            throw new IllegalArgumentException(nickname + "에 해당하는 사용자가 없습니다.");
        }
        return byNickname;
    }
}
