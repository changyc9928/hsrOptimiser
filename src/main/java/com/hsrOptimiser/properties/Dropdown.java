package com.hsrOptimiser.properties;

import java.util.ArrayList;
import java.util.HashMap;
import lombok.Data;

@Data
public class Dropdown {

    String title;
    ArrayList<String> stats;
    ArrayList<String> placeholders;
    HashMap<String, ArrayList<String>> scale;
}
