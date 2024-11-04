package com.hsrOptimiser.properties;

import lombok.Data;

@Data
public class StatBonus {

    String stat;
    float value;
    StatBonusCondition condition;
}
