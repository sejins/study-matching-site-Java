package com.jinstudy.event;

import com.jinstudy.domain.Account;
import com.jinstudy.domain.Enrollment;
import com.jinstudy.domain.Event;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    boolean existsByEventAndAccount(Event event, Account account);

    Enrollment findByEventAndAccount(Event event, Account account);
}
