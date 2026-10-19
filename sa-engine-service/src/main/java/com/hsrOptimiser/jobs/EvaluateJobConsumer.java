package com.hsrOptimiser.jobs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hsrOptimiser.DTO.EvaluationTriggerEvent;
import com.hsrOptimiser.DTO.EvaluationResult;
import com.hsrOptimiser.services.EvaluateStatusService;
import com.hsrOptimiser.services.EvaluationService;
import com.hsrOptimiser.services.InventoryClient;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class EvaluateJobConsumer {

    private final EvaluationService evaluationService;
    private final EvaluateStatusService statusService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @KafkaListener(topics = "evaluation-jobs", groupId = "sa-engine")
    public void onEvaluateJob(String message) {
        EvaluationTriggerEvent event;
        try {
            event = objectMapper.readValue(message, EvaluationTriggerEvent.class);
        } catch (Exception e) {
            log.error("Failed to parse evaluation trigger event", e);
            return;
        }

        String jobId = event.getJobId();
        String userId = event.getUserId();

        // Kafka redelivers a message when its offset was never committed (restart, rebalance,
        // dropped connection). Re-running a finished job would overwrite a good result with a
        // partial one and burn another 800 damage evaluations, so terminal states are final.
        String existing = statusService.getStatus(jobId);
        if ("finished".equals(existing) || "failed".equals(existing)) {
            log.info("Skipping already processed evaluate job {} (status: {})", jobId, existing);
            return;
        }

        statusService.setStatus(jobId, "queued");
        log.info("Processing evaluate job {} for user {}", jobId, userId);
        statusService.setStatus(jobId, "running");

        try {
            List<String> team = readTeam(userId);
            if (team == null || team.isEmpty()) {
                throw new IllegalStateException("No cached team lineup for user: " + userId);
            }
            EvaluationResult result = evaluationService.evaluateAsagi(
                userId,
                team,
                List.of(),
                team,
                List.of(),
                jobId
            );
            statusService.setResult(jobId, result);
            statusService.setStatus(jobId, "finished");
            log.info("Evaluate job {} finished", jobId);
        } catch (Exception e) {
            log.error("Evaluate job {} failed", jobId, e);
            statusService.setError(jobId, e.getMessage());
            statusService.setStatus(jobId, "failed");
        }
    }

    @SuppressWarnings("unchecked")
    private List<String> readTeam(String userId) {
        Object value = redisTemplate.opsForValue().get("inventory:team:" + userId);
        if (value instanceof List<?> list) {
            List<String> ids = new java.util.ArrayList<>();
            for (Object o : list) {
                ids.add(String.valueOf(o));
            }
            return ids;
        }
        return null;
    }
}
