package com.hsrOptimiser.domain;

import com.hsrOptimiser.domain.hsrScanner.populatedData.PopulatedCharacter;
import com.hsrOptimiser.domain.hsrScanner.populatedData.PopulatedLightCone;
import com.hsrOptimiser.domain.hsrScanner.populatedData.PopulatedRelic;
import java.util.ArrayList;
import java.util.HashMap;
import lombok.Data;

@Data
public class CharacterStats {

    PopulatedCharacter character;
    PopulatedLightCone lightCone;
    ArrayList<PopulatedRelic> relics;

    HashMap<String, Float> stats;

    float optimizationTargetValue;
}
