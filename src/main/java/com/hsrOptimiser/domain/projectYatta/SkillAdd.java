package com.hsrOptimiser.domain.projectYatta;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

import java.io.Serializable;

@Data
public class SkillAdd implements Serializable {
    float attackAdd;
    float defenceAdd;
    @JsonAlias("hPAdd")
    float hPAdd;
}
