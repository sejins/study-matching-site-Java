package com.jinstudy.modules.study;

import com.jinstudy.modules.account.CurrentUser;
import com.jinstudy.modules.account.Account;
import com.jinstudy.modules.study.form.StudyForm;
import com.jinstudy.modules.study.validator.StudyFormValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import javax.validation.Valid;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RequiredArgsConstructor
@Controller
@Slf4j
public class StudyController {

    private final StudyService studyService;
    private final ModelMapper modelMapper;
    private final StudyFormValidator studyFormValidator;
    private final StudyRepository studyRepository;

    @InitBinder("studyForm")
    public void studyFormInitBinder(WebDataBinder webDataBinder){
        webDataBinder.addValidators(studyFormValidator);
    }

    @GetMapping("/new-study")
    public String newStudyForm(@CurrentUser Account account, Model model){
        model.addAttribute(account);
        model.addAttribute(new StudyForm());
        return "study/form";
    }

    @PostMapping("/new-study")
    public String newStudySubmit(@CurrentUser Account account, @Valid StudyForm studyForm, Errors errors, Model model){

        // studyFormValidator.validate(studyForm, errors); // InitBinder 를 사용하지 않고 이렇게 직접 검증을 할 수도 있다.

        if(errors.hasErrors()){
            model.addAttribute(account);
            return "study/form";
        }
        // 입력받은 스터디 정보로 스터디를 생성
        Study newStudy = studyService.createNewStudy(account, modelMapper.map(studyForm, Study.class));
        return "redirect:/study/"+ URLEncoder.encode(newStudy.getPath(), StandardCharsets.UTF_8);
    }

    @GetMapping("/study/{path}")
    public String viewStudy(@CurrentUser Account account, Model model, @PathVariable String path){
        Study study = studyService.getStudy(path);
        model.addAttribute(account);
        model.addAttribute(study);

        log.info(study.getManagersName().toString());

        return "study/view";
    }

    @GetMapping("/study/{path}/members")
    public String viewStudyMembers(@CurrentUser Account account, Model model, @PathVariable String path){
        Study study = studyService.getStudy(path);
        model.addAttribute(account);
        model.addAttribute(study);
        return "study/members";
    }

    @GetMapping("/study/{path}/join")
    public String joinStudy(@CurrentUser Account account, @PathVariable String path){
        Study study = studyService.getStudyToJoin(path); // 스터디 가입에 필요한 쿼리 정보만 가져오기 위해(멤버, 매니저 )
        studyService.addMember(study,account);
        return "redirect:/study/"+ study.getEncodedPath(path) + "/members";
    }

    @GetMapping("study/{path}/leave")
    public String leaveStudy(@CurrentUser Account account, @PathVariable String path){
        Study study = studyService.getStudyToRemove(path); // 스터디 가입에 필요한 쿼리 정보만 가져오기 위해(멤버)
        studyService.removeMember(study,account);
        return "redirect:/study/"+ study.getEncodedPath(path) + "/members";
    }
}
