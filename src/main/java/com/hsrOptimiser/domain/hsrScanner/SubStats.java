package com.hsrOptimiser.domain.hsrScanner;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.hsrOptimiser.utils.StatsMappingSerializer;
import java.io.Serializable;
import lombok.Data;

@Data
public class SubStats implements Serializable {

    @JsonDeserialize(using = StatsMappingSerializer.class)
    String key;
    float value;
}
