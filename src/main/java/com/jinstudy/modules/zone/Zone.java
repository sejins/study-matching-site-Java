package com.jinstudy.modules.zone;

import lombok.*;

import javax.persistence.*;

@Entity
@Setter @Getter @EqualsAndHashCode(of = "id")
@Builder @AllArgsConstructor @NoArgsConstructor
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"city", "province"}))
public class Zone {

    @Id @GeneratedValue
    private Long id;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private String localNameOfCity;

    @Column(nullable = true) // 도 정보같은 경우에는 존재하지 않을 수도 있다.
    private String province;

    @Override
    public String toString(){return String.format("%s(%s)/%s",city, localNameOfCity, province);}
}
