package com.hsrOptimiser.domain.hsrScanner;

import lombok.Data;

import java.io.Serializable;

@Data
public class CharacterSkills implements Serializable {
    int basic;
    int skill;
    int ult;
    int talent;
}
