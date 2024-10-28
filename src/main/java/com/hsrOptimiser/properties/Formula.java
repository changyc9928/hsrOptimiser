package com.hsrOptimiser.properties;

import lombok.Data;

import java.util.HashMap;

@Data
public class Formula {
    HashMap<String, String> baseStat;
    HashMap<String, Character> character;
}

