package com.hsrOptimiser.properties;

import java.util.ArrayList;
import java.util.HashMap;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties
@Data
public class Properties {

    HashMap<String, HashMap<Integer, ArrayList<ConditionalStatBonus>>> conditionalSetBonus;
    HashMap<String, HashMap<Integer, ArrayList<StatBonus>>> setBonus;
    HashMap<String, String> traceTitleMapper;
    HashMap<String, String> relicStatMapper;
    HashMap<Integer, HashMap<String, RarityStat>> rarityStats;
    Formula formula;
}

