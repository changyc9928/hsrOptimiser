package com.hsrOptimiser.domain.hsrScanner.populatedData;

import com.hsrOptimiser.domain.hsrScanner.LightCone;
import java.io.Serializable;
import lombok.Data;

@Data
public class PopulatedLightCone extends LightCone implements Serializable {

    float baseHp;
    float baseAtk;
    float baseDef;
}
