package com.hsrOptimiser.controller;

import com.hsrOptimiser.domain.CharacterStats;
import com.hsrOptimiser.domain.NewEvaluatorRequest;
import com.hsrOptimiser.domain.hsrScanner.populatedData.PopulatedData;
import com.hsrOptimiser.services.EvaluationService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/optimize")
public class OptimizerController {

    @Autowired
    EvaluationService evaluationService;

    @PostMapping
    public CharacterStats newEvaluator(HttpSession httpSession,
        @RequestBody NewEvaluatorRequest newEvaluatorRequest) {
        PopulatedData populatedData = (PopulatedData) httpSession.getAttribute("populated_data");
        return evaluationService.getCharacterStats(
            populatedData,
            newEvaluatorRequest.getCharacterId(), newEvaluatorRequest.getEnemySetup(),
            newEvaluatorRequest.getOtherBonuses());

    }
}
