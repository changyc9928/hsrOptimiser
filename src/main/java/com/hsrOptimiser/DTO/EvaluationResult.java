package com.hsrOptimiser.DTO;

import java.util.List;
import lombok.Data;

@Data
public class EvaluationResult {
    double totalDamage;
    List<CharacterDamage> characterDamage;
}
