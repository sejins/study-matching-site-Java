package com.sejin.study;

import com.sejin.domain.Account;
import com.sejin.domain.Study;
import com.sejin.domain.Tag;
import com.sejin.domain.Zone;
import com.sejin.study.form.StudyDescriptionForm;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@RequiredArgsConstructor
@Service
@Transactional
public class StudyService {

    private final StudyRepository studyRepository;
    private final ModelMapper modelMapper;

    public Study createNewStudy(Account account, Study study) {

        Study newStudy = studyRepository.save(study);
        newStudy.addManager(account);
        return newStudy;

    }

    public Study getStudyToUpdate(Account account, String path) {
        Study study = getStudy(path);
        if(!account.isManagerOf(study)){ // 관리자로 등록이 되어있지 않은 경우
            throw new AccessDeniedException("해당 기능을 사용할 수 없습니다.");
        }
        return study;
    }

    public Study getStudy(String path) {
        Study study = studyRepository.findByPath(path);

        if(study==null){
            throw new IllegalArgumentException(path + "에 해당하는 스터디가 없습니다.");
        }

        return study;
    }

    public void updateStudyDescription(Study study, StudyDescriptionForm studyDescriptionForm) {
        modelMapper.map(studyDescriptionForm, Study.class);
    }

    public void enableStudyBanner(Study study) {
        study.setUseBanner(true);
    }

    public void disableStudyBanner(Study study) {
        study.setUseBanner(false);
    }

    public void updateStudyImage(String image, Study study) {
        study.setImage(image);
    }

    public void addTag(Study study, Tag tag) {
        study.getTags().add(tag); // 두 객체 모두 persist 상태의 객체이므로 그냥 객체의 관점에서 변경을 해줘도 나중에 commit 할때 디비에 변경사항이 반영이된다.
    }

    public void removeTag(Study study, Tag tag) {
        study.getTags().remove(tag);
    }

    public void addZone(Study study, Zone zone) {
        study.getZones().add(zone);
    }

    public void removeZone(Study study, Zone zone) {
        study.getZones().remove(zone);
    }

    public Study getStudyToUpdateTag(Account account, String path) {
        Study study = studyRepository.findStudyWithTagByPath(path);
        checkIfExistingStudy(path,study);
        checkIfManager(account,study);
        return study;
    }

    public Study getStudyToUpdateZone(Account account, String path) {
        Study study = studyRepository.findStudyWithZoneByPath(path);
        checkIfExistingStudy(path,study);
        checkIfManager(account,study);
        return study;
    }

    private void checkIfManager(Account account, Study study) {
        if(!account.isManagerOf(study)){
            throw new AccessDeniedException("해당 기능을 사용할 수 없습니다.");
        }
    }

    private void checkIfExistingStudy(String path, Study study) {
        if(study==null){
            throw new IllegalArgumentException(path="에 해당하는 스터디가 없습니다.");
        }
    }


    public Study getStudyToUpdateStatus(Account account, String path) {
        Study study = studyRepository.findStudyWithManagersByPath(path);
        checkIfExistingStudy(path,study);
        checkIfManager(account,study);
        return study;
    }

    public void publish(Study study) {
        study.publish(); // 잘 보도록 하자.
    }

    public void close(Study study) {
        study.close();
    }

    public void startRecruit(Study study) {
        study.startRecruit();
    }

    public void stopRecruit(Study study) {
        study.stopRecruit();
    }
}
