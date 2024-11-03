package com.hsrOptimiser.properties;

import java.util.HashMap;
import lombok.Data;

@Data
public class Character {

    HashMap<String, OptimizationTarget> optimizationTarget;
    HashMap<String, ScaleMapping> scaleMapping;
    HashMap<String, Trace> trace;
}

