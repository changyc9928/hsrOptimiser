package com.hsrOptimiser.controller;

import com.hsrOptimiser.DTO.ApiResponse;
import com.hsrOptimiser.services.EvaluateStatusService;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/evaluate")
@RequiredArgsConstructor
public class EvaluateController {

    private final EvaluateStatusService evaluateStatusService;

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
