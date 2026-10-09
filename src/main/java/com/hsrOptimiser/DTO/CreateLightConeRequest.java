package com.hsrOptimiser.DTO;

import lombok.Data;

@Data
public class CreateLightConeRequest {

    private String id;
    private String name;
    private Integer level;
    private Integer ascension;
    private Integer superimposition;
    private String location;
    private Boolean lock;
    private String uid;
}
