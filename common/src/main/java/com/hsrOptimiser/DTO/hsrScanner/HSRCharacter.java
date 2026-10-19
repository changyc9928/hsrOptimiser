package com.hsrOptimiser.DTO.hsrScanner;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.io.Serializable;
import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class HSRCharacter implements Serializable {

    String id;
    String name;
    String path;
    int level;
    int ascension;
    int eidolon;
    CharacterSkills skills;
    CharacterTraces traces;
    int abilityVersion;
    CharacterSkills memosprite;
}

