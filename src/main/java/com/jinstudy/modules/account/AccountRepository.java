package com.jinstudy.modules.account;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;


@Transactional(readOnly = true) // JpaRepository는 알아서 트렌젝션 처리가 되지만,  AccountRepository 내부의 내가 만든 메서드 때문에 Transactional 처리를 해 줘야한다.
public interface AccountRepository extends JpaRepository<Account, Long> {
    boolean existsByEmail(String email);

    boolean existsByNickname(String nickname);

    Account findByEmail(String email);

    Account findByNickname(String emailOrNickname);
}

// 여기는 JPA 안들으니까 모르겠다...
// existsByEmail, existsByNickname 구현은 누가 어떻게 해서 인식하는건데....(의존성 주입해줄때 알아서 만들어주냐..?)
// 이 repository 인터페이스에 대해서 구글링 해보자!!!