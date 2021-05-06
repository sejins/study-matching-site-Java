package com.sejin.zone;

import com.sejin.domain.Zone;
import lombok.Data;

@Data
public class ZoneForm {

    private String zoneName;

    public String getCityName(){ return zoneName.substring(0,zoneName.indexOf("(")); }

    public String getLocalNameOfCity(){ return zoneName.substring(zoneName.indexOf("(")+1, zoneName.indexOf(")")); }

    public String getProvinceName(){ return zoneName.substring(zoneName.indexOf("/")+1); }

    public Zone getZone(){ // 이건 왜 만든거지??
        return Zone.builder().city(getCityName())
                .localNameOfCity(getLocalNameOfCity())
                .province(getProvinceName()).build();
    }
}
