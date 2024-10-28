package com.hsrOptimiser.properties;

import com.hsrOptimiser.domain.hsrScanner.Stats;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.HashMap;

@ConfigurationProperties
@Data
public class Properties {
    HashMap<String, HashMap<Integer, ArrayList<ConditionalStatBonus>>> conditionalSetBonus;
    HashMap<String, HashMap<Integer, ArrayList<StatBonus>>> setBonus;
    Formula formula;
}

