package com.hsrOptimiser.properties;

import java.util.HashMap;
import lombok.Data;

@Data
public class Formula {

    HashMap<String, String> baseStat;
    HashMap<String, CharacterFormula> character;
}

