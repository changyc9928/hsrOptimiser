package com.hsrOptimiser.controller;

import com.hsrOptimiser.DTO.ApiResponse;
import com.hsrOptimiser.DTO.EvaluateJobRequest;
import com.hsrOptimiser.DTO.EvaluateRequestV2;
import com.hsrOptimiser.config.RabbitMqConfig;
import com.hsrOptimiser.services.EvaluateStatusService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/evaluate")
@RequiredArgsConstructor
public class EvaluateController {

    private final RabbitTemplate rabbitTemplate;
    private final EvaluateStatusService evaluateStatusService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostMapping
    public ResponseEntity<ApiResponse<Map<String, String>>> evaluateV2(
        @org.springframework.web.bind.annotation.RequestAttribute("userId") String userId,
        @RequestBody EvaluateRequestV2 requestV2) {
        String jobId = UUID.randomUUID().toString();
        EvaluateJobRequest job = new EvaluateJobRequest(
            jobId,
            userId,
            requestV2.getCharacterIds(),
            requestV2.getFixedCharacterIds(),
            requestV2.getAllowedToScrapRelicsCharacterIds(),
            requestV2.getDisallowedToScrapRelicsCharacterIds());

        evaluateStatusService.setStatus(jobId, "queued");
        try {
            rabbitTemplate.convertAndSend(RabbitMqConfig.EVALUATE_QUEUE,
                objectMapper.writeValueAsString(job));
        } catch (Exception e) {
            evaluateStatusService.setError(jobId, e.getMessage());
            evaluateStatusService.setStatus(jobId, "failed");
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new ApiResponse<>(false, null, "Failed to queue evaluate job",
                    HttpStatus.SERVICE_UNAVAILABLE.value()));
        }
        Map<String, String> body = new HashMap<>();
        body.put("jobId", jobId);
        body.put("status", "queued");
        return ResponseEntity.ok(
            new ApiResponse<>(true, body, "Evaluate job queued", HttpStatus.OK.value()));
    }

    @GetMapping("/{jobId}/status")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStatus(@PathVariable String jobId) {
        String status = evaluateStatusService.getStatus(jobId);
        if (status == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiResponse<>(false, null, "No such job", HttpStatus.NOT_FOUND.value()));
        }

        Map<String, Object> body = new HashMap<>();
        body.put("jobId", jobId);
        body.put("status", status);
        body.put("progress", evaluateStatusService.getProgress(jobId));
        if ("failed".equals(status)) {
            body.put("error", evaluateStatusService.getError(jobId));
        }
        return ResponseEntity.ok(
            new ApiResponse<>(true, body, "OK", HttpStatus.OK.value()));
    }

    @GetMapping("/{jobId}/result")
    public ResponseEntity<ApiResponse<Object>> getResult(@PathVariable String jobId) {
        String status = evaluateStatusService.getStatus(jobId);
        if (status == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiResponse<>(false, null, "No such job", HttpStatus.NOT_FOUND.value()));
        }
        if (!"finished".equals(status)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ApiResponse<>(false, null, "Job not finished: " + status,
                    HttpStatus.CONFLICT.value()));
        }
        return ResponseEntity.ok(
            new ApiResponse<>(true, evaluateStatusService.getResult(jobId), "OK",
                HttpStatus.OK.value()));
    }
}
