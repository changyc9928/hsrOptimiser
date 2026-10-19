package com.hsrOptimiser.services;

import com.hsrOptimiser.DTO.EvaluationResult;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EvaluateStatusService {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String KEY_STATUS = "evaluate:status:%s";
    private static final String KEY_PROGRESS = "evaluate:progress:%s";
    private static final String KEY_RESULT = "evaluate:result:%s";
    private static final String KEY_ERROR = "evaluate:error:%s";

    public void setStatus(String jobId, String status) {
        redisTemplate.opsForValue().set(String.format(KEY_STATUS, jobId), status);
    }

    public String getStatus(String jobId) {
        Object status = redisTemplate.opsForValue().get(String.format(KEY_STATUS, jobId));
        return status == null ? null : status.toString();
    }

    public Map<Object, Object> getProgress(String jobId) {
        return redisTemplate.opsForHash().entries(String.format(KEY_PROGRESS, jobId));
    }

    public void setResult(String jobId, EvaluationResult result) {
        redisTemplate.opsForValue().set(String.format(KEY_RESULT, jobId), result);
    }

    public Object getResult(String jobId) {
        return redisTemplate.opsForValue().get(String.format(KEY_RESULT, jobId));
    }

    public void setError(String jobId, String message) {
        redisTemplate.opsForValue().set(String.format(KEY_ERROR, jobId), message);
    }

    public String getError(String jobId) {
        Object e = redisTemplate.opsForValue().get(String.format(KEY_ERROR, jobId));
        return e == null ? null : e.toString();
    }
}
