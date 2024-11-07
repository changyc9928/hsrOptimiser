package com.hsrOptimiser.domain;

import java.util.ArrayList;
import lombok.Data;

@Data
public class EnemySetup {

    float resistance;
    int level;
    ArrayList<Float> dmgMitigation;
}
