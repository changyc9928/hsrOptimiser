package com.hsrOptimiser.properties;

import lombok.Data;

import java.util.ArrayList;

@Data
public class OptimizationTarget {
    String formula;
    ArrayList<Dropdown> dropdown;
}

