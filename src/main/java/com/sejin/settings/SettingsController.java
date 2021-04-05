package com.sejin.settings;

import com.sejin.account.AccountService;
import com.sejin.account.CurrentUser;
import com.sejin.domain.Account;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;


@Controller
@RequiredArgsConstructor
public class SettingsController {

    @InitBinder("passwordForm")
    public void initBinder(WebDataBinder webDataBinder){
        webDataBinder.addValidators(new PasswordFormValidator());
    }

    private final AccountService accountService;

    @GetMapping("/settings/profile")
    public String profileUpdateForm(@CurrentUser Account account, Model model){
        // profile 뷰에서 프로필 수정은 isOwner 를 통해 자신의 프로필에 대해서만 요청이 가능하므로
        // url에 유저에 대한 정보가 없어도 된다.
        model.addAttribute(account);
        model.addAttribute(new Profile(account)); // 폼 클래스를 뷰에 넘겨준다.
        return "settings/profile";
    }

    @PostMapping("/settings/profile")
    public String updateProfile(@CurrentUser Account account, @Valid @ModelAttribute Profile profile, Errors errors, Model model, RedirectAttributes attributes){
        // Error클래스 객체를 통해서 폼으로부터 받아오는 매개변수의 바인딩 에러를 검사한다. -> 여기서는 Profile클래스의 객체.  Error클래스의 객체는 항상 오른쪽에 위치해야 한다.

        if(errors.hasErrors()){ // @Valid 에 결려서 바인딩 에러가 발생할 경우!
            model.addAttribute(account); // 기존의 폼에서 받아오는 것들은 이미 model 객체에 존재하게 된다. 그래서 account 객체만 추가로 넣어주면 된다.
            return "settings/profile";
        }

        attributes.addFlashAttribute("message","프로필을 수정했습니다."); // 리다이렉트시에 간단한 정보를 넣어서 뷰에 전달할 수 있다.
        accountService.updateProfile(account,profile); // Transaction 처리때문에 AccountService클래스 객체에 위임을 한다.
        return "redirect:/settings/profile";

        // 여기서 발생하는 Spring MVC관련 이슈 -> Spring MVC는 @ModelAttribute로 받아오는 객체를 생성할때, 먼저 생성자를 통해서 인스턴스를 생성한 뒤, setter를 통해서 값을 주입하는 방식으로 동작한다.
        // 내 코드의 기존의 Profile 클래스의 생성자는 account를 인자로 받기 때문에, Spring MVC가 생성자를 호출하는 당시에 어디에서도 account 객체를 참조할 수 없어서 NullpointException이 발생하게 된다.
        // 그래서 Profile 클래스에 디폴트 생성자를 만들어 줘야 해결할 수 있다. -> 롬복 애너테이션을 사용하던지, 코드로 만들던지 알아서~

    }

    @GetMapping("/settings/password")
    public String passwordUpdateForm(@CurrentUser Account account, Model model){
        model.addAttribute(account);
        model.addAttribute(new PasswordForm());
        return "settings/password";
    }

    @PostMapping("/settings/password")
    public String updatePassword(@CurrentUser Account account, @Valid @ModelAttribute PasswordForm passwordForm, Errors errors, Model model, RedirectAttributes attributes){
        // @Valid를 통해서 패스워드가 8~50 자인지 검증하고, Validator를 통해서 패스워드와 확인 패스워드 값이 일치하는지 검증한다.
        if(errors.hasErrors()){
            model.addAttribute(account);
            return "settings/password";
        }

        accountService.updatePassword(account,passwordForm.getNewPassword());
        attributes.addFlashAttribute("message","패스워드를 변경했습니다.");
        return "redirect:" + "/settings/password";
    }

    @GetMapping("/settings/notifications")
    public String updateNotificationForm(@CurrentUser Account account, Model model){
        model.addAttribute(account);
        model.addAttribute(new Notifications(account));
        return "settings/notifications";
    }

    @PostMapping("/settings/notifications")
    public String updateNotifications(@CurrentUser Account account,@Valid @ModelAttribute Notifications notifications, Errors errors,
                                      Model model, RedirectAttributes attributes){
        if(errors.hasErrors()){
            model.addAttribute(account);
            return "settings/notifications";
        }

        accountService.updateNotifications(account,notifications);
        attributes.addFlashAttribute("message","알림 설정을 변경 했습니다.");
        return "redirect:"+"/settings/notifications";
    }
}
