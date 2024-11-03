package com.hsrOptimiser.properties;

import com.hsrOptimiser.domain.hsrScanner.Stats;
import lombok.Data;

@Data
public class StatBonus {

    Stats stat;
    float value;
    StatBonusCondition condition;
}
