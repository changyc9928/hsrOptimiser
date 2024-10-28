package com.hsrOptimiser.domain.hsrScanner.populatedData;

import lombok.Data;

import java.util.ArrayList;

@Data
public class PopulatedData {
    ArrayList<PopulatedCharacter> characters;
    ArrayList<PopulatedLightCone> lightCones;
    ArrayList<PopulatedRelic> relics;
}

