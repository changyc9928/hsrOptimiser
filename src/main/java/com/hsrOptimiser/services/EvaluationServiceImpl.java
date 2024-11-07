package com.hsrOptimiser.services;

import com.hsrOptimiser.domain.CharacterStats;
import com.hsrOptimiser.domain.EnemySetup;
import com.hsrOptimiser.domain.hsrScanner.populatedData.PopulatedCharacter;
import com.hsrOptimiser.domain.hsrScanner.populatedData.PopulatedData;
import com.hsrOptimiser.domain.hsrScanner.populatedData.PopulatedLightCone;
import com.hsrOptimiser.domain.hsrScanner.populatedData.PopulatedRelic;
import com.hsrOptimiser.engine.Evaluator;
import com.hsrOptimiser.properties.Properties;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EvaluationServiceImpl implements EvaluationService {

    Evaluator evaluator;
    Properties properties;

    @Override
    public CharacterStats evaluate(PopulatedData populatedData, String characterId,
        EnemySetup enemySetup, HashMap<String, Float> otherBonuses, String targetName)
        throws Exception {
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
        lightCone.ifPresent(evaluator::setLightCone);
        evaluator.setEnemySetup(enemySetup);
        if (targetName != null) {
            if (properties.getFormula().getBaseStat().containsKey(targetName)) {
                evaluator.setTargetFormula(properties.getFormula().getBaseStat().get(targetName));
            } else if (properties.getFormula().getCharacter().get(characterId)
                .getOptimizationTarget().containsKey(targetName)) {
                evaluator.setTargetFormula(
                    properties.getFormula().getCharacter().get(characterId).getOptimizationTarget()
                        .get(targetName).getFormula());
            } else {
                throw new NoSuchFieldException(
                    String.format("Target formula for %s not found", targetName));
            }
        }
        ;

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
