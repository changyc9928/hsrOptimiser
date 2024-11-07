package com.hsrOptimiser.properties;

import java.util.HashMap;
import lombok.Data;

@Data
public class CharacterFormula {

    HashMap<String, OptimizationTarget> optimizationTarget;
}

