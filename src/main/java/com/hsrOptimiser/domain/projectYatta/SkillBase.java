package com.hsrOptimiser.domain.projectYatta;

import com.fasterxml.jackson.annotation.JsonAlias;
import java.io.Serializable;
import lombok.Data;

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
