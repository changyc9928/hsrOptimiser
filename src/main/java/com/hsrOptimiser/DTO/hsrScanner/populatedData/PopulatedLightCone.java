package com.hsrOptimiser.DTO.hsrScanner.populatedData;

import com.hsrOptimiser.DTO.hsrScanner.LightCone;
import java.io.Serializable;
import lombok.Data;

@Data
public class PopulatedLightCone extends LightCone implements Serializable {

    float baseHp;
    float baseAtk;
    float baseDef;
}
