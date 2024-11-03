package com.hsrOptimiser.domain.projectYatta;

import java.io.Serializable;
import lombok.Data;

@Data
public class Upgrade implements Serializable {

    SkillAdd skillAdd;
    SkillBase skillBase;
}
