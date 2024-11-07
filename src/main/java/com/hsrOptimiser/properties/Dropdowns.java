package com.hsrOptimiser.properties;

import java.util.HashMap;
import lombok.Data;

@Data
public class Dropdowns {

    HashMap<String, Dropdown> common;
    HashMap<String, CharacterDropdown> character;
}

