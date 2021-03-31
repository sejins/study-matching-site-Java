package com.sejin.settings;

import com.sejin.account.CurrentUser;
import com.sejin.domain.Account;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SettingsController {

    @GetMapping("/settings/profile")
    public String profileUpdateForm(@CurrentUser Account account, Model model){
        // profile 뷰에서 프로필 수정은 isOwner 를 통해 자신의 프로필에 대해서만 요청이 가능하므로
        // url에 유저에 대한 정보가 없어도 된다.
        model.addAttribute(account);
        model.addAttribute(new Profile(account)); // 폼 클래스를 뷰에 넘겨준다.
        return "settings/profile";
    }
}
