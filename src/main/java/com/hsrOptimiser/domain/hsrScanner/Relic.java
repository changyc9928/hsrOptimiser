package com.hsrOptimiser.domain.hsrScanner;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.hsrOptimiser.utils.EmptyStringToNullSerializer;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class Relic implements Serializable {
    String setId;
    String name;
    Slot slot;
    int rarity;
    int level;
    @JsonAlias("mainstat")
    Stats mainStat;
    @JsonAlias("substats")
    ArrayList<SubStats> subStats;
    @JsonSerialize(using = EmptyStringToNullSerializer.class)
    String location;
    boolean lock;
    boolean discard;
    @JsonAlias("_uid")
    String uid;
}

