package com.hsrOptimiser.DTO;

import com.hsrOptimiser.DTO.hsrScanner.Slot;
import com.hsrOptimiser.DTO.hsrScanner.SubStats;
import java.util.List;
import lombok.Data;

@Data
public class CreateRelicRequest {

    private String setId;
    private String name;
    private Slot slot;
    private Integer rarity;
    private Integer level;
    private String mainstat;
    private List<SubStats> substats;
    private String location;
    private Boolean lock;
    private String uid;
}
