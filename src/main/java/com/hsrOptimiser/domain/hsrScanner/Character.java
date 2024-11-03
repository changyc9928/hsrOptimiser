package com.hsrOptimiser.domain.hsrScanner;

import java.io.Serializable;
import lombok.Data;

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

