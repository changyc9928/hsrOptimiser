package com.hsrOptimiser.properties;

import java.util.ArrayList;
import lombok.Data;

@Data
public class OptimizationTarget {

    String formula;
    ArrayList<String> dropdownUsed;
}

