package com.hsrOptimiser.domain.hsrScanner;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.hsrOptimiser.utils.EmptyStringToNullSerializer;
import lombok.Data;

import java.io.Serializable;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class LightCone implements Serializable {
    String id;
    String name;
    int level;
    int ascension;
    int superimposition;
    @JsonSerialize(using = EmptyStringToNullSerializer.class)
    String location;
    boolean lock;
    @JsonAlias("_uid")
    String uid;
}
