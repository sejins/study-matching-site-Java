package com.sejin.study;

import com.sejin.account.CurrentUser;
import com.sejin.domain.Account;
import com.sejin.domain.Study;
import com.sejin.study.form.StudyForm;
import com.sejin.study.validator.StudyFormValidator;
import lombok.RequiredArgsConstructor;
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
        if(errors.hasErrors()){
            model.addAttribute(account);
            return "study/form";
        }
        // 입력받은 스터디 정보로 스터디를 생성하는 로직
        Study newStudy = studyService.createNewStudy(account, modelMapper.map(studyForm, Study.class));
        return "redirect:/study/"+ URLEncoder.encode(newStudy.getPath(), StandardCharsets.UTF_8);
    }

    @GetMapping("/study/{path}")
    public String viewStudy(@CurrentUser Account account, Model model, @PathVariable String path){
        Study study = studyService.getStudy(path); // path 에 해당하는 스터디 못 찾으면  예외 던지게 해놨음.
        model.addAttribute(account);
        model.addAttribute(study);
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
        Study study = studyService.getStudyToJoinOrRemove(path); // 스터디에 가입하기 위해서 필요한 정보들만 DB에서 쿼리하기 위해
        studyService.addMember(study,account);
        return "redirect:/study/"+ study.getEncodedPath(path) + "/members";
    }

    @GetMapping("study/{path}/leave")
    public String leaveStudy(@CurrentUser Account account, @PathVariable String path){
        Study study = studyService.getStudyToJoinOrRemove(path);
        studyService.removeMember(study,account);
        return "redirect:/study/"+ study.getEncodedPath(path) + "/members";
    }
}
