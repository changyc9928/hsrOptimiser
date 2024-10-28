package com.hsrOptimiser.domain.projectYatta;

import lombok.Data;

import java.io.Serializable;

@Data
public class Upgrade implements Serializable {
    SkillAdd skillAdd;
    SkillBase skillBase;
}
