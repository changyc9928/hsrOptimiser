package com.hsrOptimiser.domain;

import java.util.HashMap;
import lombok.Data;

@Data
public class EvaluateRequest {

    String characterId;
    EnemySetup enemySetup;
    HashMap<String, Float> otherBonuses;
    String targetName;
}
