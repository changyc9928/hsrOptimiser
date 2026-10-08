package com.hsrOptimiser.jobs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hsrOptimiser.DTO.EvaluateJobRequest;
import com.hsrOptimiser.DTO.EvaluationResult;
import com.hsrOptimiser.config.RabbitMqConfig;
import com.hsrOptimiser.services.EvaluateStatusService;
import com.hsrOptimiser.services.EvaluationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class EvaluateJobConsumer {

    private final EvaluationService evaluationService;
    private final EvaluateStatusService statusService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @RabbitListener(queues = RabbitMqConfig.EVALUATE_QUEUE)
    public void onEvaluateJob(String messageJson) {
        EvaluateJobRequest request;
        try {
            request = objectMapper.readValue(messageJson, EvaluateJobRequest.class);
        } catch (Exception e) {
            log.error("Failed to parse evaluate job message", e);
            return;
        }

        log.info("Processing evaluate job {} for user {}", request.getJobId(), request.getUserId());
        statusService.setStatus(request.getJobId(), "running");

        try {
            EvaluationResult result = evaluationService.evaluateAsagi(
                request.getUserId(),
                request.getCharacterIds(),
                request.getFixedCharacterIds(),
                request.getAllowedToScrapRelicsCharacterIds(),
                request.getDisallowedToScrapRelicsCharacterIds(),
                request.getJobId()
            );
            statusService.setResult(request.getJobId(), result);
            statusService.setStatus(request.getJobId(), "finished");
            log.info("Evaluate job {} finished", request.getJobId());
        } catch (Exception e) {
            log.error("Evaluate job {} failed", request.getJobId(), e);
            statusService.setError(request.getJobId(), e.getMessage());
            statusService.setStatus(request.getJobId(), "failed");
        }
    }
}
