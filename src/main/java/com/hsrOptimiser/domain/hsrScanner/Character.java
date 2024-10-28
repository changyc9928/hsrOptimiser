package com.hsrOptimiser.domain.hsrScanner;

import lombok.Data;

import java.io.Serializable;

@Data
public class Character implements Serializable {
    String id;
    String name;
    Path path;
    int level;
    int ascension;
    int eidolon;
    CharacterSkills skills;
    CharacterTraces traces;
}

