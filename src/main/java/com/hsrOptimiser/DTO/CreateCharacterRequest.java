package com.hsrOptimiser.DTO;

import com.hsrOptimiser.DTO.hsrScanner.CharacterSkills;
import com.hsrOptimiser.DTO.hsrScanner.CharacterTraces;
import lombok.Data;

@Data
public class CreateCharacterRequest {

    private String id;
    private String name;
    private String path;
    private Integer level;
    private Integer ascension;
    private Integer eidolon;
    private CharacterSkills skills;
    private CharacterTraces traces;
    private Integer abilityVersion;
    private CharacterSkills memosprite;
}
