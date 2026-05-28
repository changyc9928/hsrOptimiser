package com.hsrOptimiser.DTO;

import java.util.HashMap;
import lombok.Data;

@Data
public class EvaluateRequest {

    String characterId;
    EnemySetup enemySetup;
    HashMap<String, Double> otherBonuses;
    String targetName;
}
