package com.hsrOptimiser.DTO.hsrScanner;

import java.io.Serializable;
import lombok.Data;

@Data
public class CharacterSkills implements Serializable {

    int basic;
    int skill;
    int ult;
    int talent;
    int elation;
}
