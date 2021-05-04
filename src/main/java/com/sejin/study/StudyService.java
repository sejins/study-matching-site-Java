package com.sejin.study;

import com.sejin.domain.Account;
import com.sejin.domain.Study;
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
}
