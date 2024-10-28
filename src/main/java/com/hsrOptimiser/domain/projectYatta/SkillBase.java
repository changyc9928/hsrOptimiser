package com.hsrOptimiser.domain.projectYatta;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

import java.io.Serializable;

@Data
public class SkillBase implements Serializable {
    float attackBase;
    float defenceBase;
    @JsonAlias("hPBase")
    float hPBase;
    int speedBase;
    int baseAggro;
    float criticalChance;
    float criticalDamage;
}