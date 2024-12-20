package com.hsrOptimiser.services;

import com.hsrOptimiser.domain.CharacterStats;
import com.hsrOptimiser.domain.EnemySetup;
import com.hsrOptimiser.domain.hsrScanner.populatedData.PopulatedData;
import java.util.HashMap;

public interface EvaluationService {

    CharacterStats evaluate(PopulatedData populatedData, String characterId,
        EnemySetup enemySetup, HashMap<String, Float> otherBonuses, String targetName)
        throws Exception;
}
