package com.hsrOptimiser.domain.hsrScanner.populatedData;

import com.hsrOptimiser.domain.hsrScanner.Character;
import com.hsrOptimiser.domain.hsrScanner.Stats;
import lombok.Data;

import java.util.HashMap;

@Data
public class PopulatedCharacter extends Character {
    float baseHp;
    float baseAtk;
    float baseDef;
    float baseSpd;
    float baseAggro;
    float criticalChance;
    float criticalDamage;
    HashMap<Stats, Float> statBonus;
}
