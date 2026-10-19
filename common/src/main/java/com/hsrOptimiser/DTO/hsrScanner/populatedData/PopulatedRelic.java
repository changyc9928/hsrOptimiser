package com.hsrOptimiser.DTO.hsrScanner.populatedData;

import com.hsrOptimiser.DTO.hsrScanner.Relic;
import java.io.Serializable;
import lombok.Data;

@Data
public class PopulatedRelic extends Relic implements Serializable {

    double mainStatValue;
}
