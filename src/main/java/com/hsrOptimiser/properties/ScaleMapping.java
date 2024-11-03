package com.hsrOptimiser.properties;

import java.util.HashMap;
import lombok.Data;

@Data
public class ScaleMapping {

    String key;
    HashMap<String, String> scale;
}
