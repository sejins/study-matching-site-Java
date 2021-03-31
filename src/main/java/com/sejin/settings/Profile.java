package com.sejin.settings;

import com.sejin.domain.Account;
import lombok.Data;

@Data
public class Profile {
    // profile 수정 뷰에 넘겨줄 폼 클래스

    private String bio;

    private String url;

    private String occupation;

    private String location;

    public Profile(Account account){
        this.bio = account.getBio();
        this.url = account.getUrl();
        this.occupation = account.getOccupation();
        this.location = account.getLocation();
    }

}
