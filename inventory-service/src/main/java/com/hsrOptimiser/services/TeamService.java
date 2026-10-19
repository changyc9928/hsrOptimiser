package com.hsrOptimiser.services;

import com.hsrOptimiser.DTO.hsrScanner.HSRCharacter;
import java.util.List;

public interface TeamService {

    List<HSRCharacter> getTeam(String userId);

    List<HSRCharacter> saveTeam(String userId, List<String> characterIds);
}
