package com.sejin.main;

import com.sejin.account.CurrentUser;
import com.sejin.domain.Account;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {

    @GetMapping("/")
    public String home(@CurrentUser Account account, Model model){ // 매개변수로 들어오는 account 의 값이 null이거나 아니면 실제 Account의 인스턴스 이거나.  -> 애노테이션을 통해서 지정
        if(account!=null)
            model.addAttribute(account);

        return "index";
    }
}
