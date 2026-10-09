package com.hsrOptimiser.services;

import com.hsrOptimiser.DTO.LoadoutDTO;
import com.hsrOptimiser.DTO.CreateCharacterRequest;
import com.hsrOptimiser.DTO.CreateLightConeRequest;
import com.hsrOptimiser.DTO.CreateRelicRequest;
import com.hsrOptimiser.DTO.hsrScanner.CharacterSkills;
import com.hsrOptimiser.DTO.hsrScanner.CharacterTraces;
import com.hsrOptimiser.DTO.hsrScanner.HSRCharacter;
import com.hsrOptimiser.DTO.hsrScanner.LightCone;
import com.hsrOptimiser.DTO.hsrScanner.Relic;
import com.hsrOptimiser.DTO.hsrScanner.ScannedData;
import com.hsrOptimiser.DTO.hsrScanner.Slot;
import com.hsrOptimiser.DTO.hsrScanner.SubStats;
import com.hsrOptimiser.clientConfig.AsagiCharacterMetadata;
import com.hsrOptimiser.clientConfig.AsagiLightConeMetadata;
import com.hsrOptimiser.clientConfig.AsagiRelicSetMetadata;
import com.hsrOptimiser.engine.SubStatAggregator;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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

    @Override
    public LightCone addLightCone(String userId, CreateLightConeRequest request) {
        ScannedData data = requireData(userId);
        if (request.getId() == null) {
            throw new IllegalArgumentException("Light cone id must not be null");
        }
        AsagiLightConeMetadata info;
        try {
            info = AsagiLightConeMetadata.getInfoById(request.getId());
        } catch (EnumConstantNotPresentException e) {
            throw new IllegalArgumentException("Unknown light cone id: " + request.getId());
        }
        String uid = requireUniqueUid(
            data.getLightCones().stream().map(LightCone::getUid).toList(), request.getUid());
        int superimposition = request.getSuperimposition() == null ? 1
            : request.getSuperimposition();
        if (superimposition < 1 || superimposition > 5) {
            throw new IllegalArgumentException("Superimposition must be between 1 and 5");
        }
        String location = request.getLocation() == null ? "" : request.getLocation();
        if (!location.isEmpty()) {
            requireCharacter(data, location);
        }
        LightCone cone = new LightCone();
        cone.setId(request.getId());
        cone.setName(request.getName() == null ? info.getInternalName() : request.getName());
        cone.setLevel(request.getLevel() == null ? 1 : request.getLevel());
        cone.setAscension(request.getAscension() == null ? 0 : request.getAscension());
        cone.setSuperimposition(superimposition);
        cone.setLocation(location);
        cone.setLock(request.getLock() != null && request.getLock());
        cone.setUid(uid);
        data.getLightCones().add(cone);
        return cone;
    }

    @Override
    public HSRCharacter addCharacter(String userId, CreateCharacterRequest request) {
        ScannedData data = requireData(userId);
        if (request.getId() == null) {
            throw new IllegalArgumentException("Character id must not be null");
        }
        if (data.getCharacters().stream().anyMatch(c -> request.getId().equals(c.getId()))) {
            throw new IllegalArgumentException(
                "Character already exists: " + request.getId());
        }
        int abilityVersion = request.getAbilityVersion() == null ? 0
            : request.getAbilityVersion();
        AsagiCharacterMetadata info;
        try {
            info = AsagiCharacterMetadata.getInfoById(request.getId(), abilityVersion);
        } catch (EnumConstantNotPresentException e) {
            throw new IllegalArgumentException("Unknown character id: " + request.getId());
        }
        int eidolon = request.getEidolon() == null ? 0 : request.getEidolon();
        if (eidolon < 0 || eidolon > 6) {
            throw new IllegalArgumentException("Eidolon must be between 0 and 6");
        }
        HSRCharacter character = new HSRCharacter();
        character.setId(request.getId());
        character.setName(request.getName() == null ? info.getDisplayName() : request.getName());
        character.setPath(request.getPath() == null ? capitalize(info.getPath())
            : request.getPath());
        character.setLevel(request.getLevel() == null ? 1 : request.getLevel());
        character.setAscension(request.getAscension() == null ? 0 : request.getAscension());
        character.setEidolon(eidolon);
        character.setSkills(request.getSkills() == null ? new CharacterSkills()
            : request.getSkills());
        character.setTraces(request.getTraces() == null ? new CharacterTraces()
            : request.getTraces());
        character.setAbilityVersion(abilityVersion);
        character.setMemosprite(request.getMemosprite());
        data.getCharacters().add(character);
        return character;
    }

    @Override
    public Relic addRelic(String userId, CreateRelicRequest request) {
        ScannedData data = requireData(userId);
        if (request.getSetId() == null) {
            throw new IllegalArgumentException("Relic setId must not be null");
        }
        if (AsagiRelicSetMetadata.fromId(request.getSetId()) == null) {
            throw new IllegalArgumentException("Unknown relic setId: " + request.getSetId());
        }
        if (request.getSlot() == null) {
            throw new IllegalArgumentException("Relic slot must not be null");
        }
        if (request.getMainstat() == null || !MAIN_STATS.contains(request.getMainstat())) {
            throw new IllegalArgumentException("Unknown relic mainstat: " + request.getMainstat());
        }
        List<SubStats> substats = request.getSubstats() == null ? List.of()
            : request.getSubstats();
        for (SubStats sub : substats) {
            if (!SubStatAggregator.STAT_MAPPERS.containsKey(sub.getKey())) {
                throw new IllegalArgumentException("Unknown relic substat: " + sub.getKey());
            }
            if (sub.getValue() < 0) {
                throw new IllegalArgumentException(
                    "Substat value must not be negative: " + sub.getKey());
            }
        }
        int rarity = request.getRarity() == null ? 5 : request.getRarity();
        if (rarity < 1 || rarity > 5) {
            throw new IllegalArgumentException("Rarity must be between 1 and 5");
        }
        String uid = requireUniqueUid(
            data.getRelics().stream().map(Relic::getUid).toList(), request.getUid());
        String location = request.getLocation() == null ? "" : request.getLocation();
        if (!location.isEmpty()) {
            requireCharacter(data, location);
            boolean conflict = data.getRelics().stream()
                .anyMatch(r -> location.equals(r.getLocation())
                    && request.getSlot() == r.getSlot());
            if (conflict) {
                throw new IllegalArgumentException(
                    "Character " + location + " already has a " + request.getSlot()
                        + " relic equipped");
            }
        }
        Relic relic = new Relic();
        relic.setSetId(request.getSetId());
        relic.setName(request.getName() == null
            ? AsagiRelicSetMetadata.fromId(request.getSetId()).getLiteralName()
            : request.getName());
        relic.setSlot(request.getSlot());
        relic.setRarity(rarity);
        relic.setLevel(request.getLevel() == null ? 0 : request.getLevel());
        relic.setMainstat(request.getMainstat());
        relic.setSubstats(new ArrayList<>(substats));
        relic.setLocation(location);
        relic.setLock(request.getLock() != null && request.getLock());
        relic.setDiscard(false);
        relic.setUid(uid);
        data.getRelics().add(relic);
        return relic;
    }

    private static final Set<String> MAIN_STATS = Set.of(
        "HP", "ATK", "DEF", "SPD",
        "CRIT Rate", "CRIT DMG",
        "Effect Hit Rate", "Break Effect",
        "Energy Regeneration Rate", "Outgoing Healing Boost",
        "Physical DMG Boost", "Fire DMG Boost", "Ice DMG Boost",
        "Lightning DMG Boost", "Wind DMG Boost",
        "Quantum DMG Boost", "Imaginary DMG Boost");

    private String requireUniqueUid(List<String> existing, String requested) {
        String uid = requested == null ? UUID.randomUUID().toString() : requested;
        if (existing.contains(uid)) {
            throw new IllegalArgumentException("Uid already exists: " + uid);
        }
        return uid;
    }

    private String capitalize(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        return Character.toUpperCase(value.charAt(0)) + value.substring(1);
    }
}
