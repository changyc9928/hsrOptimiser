package com.hsrOptimiser.domain.hsrScanner.populatedData;

import com.hsrOptimiser.domain.hsrScanner.Character;
import java.io.Serializable;
import java.util.HashMap;
import lombok.Data;

@Data
public class PopulatedCharacter extends Character implements Serializable {

    float baseHp;
    float baseAtk;
    float baseDef;
    float baseSpd;
    float baseAggro;
    float criticalChance;
    float criticalDamage;
    HashMap<String, Float> statBonus;
}
