package com.hsrOptimiser.domain.hsrScanner.populatedData;

import com.hsrOptimiser.domain.hsrScanner.Relic;
import java.io.Serializable;
import lombok.Data;

@Data
public class PopulatedRelic extends Relic implements Serializable {

    float mainStatValue;
}
