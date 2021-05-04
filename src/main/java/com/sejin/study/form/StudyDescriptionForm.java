package com.sejin.study.form;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
public class StudyDescriptionForm { // ModelMapper는 인자가 없는 기본 생성자를 사용해서 객체를 생성하기 때문에 기본 생성자를 반드시 만들어줘야한다.

    @Length(max = 100)
    @NotBlank
    private String shortDescription;

    @NotBlank
    private String fullDescription;
}
