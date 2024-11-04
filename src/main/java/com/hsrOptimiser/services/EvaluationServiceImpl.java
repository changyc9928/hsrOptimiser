package com.hsrOptimiser.services;

import com.hsrOptimiser.domain.CharacterStats;
import com.hsrOptimiser.domain.EnemySetup;
import com.hsrOptimiser.domain.hsrScanner.populatedData.PopulatedCharacter;
import com.hsrOptimiser.domain.hsrScanner.populatedData.PopulatedData;
import com.hsrOptimiser.domain.hsrScanner.populatedData.PopulatedLightCone;
import com.hsrOptimiser.domain.hsrScanner.populatedData.PopulatedRelic;
import com.hsrOptimiser.engine.Evaluator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EvaluationServiceImpl implements EvaluationService {

    @Autowired
    Evaluator evaluator;

    @Override
    public CharacterStats getCharacterStats(PopulatedData populatedData, String characterId,
        EnemySetup enemySetup, HashMap<String, Float> otherBonuses) {
        PopulatedCharacter character = populatedData.getCharacters()
            .get(characterId);
        Optional<PopulatedLightCone> lightCone = populatedData.getLightCones().values().stream()
            .filter(populatedLightCone -> {
                String location = populatedLightCone.getLocation();
                if (location != null) {
                    return location.equals(characterId);
                } else {
                    return false;
                }
            })
            .toList()
            .stream().findFirst();
        evaluator.setCharacter(character);
        lightCone.ifPresent(populatedLightCone -> evaluator.setLightCone(populatedLightCone));
        evaluator.setEnemySetup(enemySetup);

        ArrayList<PopulatedRelic> relics = populatedData.getRelics()
            .values().stream()
            .filter(populatedRelic -> {
                String location = populatedRelic.getLocation();
                if (location != null) {
                    return location.equals(characterId);
                } else {
                    return false;
                }
            }).collect(
                Collectors.toCollection(ArrayList::new));
        return evaluator.getStatDetails(relics, otherBonuses);
    }
}
