package com.hsrOptimiser.services;

import com.hsrOptimiser.DTO.EvaluationResult;
import java.util.List;

public interface EvaluationService {

    EvaluationResult evaluateAsagi(String userId, List<String> characterIds,
        List<String> fixedCharacterIds,
        List<String> allowedToScrapRelicsCharacterIds,
        List<String> disallowedToScrapRelicsCharacterIds);
}
