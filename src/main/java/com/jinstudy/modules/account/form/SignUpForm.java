package com.jinstudy.modules.account.form;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Data // 롬복 어노테이션
public class SignUpForm {

    //JSR303 애노테이션
    
    @NotBlank  // 비어있는 값이면 안된다.
    @Length(min = 3, max = 20)
    @Pattern(regexp = "^[ㄱ-ㅎ가-힣a-z0-9_-]{3,20}$") // 패턴지정
    private String nickname;

    @Email // 이메일 형식의 값
    @NotBlank
    private String email;

    @NotBlank
    @Length(min = 8, max = 50)
    private String password;
}
