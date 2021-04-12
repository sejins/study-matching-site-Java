package com.sejin.settings;

import com.sejin.account.AccountService;
import com.sejin.account.CurrentUser;
import com.sejin.domain.Account;
import com.sejin.domain.Tag;
import com.sejin.settings.form.*;
import com.sejin.settings.validator.PasswordFormValidator;
import com.sejin.tag.TagRepository;
import lombok.RequiredArgsConstructor;
import org.apache.catalina.connector.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.util.Set;
import java.util.stream.Collectors;


@Controller
@RequiredArgsConstructor
public class SettingsController {

    @InitBinder("passwordForm")
    public void initBinder(WebDataBinder webDataBinder){
        webDataBinder.addValidators(new PasswordFormValidator());
    }
    // TODO 닉네임 Validator로 Validation 해주기~~ 빼먹은듯

    private final AccountService accountService;
    private final TagRepository tagRepository;

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

    @GetMapping("/settings/account")
    public String updateAccountForm(@CurrentUser Account account, Model model){
        model.addAttribute(account);
        model.addAttribute(new NicknameForm(account));
        return "settings/account";
    }

    @PostMapping("/settings/account")
    public String updateAccount(@CurrentUser Account account, @Valid @ModelAttribute NicknameForm nicknameForm, Errors errors,
                                Model model, RedirectAttributes attributes){
        if(errors.hasErrors()){
            model.addAttribute(account);
            return "settings/account";
        }

        accountService.updateNickname(account,nicknameForm.getNickname());
        attributes.addFlashAttribute("message","닉네임을 수정했습니다.");
        return "redirect:"+"/settings/account";
    }

    @GetMapping("/settings/tags")
    public String updateTags(@CurrentUser Account account, Model model){
        model.addAttribute(account);
        // detached 객체에 대해서는 @ManyToMany로 관계를 맺었기 떄문에, tags 필드에 대한 값이 null이다! 즉, 맺은 관계에 대해서 참조가 되지 않는다.
        // 그래서 persistent한 account 객체를 다시 생성할 필요가 있다.
        // 그래서 persistent 한 객체에 대해서는 DB에 값을 넣지 않아도 null이 아닌 비어있는 Set이 출력된다.
        Set<Tag> tags = accountService.getTags(account);
        // tags 정보들을 문자열 리스트로 변경하는 작업이 필요하다. 그다음 뷰에 추가 해준다.
        // Java8 이후의 문법. 람다식과 스트림!!
        // tags의 정보들을 title에 해당하는 문자열들로 매핑하고, collector를 통해서 리스트로 만듦.
        model.addAttribute("tags",tags.stream().map(Tag::getTitle).collect(Collectors.toList()));
        return "settings/tags";
    }

    @PostMapping("/settings/tags/add")
    @ResponseBody
    public ResponseEntity addTag(@CurrentUser Account account, @RequestBody TagForm tagForm){
        String title = tagForm.getTagTitle();
        // title에 해당하는 태그가 있는지 DB에서 확인하고, 없으면 저장해서 account에 추가를 해주면 된다. -> TagRepository가 필요하당!
        // Optional로 받아오는 코드도 있었는데 이건 나중에 람다 다시 복습하고 이해하던가~

        Tag tag = tagRepository.findByTitle(title);
        if(tag == null){
            tag = tagRepository.save(Tag.builder().title(tagForm.getTagTitle()).build());
        }
        // tag가 없으면 DE에 새로 만들어서 account와 관계를 맺고, 이미 존재하면 존재하는 객체로 account와 관계를 맺는다.
        accountService.addTag(account, tag);
        return ResponseEntity.ok().build(); // AJAX요청에 대한 응답으로, 뷰룰 응답으로 보내는 것이 아니다!
    }

    @PostMapping("settings/tags/remove")
    @ResponseBody
    public ResponseEntity removeTag(@CurrentUser Account account, @RequestBody TagForm tagForm){
        String title = tagForm.getTagTitle();
        Tag tag = tagRepository.findByTitle(title);
        if(tag==null){
            return ResponseEntity.badRequest().build();
        }
        accountService.removeTag(account,tag);
        return ResponseEntity.ok().build();
    }
}
