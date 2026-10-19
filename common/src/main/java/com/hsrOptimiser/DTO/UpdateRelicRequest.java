package com.hsrOptimiser.DTO;

import com.hsrOptimiser.DTO.hsrScanner.SubStats;
import java.util.List;
import lombok.Data;

@Data
public class UpdateRelicRequest {

    private String name;
    private Integer level;
    private String mainstat;
    private List<SubStats> substats;
    private Boolean lock;
    private Boolean discard;
}
