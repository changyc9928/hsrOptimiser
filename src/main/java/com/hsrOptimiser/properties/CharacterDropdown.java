package com.hsrOptimiser.properties;

import java.util.HashMap;
import lombok.Data;

@Data
public class CharacterDropdown {

    HashMap<String, Dropdown> skill;
    TracesDropdown trace;
}