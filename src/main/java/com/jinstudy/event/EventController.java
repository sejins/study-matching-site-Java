package com.jinstudy.event;

import com.jinstudy.account.CurrentUser;
import com.jinstudy.domain.Account;
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
}
