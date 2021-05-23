package com.jinstudy.event;

import com.jinstudy.account.CurrentUser;
import com.jinstudy.domain.Account;
import com.jinstudy.domain.Enrollment;
import com.jinstudy.domain.Event;
import com.jinstudy.domain.Study;
import com.jinstudy.event.form.EventForm;
import com.jinstudy.event.validator.EventValidator;
import com.jinstudy.study.StudyService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/study/{path}")
@Controller
public class EventController {

    private final StudyService studyService;
    private final EventService eventService;
    private final ModelMapper modelMapper;
    private final EventValidator eventValidator;
    private final EventRepository eventRepository;

    @InitBinder("eventForm")
    public void initBinder(WebDataBinder webDataBinder){
        webDataBinder.addValidators(eventValidator);
    }

    @GetMapping("/new-event")
    public String newEventForm(@CurrentUser Account account, @PathVariable String path, Model model){
        Study study = studyService.getStudyToUpdateStatus(account, path); // 모임도 결국 스터디 관리자만 생성할 수 있음.
        model.addAttribute(account);
        model.addAttribute(study);
        model.addAttribute(new EventForm());
        return "event/form";
    }

    @PostMapping("/new-event")
    public String newEventSubmit(@CurrentUser Account account, @PathVariable String path, @Valid EventForm eventForm,
                                 Errors errors, Model model){
        Study study = studyService.getStudyToUpdateStatus(account,path);

        if(errors.hasErrors()){ // 추가적으로 날짜에 대한 Validator를 구현해서 사용.
            model.addAttribute(account);
            model.addAttribute(study);
            return "event/form";
        }

        Event event = eventService.createEvent(modelMapper.map(eventForm, Event.class), study, account); // 스터디 정보, 관리자 정보, 폼으로부터 입력받은 정보를 사용한다.
        return "redirect:/study/"+study.getEncodedPath(path)+"/event/"+event.getId();
    }

    @GetMapping("/event/{id}")
    public String getEvent(@CurrentUser Account account, @PathVariable String path, @PathVariable Long id, Model model){

        Study study = studyService.getStudy(path);
        Event event = eventRepository.findById(id).orElseThrow();
        model.addAttribute(account);
        model.addAttribute(study);
        model.addAttribute(event);

        return "event/view";
    }

    @GetMapping("/events")
    public String viewStudyEvents(@CurrentUser Account account, @PathVariable String path, Model model){
        Study study = studyService.getStudy(path); // 스터디에 해당하는 정보를 모두 가져와야하기 때문에 해당 전체 쿼리를 요청하는 메서드가 알맞다.
        model.addAttribute(account);
        model.addAttribute(study);

        List<Event> events = eventRepository.findByStudyOrderByStartDateTime(study);
        List<Event> newEvents = new ArrayList<>();
        List<Event> oldEvents = new ArrayList<>();
        events.stream().forEach(e -> {
            if(e.getEndDateTime().isBefore(LocalDateTime.now())){
                oldEvents.add(e);
            }
            else{
                newEvents.add(e);
            }
        });
        model.addAttribute("newEvents",newEvents);
        model.addAttribute("oldEvents",oldEvents);
        return "study/events";
    }

    @GetMapping("/event/{id}/edit")
    public String updateEventForm(@CurrentUser Account account, @PathVariable String path, @PathVariable Long id, Model model){
        Study study = studyService.getStudyToUpdate(account,path);
        Event event = eventRepository.findById(id).orElseThrow();
        model.addAttribute(account);
        model.addAttribute(study);
        model.addAttribute(event);
        model.addAttribute(modelMapper.map(event,EventForm.class));
        return "event/update-form"; // 수정하는 폼은
    }

    @PostMapping("/event/{id}/edit")
    public String updateEventSubmit(@CurrentUser Account account, @PathVariable String path, @PathVariable Long id, @Valid EventForm eventForm, Errors errors, Model model){
        Study study = studyService.getStudyToUpdate(account, path);
        Event event = eventRepository.findById(id).orElseThrow();

        // 뷰에서 모집 방법을 설정하지 못하게 설정했지만, 악의적으로 요청을 보낼 수도 있기 때문에 이에대한 처리를 해줘야함!
        eventForm.setEventType(event.getEventType());

        // 그리고 추가적인 Validator 설정을 통해서 수정시에 필요한 검증을 별도로 해준다.
        eventValidator.validateUpdateForm(eventForm,event,errors);

        if(errors.hasErrors()){
            model.addAttribute(account);
            model.addAttribute(study);
            model.addAttribute(event);
            return "event/update-form";
        }

        eventService.updateEvent(event,eventForm);

        // 모임을 수정하고나면 해당 모임으로 리다이랙트를 시켜준다.
        return "redirect:/study/" + study.getEncodedPath(path) + "/event/" + event.getId();
    }

    @DeleteMapping("/event/{id}")
    public String cancelEvent(@CurrentUser Account account, @PathVariable String path, @PathVariable Long id){
        Study study = studyService.getStudyToUpdateStatus(account,path);
        Event event = eventRepository.findById(id).orElseThrow();
        eventService.deleteEvent(event);
        return "redirect:/study/" + study.getEncodedPath(path) + "/events";
    }

    @PostMapping("/event/{id}/enroll")
    public String newEnrollment(@CurrentUser Account account, @PathVariable String path, @PathVariable Long id){
        // 관리자가 아닌 사람들도 모임 신청을 사용할 수 있어야한다.
        Study study = studyService.getStudyToEnroll(path);
        eventService.newEnrollment(eventRepository.findById(id).orElseThrow(),account); // 새로운 참여를 생성
        return "redirect:/study/" + study.getEncodedPath(path) + "/event/" + id;
    }

    @PostMapping("/event/{id}/disenroll")
    public String cancelEnrollment(@CurrentUser Account account, @PathVariable String path, @PathVariable Long id){
        // 관리자가 아닌 사람들도 모임 신청을 취소할 수 있어야한다.
        Study study = studyService.getStudyToEnroll(path);
        Event event = eventRepository.findById(id).orElseThrow();
        eventService.cancelEnrollment(event, account); // 모임 참여를 취소
        return "redirect:/study/" + study.getEncodedPath(path) + "/event/" + id;
    }

    @GetMapping("/event/{eventId}/enrollments/{enrollmentId}/accept")
    public String acceptEnrollment(@CurrentUser Account account, @PathVariable String path, @PathVariable("eventId") Event event, @PathVariable("enrollmentId") Enrollment enrollment){
        // 관리자 확정 모임의 경우에 관리자가 사용자에 대해서 확정을 해주는 요청
        Study study = studyService.getStudyToUpdateStatus(account,path);
        eventService.acceptEnrollment(event,enrollment);
        return "redirect:/study/"+study.getEncodedPath(path)+"/event/"+event.getId();
    }

    @GetMapping("/event/{eventId}/enrollments/{enrollmentId}/reject")
    public String rejectEnrollment(@CurrentUser Account account, @PathVariable String path, @PathVariable("eventId") Event event, @PathVariable("enrollmentId") Enrollment enrollment){
        // 관리자 확정 모임의 경우에 관리자가 사용자에 대해서 확정을 취소하는 요청
        Study study = studyService.getStudyToUpdateStatus(account,path);
        eventService.rejectEnrollment(event,enrollment);
        return "redirect:/study/"+study.getEncodedPath(path)+"/event/"+event.getId();
    }

    @GetMapping("/event/{eventId}/enrollments/{enrollmentId}/checkin")
    public String checkInEnrollment(@CurrentUser Account account, @PathVariable String path, @PathVariable("eventId") Event event, @PathVariable("enrollmentId") Enrollment enrollment){
        // 관리자의 체크인
        Study study = studyService.getStudyToUpdateStatus(account,path);
        eventService.checkInEnrollment(enrollment);
        return "redirect:/study/"+study.getEncodedPath(path)+"/event/"+event.getId();
    }

    @GetMapping("/event/{eventId}/enrollments/{enrollmentId}/cancel-checkin")
    public String cancelCheckInEnrollment(@CurrentUser Account account, @PathVariable String path, @PathVariable("eventId") Event event, @PathVariable("enrollmentId") Enrollment enrollment){
        Study study = studyService.getStudyToUpdateStatus(account,path);
        eventService.cancelCheckInEnrollment(enrollment);
        return "redirect:/study/"+study.getEncodedPath(path)+"/event/"+event.getId();
    }
}