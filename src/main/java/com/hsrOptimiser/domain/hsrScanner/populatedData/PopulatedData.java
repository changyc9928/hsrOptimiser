package com.hsrOptimiser.domain.hsrScanner.populatedData;

import java.io.Serializable;
import java.util.HashMap;
import lombok.Data;

@Data
public class PopulatedData implements Serializable {

    HashMap<String, PopulatedCharacter> characters;
    HashMap<String, PopulatedLightCone> lightCones;
    HashMap<String, PopulatedRelic> relics;
}

