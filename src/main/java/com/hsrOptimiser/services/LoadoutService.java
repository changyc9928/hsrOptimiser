package com.hsrOptimiser.services;

import com.hsrOptimiser.DTO.LoadoutDTO;
import java.util.List;

public interface LoadoutService {

    LoadoutDTO getLoadout(String userId, String characterId);

    LoadoutDTO equipLightCone(String userId, String characterId, String lightConeUid);

    LoadoutDTO unequipLightCone(String userId, String characterId);

    LoadoutDTO equipRelics(String userId, String characterId, List<String> relicUids);

    LoadoutDTO unequipRelics(String userId, String characterId);
}
