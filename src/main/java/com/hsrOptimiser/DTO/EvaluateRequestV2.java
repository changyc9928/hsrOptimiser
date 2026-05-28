package com.hsrOptimiser.DTO;

import java.util.List;
import lombok.Data;

@Data
public class EvaluateRequestV2 {
    String userId;
    List<String> characterIds;
    List<String> fixedCharacterIds;
    List<String> allowedToScrapRelicsCharacterIds;
    List<String> disallowedToScrapRelicsCharacterIds;
}
