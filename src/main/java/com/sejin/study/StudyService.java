package com.sejin.study;

import com.sejin.domain.Account;
import com.sejin.domain.Study;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@RequiredArgsConstructor
@Service
@Transactional
public class StudyService {

    private final StudyRepository studyRepository;

    public Study createNewStudy(Account account, Study study) {

        Study newStudy = studyRepository.save(study);
        newStudy.addManager(account);
        return newStudy;

    }
}
