package com.hsrOptimiser.controller;

import com.hsrOptimiser.DTO.ApiResponse;
import com.hsrOptimiser.DTO.EvaluationTriggerEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/evaluate")
@RequiredArgsConstructor
public class EvaluateController {

    public static final String TOPIC = "evaluation-jobs";

    private final KafkaTemplate<String, EvaluationTriggerEvent> kafkaTemplate;

    @PostMapping
    public ResponseEntity<ApiResponse<Map<String, String>>> trigger(
        @org.springframework.web.bind.annotation.RequestAttribute("userId") String userId) {
        String jobId = UUID.randomUUID().toString();
        kafkaTemplate.send(TOPIC, userId, new EvaluationTriggerEvent(jobId, userId));
        Map<String, String> body = new HashMap<>();
        body.put("jobId", jobId);
        body.put("status", "queued");
        return ResponseEntity.ok(new ApiResponse<>(true, body, "Evaluate job queued",
            org.springframework.http.HttpStatus.OK.value()));
    }
}
