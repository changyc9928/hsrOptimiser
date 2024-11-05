package com.hsrOptimiser.controller;

import com.hsrOptimiser.domain.CharacterStats;
import com.hsrOptimiser.domain.NewEvaluatorRequest;
import com.hsrOptimiser.domain.hsrScanner.populatedData.PopulatedData;
import com.hsrOptimiser.services.EvaluationService;
import jakarta.servlet.http.HttpSession;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/optimize")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OptimizerController {

    EvaluationService evaluationService;

    @PostMapping
    public CharacterStats evaluate(HttpSession httpSession,
        @RequestBody NewEvaluatorRequest newEvaluatorRequest) {
        PopulatedData populatedData = (PopulatedData) httpSession.getAttribute("populated_data");
        return evaluationService.getCharacterStats(
            populatedData,
            newEvaluatorRequest.getCharacterId(), newEvaluatorRequest.getEnemySetup(),
            newEvaluatorRequest.getOtherBonuses());
    }
}
