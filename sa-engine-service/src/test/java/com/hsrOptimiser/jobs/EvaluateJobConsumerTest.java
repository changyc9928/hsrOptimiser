package com.hsrOptimiser.jobs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hsrOptimiser.DTO.EvaluationResult;
import com.hsrOptimiser.DTO.EvaluationTriggerEvent;
import com.hsrOptimiser.services.EvaluateStatusService;
import com.hsrOptimiser.services.EvaluationService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

/**
 * Kafka redelivers messages whose offsets were never committed. A redelivered job must not
 * overwrite an already published result.
 */
@ExtendWith(MockitoExtension.class)
class EvaluateJobConsumerTest {

    private static final String JOB_ID = "job-1";
    private static final String USER_ID = "user-1";

    @Mock
    private EvaluationService evaluationService;

    @Mock
    private EvaluateStatusService statusService;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    private EvaluateJobConsumer consumer;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        consumer = new EvaluateJobConsumer(evaluationService, statusService, redisTemplate);
    }

    private String message() throws Exception {
        return new ObjectMapper().writeValueAsString(new EvaluationTriggerEvent(JOB_ID, USER_ID));
    }

    @Test
    @DisplayName("A finished job is not re-run when the message is redelivered")
    void skipsAlreadyFinishedJob() throws Exception {
        when(statusService.getStatus(JOB_ID)).thenReturn("finished");

        consumer.onEvaluateJob(message());

        verify(evaluationService, never()).evaluateAsagi(any(), anyList(), anyList(), anyList(),
            anyList(), any());
        verify(statusService, never()).setResult(any(), any());
    }

    @Test
    @DisplayName("A failed job is not retried either")
    void skipsAlreadyFailedJob() throws Exception {
        when(statusService.getStatus(JOB_ID)).thenReturn("failed");

        consumer.onEvaluateJob(message());

        verify(evaluationService, never()).evaluateAsagi(any(), anyList(), anyList(), anyList(),
            anyList(), any());
    }

    @Test
    @DisplayName("An unfinished job runs and stores its result")
    void runsUnfinishedJob() throws Exception {
        when(statusService.getStatus(JOB_ID)).thenReturn(null, null);
        when(valueOperations.get("inventory:team:" + USER_ID)).thenReturn(List.of("1505"));
        EvaluationResult result = new EvaluationResult();
        result.setTotalDamage(1234);
        when(evaluationService.evaluateAsagi(eq(USER_ID), eq(List.of("1505")), anyList(),
            anyList(), anyList(), eq(JOB_ID))).thenReturn(result);

        consumer.onEvaluateJob(message());

        verify(statusService).setResult(JOB_ID, result);
        verify(statusService).setStatus(JOB_ID, "finished");
    }
}