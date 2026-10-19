package com.hsrOptimiser.DTO;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EvaluateJobRequest {

    private String jobId;
    private String userId;
    private List<String> characterIds;
    private List<String> fixedCharacterIds;
    private List<String> allowedToScrapRelicsCharacterIds;
    private List<String> disallowedToScrapRelicsCharacterIds;
}
