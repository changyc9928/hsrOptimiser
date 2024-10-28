package com.hsrOptimiser.properties;

import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;

@Data
public class Dropdown {
    String title;
    ArrayList<String> stats;
    HashMap<String, ArrayList<String>> scale;
}
