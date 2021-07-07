package com.jinstudy.modules.study;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jinstudy.modules.account.CurrentUser;
import com.jinstudy.modules.account.Account;
import com.jinstudy.modules.tag.Tag;
import com.jinstudy.modules.zone.Zone;
import com.jinstudy.modules.zone.ZoneForm;
import com.jinstudy.modules.tag.TagForm;
import com.jinstudy.modules.study.form.StudyDescriptionForm;
import com.jinstudy.modules.tag.TagRepository;
import com.jinstudy.modules.tag.TagService;
import com.jinstudy.modules.zone.ZoneRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@RequestMapping("/study/{path}/settings")
@Controller
public class StudySettingsController {

    private final StudyService studyService;
    private final ModelMapper modelMapper;
    private final TagRepository tagRepository;
    private final ObjectMapper objectMapper;
    private final TagService tagService;
    private final ZoneRepository zoneRepository;

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

        Study study = studyService.getStudyToUpdate(account, path);
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
        return "redirect:/study/"+study.getEncodedPath(path)+"/settings/description";
    }

    @GetMapping("/banner")
    public String viewStudyBannerForm(@CurrentUser Account account, @PathVariable String path, Model model){
        Study study = studyService.getStudyToUpdate(account, path);
        model.addAttribute(account);
        model.addAttribute(study);

        return "study/settings/banner";
    }

    @PostMapping("/banner/enable")
    public String enableStudyBanner(@CurrentUser Account account, @PathVariable String path, RedirectAttributes attributes){
        Study study = studyService.getStudyToUpdate(account, path);
        studyService.enableStudyBanner(study);
        attributes.addFlashAttribute("message","배너 이미지를 사용합니다.");
        return "redirect:/study/"+study.getEncodedPath(path)+"/settings/banner";
    }

    @PostMapping("/banner/disable")
    public String disableStudyBanner(@CurrentUser Account account, @PathVariable String path, RedirectAttributes attributes){
        Study study = studyService.getStudyToUpdate(account, path);
        studyService.disableStudyBanner(study);
        attributes.addFlashAttribute("message","배너 이미지를 사용하지 않습니다.");
        return "redirect:/study/"+study.getEncodedPath(path)+"/settings/banner";
    }

    @PostMapping("/banner")
    public String studyImageSubmit(@CurrentUser Account account, @PathVariable String path, Model model, String image, RedirectAttributes attributes){
        Study study = studyService.getStudyToUpdate(account, path);
        studyService.updateStudyImage(image,study);
        attributes.addFlashAttribute("message","스터디 이미지를 성공적으로 수정했습니다.");
        return "redirect:/study/"+study.getEncodedPath(path)+"/settings/banner";
    }

    @GetMapping("/tags")
    public String studyTagsForm(@CurrentUser Account account, Model model, @PathVariable String path) throws JsonProcessingException {
        Study study = studyService.getStudyToUpdate(account,path);
        model.addAttribute(account);
        model.addAttribute(study);

        // 현재 tags정보로 저장되어 있는 값을 리스트로 view에 넘겨준다.
        model.addAttribute("tags",study.getTags().stream().map(Tag::getTitle).collect(Collectors.toList()));

        List<String> allTagTitles = tagRepository.findAll().stream().map(Tag::getTitle).collect(Collectors.toList());
        model.addAttribute("whitelist",objectMapper.writeValueAsString(allTagTitles));
        return "study/settings/tags";
    }

    @PostMapping("/tags/add")
    @ResponseBody
    public ResponseEntity addTag(@CurrentUser Account account, @PathVariable String path, @RequestBody TagForm tagForm){
        //Study study = studyService.getStudyToUpdate(account,path); // DB 쿼리를 생각해볼 필요가 있다.
        // 그냥 우리가 하는 getStudyToUpdate 메서드를 사용하면, 앞서 설정한 @EntityGraph에 의해서 많은 테이블를 EAGER FETCH로 가져오게 된다.
        // study, managers, members, zones, tags 정보를 참조하기 위한 쿼리가 발생하게 된다.
        // 이는 Ajax 요청을 처리하는 로직에는 불필요한 정보들이다.
        // 그래서 이 Ajax 요청 처리에 필요한 정보들만 쿼리하는 @EntityGraph를 새로 설정하여 불필요한 쿼리의 발생을 방지한다.
        Study study = studyService.getStudyToUpdateTag(account,path);
        // 물론 @EntotyGraph를 사용하지 않으면 알아서 필요한 정보에 한해서만 쿼리를 하겠지만, 이는 앞서 살펴본것처럼 여러개의 쿼리를 수행하게 된다.
        // 수행되는 쿼리의 개수를 줄이기 위해서 @EntotyGraph를 사용해서 필요한 정보를 미리 하나의 쿼리로 가져오는 것이다.
        Tag tag = tagService.findOrCreateNew(tagForm.getTagTitle());
        studyService.addTag(study,tag); // JPA 관점에서 study 객체와 tag 객체는 persist 상태인 객체이다.
        return ResponseEntity.ok().build();
    }

    @PostMapping("/tags/remove")
    @ResponseBody
    public ResponseEntity removeTag(@CurrentUser Account account, @PathVariable String path, @RequestBody TagForm tagForm){
        //Study study = studyService.getStudyToUpdate(account,path); // 마찬가지로 쿼리를 생각해볼 필요가 있다.
        Study study = studyService.getStudyToUpdateTag(account,path);
        Tag tag = tagRepository.findByTitle(tagForm.getTagTitle());
        if(tag==null){
            return ResponseEntity.badRequest().build();
        }
        studyService.removeTag(study,tag);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/zones")
    public String studyZonesForm(@CurrentUser Account account, @PathVariable String path, Model model) throws JsonProcessingException {
        Study study = studyService.getStudyToUpdate(account,path);
        model.addAttribute(account);
        model.addAttribute(study);
        model.addAttribute("zones",study.getZones().stream().map(Zone::toString).collect(Collectors.toList()));
        List<String> allZones = zoneRepository.findAll().stream().map(Zone::toString).collect(Collectors.toList());
        model.addAttribute("whitelist",objectMapper.writeValueAsString(allZones));
        return "study/settings/zones";
    }

    @ResponseBody
    @PostMapping("/zones/add")
    public ResponseEntity addZone(@CurrentUser Account account, @PathVariable String path, @RequestBody ZoneForm zoneForm){
        //Study study = studyService.getStudyToUpdate(account,path);
        Study study = studyService.getStudyToUpdateZone(account,path);
        Zone zone  = zoneRepository.findByCityAndProvince(zoneForm.getCityName(),zoneForm.getProvinceName());
        if(zone==null){
            return ResponseEntity.badRequest().build();
        }
        studyService.addZone(study,zone);
        return ResponseEntity.ok().build();
    }

    @ResponseBody
    @PostMapping("/zones/remove")
    public ResponseEntity removeZone(@CurrentUser Account account, @PathVariable String path, @RequestBody ZoneForm zoneForm){
        Study study = studyService.getStudyToUpdateZone(account,path);
        Zone zone = zoneRepository.findByCityAndProvince(zoneForm.getCityName(), zoneForm.getProvinceName());
        if(zone==null){
            return ResponseEntity.badRequest().build();
        }
        studyService.removeZone(study,zone);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/study")
    public String StudySettingForm(@CurrentUser Account account, @PathVariable String path, Model model){
        Study study = studyService.getStudyToUpdate(account,path);
        model.addAttribute(account);
        model.addAttribute(study);
        return "study/settings/study";
    }

    @PostMapping("/study/publish")
    public String publishStudy(@CurrentUser Account account, @PathVariable String path, RedirectAttributes attributes){
        Study study = studyService.getStudyToUpdateStatus(account,path); // 쿼리 개수 조절로 향상 작업
        studyService.publish(study);
        attributes.addFlashAttribute("message","스터디를 공개했습니다.");
        return "redirect:/study/"+study.getEncodedPath(path)+"/settings/study";
    }

    @PostMapping("/study/close")
    public String closeStudy(@CurrentUser Account account, @PathVariable String path, RedirectAttributes attributes){
        Study study = studyService.getStudyToUpdateStatus(account,path);
        studyService.close(study);
        attributes.addFlashAttribute("message","스터디를 성공적으로 종료했습니다.");
        return "redirect:/study/"+study.getEncodedPath(path)+"/settings/study";
    }

    @PostMapping("/recruit/start")
    public String startRecruit(@CurrentUser Account account, @PathVariable String path, RedirectAttributes attributes){
        Study study = studyService.getStudyToUpdateStatus(account,path); // 쿼리 개수 조절로 향상 작업
        if(!study.canUpdateRecruiting()){
            attributes.addFlashAttribute("message","1시간 안에 인원 모집 설정을 여러번 변경할 수 없습니다.");
            return "redirect:/study/"+study.getEncodedPath(path)+"/settings/study"; //여기는 사용자가 발생시키는 에러가 아니라 서버에서 설정하는 서비스상의 제약조건이기 때문에 별도의 에러페이지 사용안함.
            // 단순히 리다이렉트 메세지를 통해서 알려주기만 하면 된다.
        }
        studyService.startRecruit(study);
        attributes.addFlashAttribute("message","인원 모집을 시작합니다.");
        return "redirect:/study/"+study.getEncodedPath(path)+"/settings/study";
    }

    @PostMapping("/recruit/stop")
    public String stopRecruit(@CurrentUser Account account, @PathVariable String path, RedirectAttributes attributes){
        Study study = studyService.getStudyToUpdateStatus(account,path);
        if(!study.canUpdateRecruiting()){
            attributes.addFlashAttribute("message","1시간 안에 인원 모집 설정을 여러번 변경할 수 없습니다.");
            return "redirect:/study/"+study.getEncodedPath(path)+"/settings/study"; // 여기는 사용자가 발생시키는 에러가 아니라 서버에서 설정하는 서비스상의 제약조건이기 때문에 별도의 에러페이지 사용안함.
            // 단순히 리다이렉트 메세지를 통해서 알려주기만 하면 된다.
        }
        studyService.stopRecruit(study);
        attributes.addAttribute("message","인원 모집을 종료합니다.");
        return "redirect:/study/"+study.getEncodedPath(path)+"/settings/study";
    }

    @PostMapping("/study/path")
    public String updateStudyPath(@CurrentUser Account account, @PathVariable String path, @RequestParam String newPath, RedirectAttributes attributes, Model model){
        Study study = studyService.getStudyToUpdateStatus(account, path);
        if(!studyService.isValidPath(newPath)){
            model.addAttribute(account);
            model.addAttribute(study);
            model.addAttribute("studyPathError","해당 스터디 경로는 사용할 수 없습니다. 다른 값을 입력하세요");
            return "study/settings/study";
        }
        studyService.updateStudyPath(study,newPath);
        attributes.addFlashAttribute("message","스터디 경로를 수정했습니다.");
        return "redirect:/study/"+study.getEncodedPath(newPath)+"/settings/study"; // 변경된 스터디 주소로 리다이랙트를 시켜야한다.
    }

    @PostMapping("/study/title")
    public String updateStudyTitle(@CurrentUser Account account, @PathVariable String path, @RequestParam String newTitle, Model model, RedirectAttributes attributes){
        Study study = studyService.getStudyToUpdateStatus(account,path);
        if(!studyService.isValidTitle(newTitle)){ // 스터디 제목이 50글자를 넘기는지 검증
            model.addAttribute(account);
            model.addAttribute(study);
            model.addAttribute("studyTitleError","스터디 이름을 다시 입력하세요.");
            return "study/settings/study";
        }
        studyService.updateStudyTitle(study,newTitle);
        attributes.addFlashAttribute("message","스터디 이름을 수정했습니다.");
        return "redirect:/study/"+study.getEncodedPath(path)+"/settings/study";
    }

    @PostMapping("/study/remove")
    public String removeStudy(@CurrentUser Account account, @PathVariable String path){
        Study study = studyService.getStudyToUpdateStatus(account, path);
        studyService.remove(study);
        return "redirect:/";
    }
}
