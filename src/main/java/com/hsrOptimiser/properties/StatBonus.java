package com.hsrOptimiser.properties;

import java.util.ArrayList;
import lombok.Data;

@Data
public class StatBonus {

    String stat;
    float value;
    ArrayList<StatBonusCondition> condition;
}
