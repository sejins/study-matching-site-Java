package com.jinstudy.modules.account;


import com.jinstudy.modules.account.form.SignUpForm;
import com.jinstudy.modules.account.validator.SignUpFormValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;

@Controller
@RequiredArgsConstructor
public class AccountController { // 계정 관련 컨트롤러

    private final SignUpFormValidator signUpFormValidator;
    private final AccountService accountService;
    private final AccountRepository accountRepository;

    @InitBinder("signUpForm") // SignUpForm 클래스와 매핑이 되는 것임
    public void initBinder(WebDataBinder webDataBinder){
        webDataBinder.addValidators(signUpFormValidator);
    } // SignUpForm을 받아 올때 아래의 @Valid 에 의한 JSR303 검증과, webDataBinder에 의한 (커스텀)검증도 가능하게 된다.



    @GetMapping("/sign-up")
    public String signUpForm(Model model){
        model.addAttribute("signUpForm", new SignUpForm()); // 뷰에 넘겨줄때 model에 SignupForm 폼 객체를 넘겨준다.
        // 그다음 sign-up.html 에서 타임리프 템플릿 엔진에 의해 사용됨.
        return "account/sign-up";
    }



    @PostMapping("/sign-up")
    public String signUpSubmit(@Valid SignUpForm signUpForm, Errors errors){ // @Valid 어노테이션을 통해서 SignUpForm 객체에 대해서 검증
        // SignUpForm 폼 객체에서 검증을 통해서 바인딩 에러가 발생하게 되면, Errors 객체에 에러 정보가 들어간다. -> 세트로 기억해두자.
        if(errors.hasErrors()){
            return "account/sign-up";
        }

        // 여기서 직접 SignUpFormValidator 객체를 생성해서 validate 메서드를 통해서 (커스텀)검증을 할 수도 있고, 지금처럼 InitBinder를 통해서 (커스텀)검증을 할 수 도 있다

        Account account = accountService.processNewAccount(signUpForm);
        accountService.login(account);

        return "redirect:/";
    }

    @GetMapping("/check-email")
    public String checkEmail(@CurrentUser Account account, Model model){
        model.addAttribute("email",account.getEmail());
        return "/account/check-email";
    }

    @GetMapping("/resend-confirm-email")
    public String resendConfirmEmail(@CurrentUser Account account, Model model){
        if(account.canSendConfirmEmail()){
            accountService.sendSignUpConfirmEmail(account); // 메일을 보내는 코드는 재사용!! -> 개꿀~
            return "redirect:/";
        }
        else{
            model.addAttribute("error","인증 이메일은 1시간에 한 번만 정송할 수 있습니다.");
            model.addAttribute("email",account.getEmail());
            return "/account/check-email";
        }

    }

    @GetMapping("/check-email-token")
    public String checkEmailToken(String token, String email, Model model){
        Account account = accountRepository.findByEmail(email);
        if(account == null){
            model.addAttribute("error","wrong.email");
            return "account/checked-email";
        } // 여기 통과하면 이메일은 존재한다는 뜻임!

        if(!account.isValidToken(token)){
            model.addAttribute("error","wrong.token");
            return "account/checked-email";
        } // 여기까지 통과하면 이메일도 존재하고, 토큰값도 일치. 즉, 인증이 된 사용자임.

        accountService.completeSignUp(account);
        //account.completeSignUp();   // 이 메서드는 account 객체의 내용을 변경시키기 때문에 트랜잭션 내부에서 수행이 되어야한다.
        accountService.login(account);

        model.addAttribute("numberOfUser",accountRepository.count());
        model.addAttribute("nickname",account.getNickname());
        return "account/checked-email";

        // 셋다 보면 리턴하는 뷰는 동일한데 보내는 데이터가 다르다.(model 의 내용이 다르다.)
    }

    @GetMapping("/profile/{nickname}")
    public String viewProfile(@PathVariable String nickname, Model model, @CurrentUser Account account){
        // nickname은 조회를 하려고 하는 사람을 알아내기 위한 것이고, account 는 현재 로그인 한 사람에 대한 정보다.
        // 즉 현재 로그인 한 사람과 조회하려고 하는 프로필의 사람이 동일한 사람인지 알아내기 위함이다.


        Account byNickname = accountService.getAccount(nickname);

        model.addAttribute(byNickname);
        // model.addAttribute("account",byNickname); 과 같은 결과가 된다.  뷰에 캐멀 케이스로 전달이 죔.
        model.addAttribute("isOwner", byNickname.equals(account));
        // 현재 사용자와 프로필이 같은 사람인지?!
        // 다른 사람의 프로필을 열람 할 수도 있기 때문이다.
        return "account/profile";

    }

    @GetMapping("/email-login")
    public String emailLoginForm(){
        return "account/email-login";
    }

    @PostMapping("/email-login")
    public String emailLoginLink(String email, Model model, RedirectAttributes attributes){
        Account account = accountRepository.findByEmail(email);
        if(account == null){
            model.addAttribute("error","유효한 이메일 주소가 아닙니다.");
            return "account/email-login";
        }
        if(!account.canSendConfirmEmail()){
            model.addAttribute("error","이메일 로그인은 1시간 뒤에 사용할 수 있습니다.");
            //return "account/email-login";
        }
        accountService.sendLoginLink(account);
        attributes.addFlashAttribute("message","이메일 인증 메일을 발송했습니다.");
        return "redirect:/email-login";

    }

    @GetMapping("/login-by-email")
    public String loginByEmail(String token, String email, Model model){
        Account account = accountRepository.findByEmail(email);
        String view = "account/logged-in-by-email";
        if(account == null || !account.isValidToken(token)){
            model.addAttribute("error","로그인 할 수 없습니다.");
            return view;
        }
        accountService.login(account);
        return view;
    }


}