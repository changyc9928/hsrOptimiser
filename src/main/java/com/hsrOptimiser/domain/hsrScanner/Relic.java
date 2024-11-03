package com.hsrOptimiser.domain.hsrScanner;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.hsrOptimiser.utils.EmptyStringToNullSerializer;
import com.hsrOptimiser.utils.StatsMappingSerializer;
import java.io.Serializable;
import java.util.ArrayList;
import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class Relic implements Serializable {

    String setId;
    String name;
    Slot slot;
    int rarity;
    int level;
    @JsonAlias("mainstat")
    @JsonDeserialize(using = StatsMappingSerializer.class)
    String mainStat;
    @JsonAlias("substats")
    ArrayList<SubStats> subStats;
    @JsonDeserialize(using = EmptyStringToNullSerializer.class)
    String location;
    boolean lock;
    boolean discard;
    @JsonAlias("_uid")
    String uid;
}

