package com.hsrOptimiser.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EvaluationTriggerEvent {

    private String jobId;
    private String userId;
}
