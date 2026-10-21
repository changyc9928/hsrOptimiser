package com.hsrOptimiser.services;

import com.hsrOptimiser.DTO.CreateCharacterRequest;
import com.hsrOptimiser.DTO.CreateLightConeRequest;
import com.hsrOptimiser.DTO.CreateRelicRequest;
import com.hsrOptimiser.DTO.LoadoutDTO;
import com.hsrOptimiser.DTO.UpdateCharacterRequest;
import com.hsrOptimiser.DTO.UpdateLightConeRequest;
import com.hsrOptimiser.DTO.UpdateRelicRequest;
import com.hsrOptimiser.DTO.hsrScanner.HSRCharacter;
import com.hsrOptimiser.DTO.hsrScanner.LightCone;
import com.hsrOptimiser.DTO.hsrScanner.Relic;
import java.util.List;

public interface LoadoutService {

    LoadoutDTO getLoadout(String userId, String characterId);

    LoadoutDTO equipLightCone(String userId, String characterId, String lightConeUid);

    LoadoutDTO unequipLightCone(String userId, String characterId);

    LoadoutDTO equipRelics(String userId, String characterId, List<String> relicUids);

    LoadoutDTO unequipRelics(String userId, String characterId);

    LightCone addLightCone(String userId, CreateLightConeRequest request);

    HSRCharacter addCharacter(String userId, CreateCharacterRequest request);

    Relic addRelic(String userId, CreateRelicRequest request);

    HSRCharacter updateCharacter(String userId, String characterId, UpdateCharacterRequest request);

    LightCone updateLightCone(String userId, String lightConeUid, UpdateLightConeRequest request);

    Relic updateRelic(String userId, String relicUid, UpdateRelicRequest request);

    void deleteCharacter(String userId, String characterId);

    void deleteLightCone(String userId, String lightConeUid);

    void deleteRelic(String userId, String relicUid);
}
