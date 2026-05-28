package com.hsrOptimiser.DTO.hsrScanner;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.io.Serializable;
import java.util.List;
import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ScannedData implements Serializable {

    String source;
    int version;
    String build;
    MetaData metadata;
    Object gacha;
    Object materials;
    List<LightCone> lightCones;
    List<Relic> relics;
    List<HSRCharacter> characters;
}
