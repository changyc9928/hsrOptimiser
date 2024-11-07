package com.hsrOptimiser.properties;

import java.util.ArrayList;
import lombok.Data;

@Data
public class CharacterScale {

    ArrayList<Scale> basic;
    ArrayList<Scale> skill;
    ArrayList<Scale> ult;
    ArrayList<Scale> talent;
    ArrayList<Scale> ability1;
    ArrayList<Scale> ability2;
    ArrayList<Scale> ability3;
}

