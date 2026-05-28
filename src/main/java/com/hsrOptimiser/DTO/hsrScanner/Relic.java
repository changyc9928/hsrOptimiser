package com.hsrOptimiser.DTO.hsrScanner;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.io.Serializable;
import java.util.List;
import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class Relic implements Serializable {

    String setId;
    String name;
    Slot slot;
    int rarity;
    int level;
//    @JsonAlias("mainstat")
//    @JsonDeserialize(using = StatsMappingSerializer.class)
    String mainstat;
//    @JsonAlias("substats")
    List<SubStats> substats;
    List<SubStats> previewSubstats;
    String location;
    boolean lock;
    boolean discard;
    @JsonAlias("_uid")
    String uid;
}

