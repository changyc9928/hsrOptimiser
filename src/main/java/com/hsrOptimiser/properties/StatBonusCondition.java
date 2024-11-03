package com.hsrOptimiser.properties;

import com.hsrOptimiser.domain.hsrScanner.Stats;
import lombok.Data;

@Data
public class StatBonusCondition {

    Stats stat;
    float value;
}
