package com.hsrOptimiser.properties;

import com.hsrOptimiser.domain.hsrScanner.Stats;
import lombok.Data;

@Data
public class ConditionalStatBonus {

    String stat;
    float value;
    int maxStack;
    String description;
}