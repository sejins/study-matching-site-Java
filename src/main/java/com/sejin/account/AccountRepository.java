package com.sejin.account;

import com.sejin.domain.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;


@Transactional(readOnly = true)
public interface AccountRepository extends JpaRepository<Account, Long> {
    boolean existsByEmail(String email);

    boolean existsByNickname(String nickname);

    Account findByEmail(String email);

    Account findByNickname(String emailOrNickname);
}

// 여기는 JPA 안들으니까 모르겠다...
// existsByEmail, existsByNickname 구현은 누가 어떻게 해서 인식하는건데....(의존성 주입해줄때 알아서 만들어주냐..?)
// 이 repository 인터페이스에 대해서 구글링 해보자!!!