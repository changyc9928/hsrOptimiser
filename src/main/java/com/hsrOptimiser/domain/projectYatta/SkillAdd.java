package com.hsrOptimiser.domain.projectYatta;

import com.fasterxml.jackson.annotation.JsonAlias;
import java.io.Serializable;
import lombok.Data;

@Data
public class SkillAdd implements Serializable {

    float attackAdd;
    float defenceAdd;
    @JsonAlias("hPAdd")
    float hPAdd;
}
