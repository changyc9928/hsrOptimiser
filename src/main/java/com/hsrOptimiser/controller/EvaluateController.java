package com.hsrOptimiser.controller;

import com.hsrOptimiser.DTO.EvaluateRequestV2;
import com.hsrOptimiser.DTO.EvaluationResult;
import com.hsrOptimiser.services.EvaluationService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.PathVariable;
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

    @PostMapping("/{userId}")
    public EvaluationResult evaluateV2(
        @PathVariable String userId, @RequestBody EvaluateRequestV2 requestV2) {
        return evaluationService.evaluateAsagi(userId, requestV2.getCharacterIds(),
            requestV2.getFixedCharacterIds(), requestV2.getAllowedToScrapRelicsCharacterIds(),
            requestV2.getDisallowedToScrapRelicsCharacterIds());
    }
}
