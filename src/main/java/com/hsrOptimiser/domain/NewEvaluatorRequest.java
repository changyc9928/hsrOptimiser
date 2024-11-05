package com.hsrOptimiser.domain;

import java.util.HashMap;
import lombok.Data;

@Data
public class NewEvaluatorRequest {

    String characterId;
    EnemySetup enemySetup;
    HashMap<String, Float> otherBonuses;
    String targetFormula;
    String targetName;
}
