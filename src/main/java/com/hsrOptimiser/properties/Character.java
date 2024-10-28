package com.hsrOptimiser.properties;

import lombok.Data;

import java.util.HashMap;

@Data
public class Character {
    HashMap<String, OptimizationTarget> optimizationTarget;
}

