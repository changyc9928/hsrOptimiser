package com.hsrOptimiser.services;

import com.hsrOptimiser.DTO.CharacterDamage;
import com.hsrOptimiser.DTO.EvaluationResult;
import com.hsrOptimiser.DTO.asagi.TItem;
import com.hsrOptimiser.DTO.hsrScanner.HSRCharacter;
import com.hsrOptimiser.DTO.hsrScanner.Relic;
import com.hsrOptimiser.DTO.hsrScanner.ScannedData;
import com.hsrOptimiser.client.AsagiClient;
import com.hsrOptimiser.clientConfig.AsagiCharacterMetadata;
import com.hsrOptimiser.engine.SimulatedAnnealing;
import com.hsrOptimiser.engine.SimulatedAnnealing.SimulationResult;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EvaluationServiceImpl implements EvaluationService {

    @Autowired
    Memory memory;

    @Autowired
    AsagiClient asagiClient;

    @Autowired
    SimulatedAnnealing simulatedAnnealing;

    @Override
    public EvaluationResult evaluateAsagi(String userId, List<String> characterIds,
        List<String> fixedCharacterIds,
        List<String> allowedToScrapRelicsCharacterIds,
        List<String> disallowedToScrapRelicsCharacterIds) {
        ScannedData scannedData = memory.getMemory(userId);
        SimulationResult simulationResult = simulatedAnnealing.simulateAnnealing(scannedData,
            asagiClient, characterIds, fixedCharacterIds, allowedToScrapRelicsCharacterIds,
            disallowedToScrapRelicsCharacterIds);
        EvaluationResult evaluationResult = new EvaluationResult();
        evaluationResult.setTotalDamage(
            simulationResult.mocResponse().getT().stream().map(TItem::getTotal)
                .reduce(Double::sum).orElse(0D));
        List<CharacterDamage> characterDamages = new ArrayList<>();
        for (String characterId : characterIds) {
            CharacterDamage characterDamage = new CharacterDamage();
            List<Relic> relics = simulationResult.data().getRelics().stream()
                .filter(relic -> relic.getLocation().equals(characterId)).toList();
            characterDamage.setRelics(relics);
            HSRCharacter hsrCharacter = scannedData.getCharacters().stream()
                .filter(hsrCharacter1 -> hsrCharacter1.getId().equals(characterId)).findFirst()
                .orElseThrow();
            AsagiCharacterMetadata characterInfoDTO = AsagiCharacterMetadata.getInfoById(
                characterId,
                hsrCharacter.getAbilityVersion());
            characterDamage.setTotalDamage(simulationResult.mocResponse().getT().stream()
                .filter(tItem -> tItem.getName().equals(characterInfoDTO.getDisplayName()))
                .map(TItem::getTotal)
                .findFirst().orElse(0D));
            characterDamage.setName(characterInfoDTO.getDisplayName());
            characterDamages.add(characterDamage);
        }
        evaluationResult.setCharacterDamage(characterDamages);
        return evaluationResult;
    }
}
