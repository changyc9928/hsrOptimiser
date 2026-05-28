package com.hsrOptimiser.DTO;

import com.hsrOptimiser.DTO.hsrScanner.Relic;
import java.util.List;
import lombok.Data;

@Data
public class CharacterDamage {
    double totalDamage;
    String name;
    List<Relic>  relics;
}
