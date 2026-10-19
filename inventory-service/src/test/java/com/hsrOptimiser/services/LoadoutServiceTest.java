package com.hsrOptimiser.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.hsrOptimiser.DTO.CreateCharacterRequest;
import com.hsrOptimiser.DTO.CreateLightConeRequest;
import com.hsrOptimiser.DTO.CreateRelicRequest;
import com.hsrOptimiser.DTO.LoadoutDTO;
import com.hsrOptimiser.DTO.hsrScanner.CharacterSkills;
import com.hsrOptimiser.DTO.hsrScanner.HSRCharacter;
import com.hsrOptimiser.DTO.hsrScanner.LightCone;
import com.hsrOptimiser.DTO.hsrScanner.Relic;
import com.hsrOptimiser.DTO.hsrScanner.ScannedData;
import com.hsrOptimiser.DTO.hsrScanner.Slot;
import com.hsrOptimiser.DTO.hsrScanner.SubStats;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LoadoutServiceTest {

    private static final String USER = "u1";
    private static final String CHAR = "1308";

    private Memory memory;
    private LoadoutService loadoutService;

    private Relic relic(String uid, Slot slot, String location) {
        Relic relic = new Relic();
        relic.setUid(uid);
        relic.setSlot(slot);
        relic.setLocation(location);
        relic.setRarity(5);
        return relic;
    }

    private LightCone cone(String uid, String location) {
        LightCone cone = new LightCone();
        cone.setUid(uid);
        cone.setLocation(location);
        return cone;
    }

    @BeforeEach
    void setUp() {
        memory = new MemoryImpl();
        loadoutService = new LoadoutServiceImpl(memory);

        ScannedData data = new ScannedData();
        HSRCharacter character = new HSRCharacter();
        character.setId(CHAR);
        data.setCharacters(new ArrayList<>(List.of(character)));
        data.setLightCones(new ArrayList<>(List.of(cone("c1", CHAR), cone("c2", ""))));
        data.setRelics(new ArrayList<>(List.of(
            relic("r1", Slot.Head, CHAR),
            relic("r2", Slot.Hands, ""),
            relic("r3", Slot.Hands, ""))));
        memory.insertMemory(USER, data);
    }

    @Test
    void getLoadoutReturnsEquippedItems() {
        LoadoutDTO loadout = loadoutService.getLoadout(USER, CHAR);
        assertThat(loadout.getLightCone().getUid()).isEqualTo("c1");
        assertThat(loadout.getRelics()).extracting(Relic::getUid).containsExactly("r1");
    }

    @Test
    void equipLightConeReplacesPrevious() {
        LoadoutDTO loadout = loadoutService.equipLightCone(USER, CHAR, "c2");
        assertThat(loadout.getLightCone().getUid()).isEqualTo("c2");
        assertThat(memory.getMemory(USER).getLightCones())
            .filteredOn(lc -> CHAR.equals(lc.getLocation()))
            .extracting(LightCone::getUid)
            .containsExactly("c2");
    }

    @Test
    void equipLightConeUnknownUidFails() {
        assertThatThrownBy(() -> loadoutService.equipLightCone(USER, CHAR, "nope"))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void equipRelicsReplacesFullSet() {
        LoadoutDTO loadout = loadoutService.equipRelics(USER, CHAR, List.of("r2"));
        assertThat(loadout.getRelics()).extracting(Relic::getUid)
            .containsExactly("r2");
    }

    @Test
    void equipRelicsDuplicateSlotFails() {
        assertThatThrownBy(() -> loadoutService.equipRelics(USER, CHAR, List.of("r2", "r3")))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Duplicate slot");
    }

    @Test
    void equipRelicsUnknownUidFails() {
        assertThatThrownBy(() -> loadoutService.equipRelics(USER, CHAR, List.of("nope")))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void unequipClearsAll() {
        loadoutService.unequipLightCone(USER, CHAR);
        loadoutService.unequipRelics(USER, CHAR);
        LoadoutDTO loadout = loadoutService.getLoadout(USER, CHAR);
        assertThat(loadout.getLightCone()).isNull();
        assertThat(loadout.getRelics()).isEmpty();
    }

    @Test
    void unknownCharacterFails() {
        assertThatThrownBy(() -> loadoutService.getLoadout(USER, "9999"))
            .isInstanceOf(IllegalArgumentException.class);
    }
}

class LoadoutServiceInventoryTest {

    private Memory memory;
    private LoadoutService loadoutService;

    @BeforeEach
    void setUp() {
        memory = new MemoryImpl();
        loadoutService = new LoadoutServiceImpl(memory);

        ScannedData data = new ScannedData();
        HSRCharacter character = new HSRCharacter();
        character.setId("1308");
        data.setCharacters(new ArrayList<>(List.of(character)));
        data.setLightCones(new ArrayList<>());
        data.setRelics(new ArrayList<>());
        memory.insertMemory("u1", data);
    }

    @Test
    void addLightConeWithLevelAndSuperimposition() {
        CreateLightConeRequest request = new CreateLightConeRequest();
        request.setId("23000");
        request.setLevel(80);
        request.setSuperimposition(5);
        request.setUid("lc-new");

        LightCone cone = loadoutService.addLightCone("u1", request);

        assertThat(cone.getUid()).isEqualTo("lc-new");
        assertThat(cone.getLevel()).isEqualTo(80);
        assertThat(cone.getSuperimposition()).isEqualTo(5);
        assertThat(cone.getName()).isNotBlank();
    }

    @Test
    void addLightConeUnknownIdFails() {
        CreateLightConeRequest request = new CreateLightConeRequest();
        request.setId("99999");
        assertThatThrownBy(() -> loadoutService.addLightCone("u1", request))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void addLightConeDuplicateUidFails() {
        CreateLightConeRequest first = new CreateLightConeRequest();
        first.setId("23000");
        first.setUid("dup");
        loadoutService.addLightCone("u1", first);

        CreateLightConeRequest second = new CreateLightConeRequest();
        second.setId("23001");
        second.setUid("dup");
        assertThatThrownBy(() -> loadoutService.addLightCone("u1", second))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void addCharacterWithLevelEidolonAndSkills() {
        CreateCharacterRequest request = new CreateCharacterRequest();
        request.setId("1310");
        request.setLevel(80);
        request.setEidolon(6);
        CharacterSkills skills = new CharacterSkills();
        skills.setBasic(6);
        skills.setSkill(10);
        skills.setUlt(10);
        skills.setTalent(10);
        request.setSkills(skills);

        HSRCharacter character = loadoutService.addCharacter("u1", request);

        assertThat(character.getId()).isEqualTo("1310");
        assertThat(character.getEidolon()).isEqualTo(6);
        assertThat(character.getSkills().getUlt()).isEqualTo(10);
    }

    @Test
    void addCharacterDuplicateIdFails() {
        CreateCharacterRequest request = new CreateCharacterRequest();
        request.setId("1308");
        assertThatThrownBy(() -> loadoutService.addCharacter("u1", request))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void addCharacterBadEidolonFails() {
        CreateCharacterRequest request = new CreateCharacterRequest();
        request.setId("1310");
        request.setEidolon(7);
        assertThatThrownBy(() -> loadoutService.addCharacter("u1", request))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void addRelicWithStats() {
        CreateRelicRequest request = new CreateRelicRequest();
        request.setSetId("129");
        request.setSlot(Slot.Body);
        request.setLevel(15);
        request.setMainstat("CRIT DMG");
        SubStats sub = new SubStats();
        sub.setKey("CRIT Rate_");
        sub.setValue(10.3);
        sub.setCount(1);
        sub.setStep(1);
        request.setSubstats(List.of(sub));
        request.setUid("relic-new");

        Relic relic = loadoutService.addRelic("u1", request);

        assertThat(relic.getUid()).isEqualTo("relic-new");
        assertThat(relic.getMainstat()).isEqualTo("CRIT DMG");
        assertThat(relic.getSubstats()).hasSize(1);
        assertThat(relic.getSubstats().get(0).getValue()).isEqualTo(10.3);
    }

    @Test
    void addRelicUnknownSetFails() {
        CreateRelicRequest request = new CreateRelicRequest();
        request.setSetId("999");
        request.setSlot(Slot.Body);
        request.setMainstat("CRIT DMG");
        assertThatThrownBy(() -> loadoutService.addRelic("u1", request))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void addRelicUnknownMainstatFails() {
        CreateRelicRequest request = new CreateRelicRequest();
        request.setSetId("129");
        request.setSlot(Slot.Body);
        request.setMainstat("NOPE");
        assertThatThrownBy(() -> loadoutService.addRelic("u1", request))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void addRelicUnknownSubstatFails() {
        CreateRelicRequest request = new CreateRelicRequest();
        request.setSetId("129");
        request.setSlot(Slot.Body);
        request.setMainstat("CRIT DMG");
        SubStats sub = new SubStats();
        sub.setKey("NOPE");
        sub.setValue(1.0);
        request.setSubstats(List.of(sub));
        assertThatThrownBy(() -> loadoutService.addRelic("u1", request))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void addRelicWithLocationConflictFails() {
        CreateRelicRequest existing = new CreateRelicRequest();
        existing.setSetId("129");
        existing.setSlot(Slot.Body);
        existing.setMainstat("CRIT DMG");
        existing.setLocation("1308");
        loadoutService.addRelic("u1", existing);

        CreateRelicRequest conflict = new CreateRelicRequest();
        conflict.setSetId("130");
        conflict.setSlot(Slot.Body);
        conflict.setMainstat("CRIT Rate");
        conflict.setLocation("1308");
        assertThatThrownBy(() -> loadoutService.addRelic("u1", conflict))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("already has");
    }
}
