package com.hsrOptimiser.services;

import com.hsrOptimiser.DTO.LoadoutDTO;
import com.hsrOptimiser.DTO.hsrScanner.HSRCharacter;
import com.hsrOptimiser.DTO.hsrScanner.LightCone;
import com.hsrOptimiser.DTO.hsrScanner.Relic;
import com.hsrOptimiser.DTO.hsrScanner.ScannedData;
import com.hsrOptimiser.DTO.hsrScanner.Slot;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class LoadoutServiceImpl implements LoadoutService {

    private final Memory memory;

    @Override
    public LoadoutDTO getLoadout(String userId, String characterId) {
        ScannedData data = requireData(userId);
        HSRCharacter character = requireCharacter(data, characterId);
        return new LoadoutDTO(character, equippedCone(data, characterId),
            equippedRelics(data, characterId));
    }

    @Override
    public LoadoutDTO equipLightCone(String userId, String characterId, String lightConeUid) {
        ScannedData data = requireData(userId);
        requireCharacter(data, characterId);
        LightCone cone = data.getLightCones().stream()
            .filter(lc -> lightConeUid.equals(lc.getUid()))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException(
                "Light cone not found: " + lightConeUid));
        for (LightCone equipped : data.getLightCones().stream()
            .filter(lc -> characterId.equals(lc.getLocation()))
            .toList()) {
            equipped.setLocation("");
        }
        cone.setLocation(characterId);
        return getLoadout(userId, characterId);
    }

    @Override
    public LoadoutDTO unequipLightCone(String userId, String characterId) {
        ScannedData data = requireData(userId);
        requireCharacter(data, characterId);
        for (LightCone equipped : data.getLightCones().stream()
            .filter(lc -> characterId.equals(lc.getLocation()))
            .toList()) {
            equipped.setLocation("");
        }
        return getLoadout(userId, characterId);
    }

    @Override
    public LoadoutDTO equipRelics(String userId, String characterId, List<String> relicUids) {
        ScannedData data = requireData(userId);
        requireCharacter(data, characterId);
        if (relicUids == null) {
            throw new IllegalArgumentException("relicUids must not be null");
        }
        if (relicUids.size() > 6) {
            throw new IllegalArgumentException("A character can equip at most 6 relics");
        }
        List<Relic> toEquip = new ArrayList<>();
        for (String uid : relicUids) {
            Relic relic = data.getRelics().stream()
                .filter(r -> uid.equals(r.getUid()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                    "Relic not found: " + uid));
            toEquip.add(relic);
        }
        Set<Slot> slots = EnumSet.noneOf(Slot.class);
        for (Relic relic : toEquip) {
            if (!slots.add(relic.getSlot())) {
                throw new IllegalArgumentException(
                    "Duplicate slot in request: " + relic.getSlot());
            }
        }
        for (Relic relic : equippedRelics(data, characterId)) {
            relic.setLocation("");
        }
        for (Relic relic : toEquip) {
            relic.setLocation(characterId);
        }
        return getLoadout(userId, characterId);
    }

    @Override
    public LoadoutDTO unequipRelics(String userId, String characterId) {
        ScannedData data = requireData(userId);
        requireCharacter(data, characterId);
        for (Relic relic : equippedRelics(data, characterId)) {
            relic.setLocation("");
        }
        return getLoadout(userId, characterId);
    }

    private ScannedData requireData(String userId) {
        ScannedData data = memory.getMemory(userId);
        if (data == null) {
            throw new IllegalArgumentException("No scanned data for user: " + userId);
        }
        return data;
    }

    private HSRCharacter requireCharacter(ScannedData data, String characterId) {
        return data.getCharacters().stream()
            .filter(c -> characterId.equals(c.getId()))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException(
                "Character not found: " + characterId));
    }

    private LightCone equippedCone(ScannedData data, String characterId) {
        return data.getLightCones().stream()
            .filter(lc -> characterId.equals(lc.getLocation()))
            .findFirst()
            .orElse(null);
    }

    private List<Relic> equippedRelics(ScannedData data, String characterId) {
        return data.getRelics().stream()
            .filter(r -> characterId.equals(r.getLocation()))
            .toList();
    }
}
