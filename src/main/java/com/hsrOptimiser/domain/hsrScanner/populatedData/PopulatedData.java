package com.hsrOptimiser.domain.hsrScanner.populatedData;

import java.util.ArrayList;
import lombok.Data;

@Data
public class PopulatedData {

    ArrayList<PopulatedCharacter> characters;
    ArrayList<PopulatedLightCone> lightCones;
    ArrayList<PopulatedRelic> relics;
}

