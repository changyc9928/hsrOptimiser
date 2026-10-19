package com.hsrOptimiser.DTO.hsrScanner.populatedData;

import com.hsrOptimiser.DTO.hsrScanner.HSRCharacter;
import java.io.Serializable;
import java.util.HashMap;
import lombok.Data;

@Data
public class PopulatedCharacter extends HSRCharacter implements Serializable {

    float baseHp;
    float baseAtk;
    float baseDef;
    float baseSpd;
    float baseAggro;
    float criticalChance;
    float criticalDamage;
    HashMap<String, Double> statBonus;
}
