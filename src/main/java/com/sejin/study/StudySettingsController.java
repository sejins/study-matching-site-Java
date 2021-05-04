package com.sejin.study;


import com.sejin.account.CurrentUser;
import com.sejin.domain.Account;
import com.sejin.domain.Study;
import com.sejin.study.form.StudyDescriptionForm;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RequiredArgsConstructor
@RequestMapping("/study/{path}/settings")
@Controller
public class StudySettingsController {

    private final StudyService studyService;
    private final ModelMapper modelMapper;

    @GetMapping("/description")
    public String viewStudySettingForm(@CurrentUser Account account, Model model, @PathVariable String path){
        Study study = studyService.getStudyToUpdate(account, path);
        // 업데이트 기능은 관리자만 사용 가능하도록 구현.  이를 위한 메서드.  -> path에 해당하는 스터디를 불러오고, account가 관리자로 등록이 되어있어야 study 객체가 반환. 안그러면 예외던지게 했음.
        model.addAttribute(account);
        model.addAttribute(study);
        model.addAttribute(modelMapper.map(study, StudyDescriptionForm.class));

        return "study/settings/description";
    }

    @PostMapping("/description")
    public String updateStudyInfo(@CurrentUser Account account, Model model, @Valid StudyDescriptionForm studyDescriptionForm, Errors errors,
                                  @PathVariable String path, RedirectAttributes attributes){

        Study study = studyService.getStudy(path);
        // study 객체는 persist 상태이다. studyService 클래스가 트랜잭션 내부에 있기도 하고, Service 클래스에 @Transactional이 없다고 하더라도 Repository를 통해서 가지고 왔기 때문!
        // 영속성 컨텍스트는 이미 OpenSessionInView 필터에 의해서 해당 post 요청이 실행되기 전에 열려있었다.
        // 그렇기 때문에, 이는 persist 상태이다.
        if(errors.hasErrors()){
            model.addAttribute(account);
            model.addAttribute(study);
            return "study/settings/description";
        }
        studyService.updateStudyDescription(study, studyDescriptionForm);
        // 그래서 트랜잭션 내부에서 변경한 Study 정보를 commit만 해주면 되는 것이다.

        attributes.addFlashAttribute("message","성공적으로 수정되었습니다.");
        return "redirect:/study/"+getPath(path)+"/settings/description";
    }

    private String getPath(String path) {
        return URLEncoder.encode(path, StandardCharsets.UTF_8);
    }

}
