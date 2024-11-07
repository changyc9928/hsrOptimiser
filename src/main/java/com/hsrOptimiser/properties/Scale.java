package com.hsrOptimiser.properties;

import java.util.HashMap;
import lombok.Data;

@Data
public class Scale {

    String stat;
    HashMap<String, Float> scale;
}
