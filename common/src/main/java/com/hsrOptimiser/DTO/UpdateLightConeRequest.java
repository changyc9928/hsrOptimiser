package com.hsrOptimiser.DTO;

import lombok.Data;

@Data
public class UpdateLightConeRequest {

    private String id;
    private String name;
    private Integer level;
    private Integer ascension;
    private Integer superimposition;
    private Boolean lock;
}
