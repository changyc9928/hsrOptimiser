package com.hsrOptimiser.DTO;

import com.hsrOptimiser.DTO.hsrScanner.populatedData.PopulatedCharacter;
import com.hsrOptimiser.DTO.hsrScanner.populatedData.PopulatedLightCone;
import com.hsrOptimiser.DTO.hsrScanner.populatedData.PopulatedRelic;
import java.util.ArrayList;
import java.util.HashMap;
import lombok.Data;

@Data
public class CharacterStats {

    PopulatedCharacter character;
    PopulatedLightCone lightCone;
    ArrayList<PopulatedRelic> relics;

    HashMap<String, Double> stats;

    float optimizationTargetValue;
}
