package com.hsrOptimiser.domain.hsrScanner;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.hsrOptimiser.utils.EmptyStringToNullSerializer;
import java.io.Serializable;
import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class LightCone implements Serializable {

    String id;
    String name;
    int level;
    int ascension;
    int superimposition;
    @JsonDeserialize(using = EmptyStringToNullSerializer.class)
    String location;
    boolean lock;
    @JsonAlias("_uid")
    String uid;
}
