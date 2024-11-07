package com.hsrOptimiser.controller;

import com.hsrOptimiser.domain.CharacterStats;
import com.hsrOptimiser.domain.EnemySetup;
import com.hsrOptimiser.domain.EvaluateRequest;
import com.hsrOptimiser.domain.hsrScanner.populatedData.PopulatedData;
import com.hsrOptimiser.services.EvaluationService;
import jakarta.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.HashMap;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/evaluate")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EvaluateController {

    EvaluationService evaluationService;

    @PostMapping("/base-stats")
    public CharacterStats getBaseStats(HttpSession httpSession,
        @RequestBody EvaluateRequest evaluateRequest) throws Exception {
        PopulatedData populatedData = (PopulatedData) httpSession.getAttribute("populated_data");
        if (evaluateRequest.getOtherBonuses() == null) {
            evaluateRequest.setOtherBonuses(new HashMap<>());
        }
        if (evaluateRequest.getEnemySetup() == null) {
            evaluateRequest.setEnemySetup(new EnemySetup());
        }
        return evaluationService.evaluate(
            populatedData,
            evaluateRequest.getCharacterId(), evaluateRequest.getEnemySetup(),
            evaluateRequest.getOtherBonuses(), null);
    }

    @PostMapping
    public CharacterStats evaluate(HttpSession httpSession,
        @RequestBody EvaluateRequest evaluateRequest) throws Exception {
        PopulatedData populatedData = (PopulatedData) httpSession.getAttribute("populated_data");
        if (evaluateRequest.getOtherBonuses() == null) {
            evaluateRequest.setOtherBonuses(new HashMap<>());
        }
        if (evaluateRequest.getEnemySetup() == null) {
            EnemySetup enemySetup = new EnemySetup();
            enemySetup.setResistance(10);
            enemySetup.setLevel(80);
            enemySetup.setDmgMitigation(new ArrayList<>(){});
            evaluateRequest.setEnemySetup(enemySetup);
        }
        return evaluationService.evaluate(
            populatedData,
            evaluateRequest.getCharacterId(), evaluateRequest.getEnemySetup(),
            evaluateRequest.getOtherBonuses(), evaluateRequest.getTargetName());
    }
}
