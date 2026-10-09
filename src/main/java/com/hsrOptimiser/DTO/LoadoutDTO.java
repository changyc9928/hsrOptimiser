package com.hsrOptimiser.DTO;

import com.hsrOptimiser.DTO.hsrScanner.HSRCharacter;
import com.hsrOptimiser.DTO.hsrScanner.LightCone;
import com.hsrOptimiser.DTO.hsrScanner.Relic;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoadoutDTO {

    private HSRCharacter character;
    private LightCone lightCone;
    private List<Relic> relics;
}
