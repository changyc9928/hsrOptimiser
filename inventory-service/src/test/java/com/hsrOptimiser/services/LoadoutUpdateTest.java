package com.hsrOptimiser.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.hsrOptimiser.DTO.UpdateCharacterRequest;
import com.hsrOptimiser.DTO.UpdateLightConeRequest;
import com.hsrOptimiser.DTO.UpdateRelicRequest;
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

class LoadoutUpdateTest {

    private Memory memory;
    private LoadoutService loadoutService;

    @BeforeEach
    void setUp() {
        memory = new MemoryImpl();
        loadoutService = new LoadoutServiceImpl(memory);

        ScannedData data = new ScannedData();
        HSRCharacter character = new HSRCharacter();
        character.setId("1308");
        character.setName("Acheron");
        character.setLevel(70);
        character.setEidolon(0);
        character.setSkills(new CharacterSkills());
        data.setCharacters(new ArrayList<>(List.of(character)));

        LightCone cone = new LightCone();
        cone.setUid("lc1");
        cone.setId("23000");
        cone.setLevel(1);
        cone.setSuperimposition(1);
        data.setLightCones(new ArrayList<>(List.of(cone)));

        Relic relic = new Relic();
        relic.setUid("r1");
        relic.setSetId("102");
        relic.setSlot(Slot.Body);
        relic.setRarity(5);
        relic.setLevel(0);
        relic.setMainstat("ATK");
        relic.setSubstats(new ArrayList<>());
        data.setRelics(new ArrayList<>(List.of(relic)));

        memory.insertMemory("u1", data);
    }

    @Test
    void updateCharacterLevelEidolonAndSkills() {
        UpdateCharacterRequest request = new UpdateCharacterRequest();
        request.setLevel(80);
        request.setEidolon(2);
        CharacterSkills skills = new CharacterSkills();
        skills.setBasic(6);
        skills.setSkill(10);
        skills.setUlt(10);
        skills.setTalent(10);
        request.setSkills(skills);

        HSRCharacter updated = loadoutService.updateCharacter("u1", "1308", request);

        assertThat(updated.getLevel()).isEqualTo(80);
        assertThat(updated.getEidolon()).isEqualTo(2);
        assertThat(updated.getSkills().getSkill()).isEqualTo(10);
        assertThat(updated.getName()).isEqualTo("Acheron");
    }

    @Test
    void updateCharacterBadEidolonFails() {
        UpdateCharacterRequest request = new UpdateCharacterRequest();
        request.setEidolon(7);
        assertThatThrownBy(() -> loadoutService.updateCharacter("u1", "1308", request))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void updateCharacterUnknownIdFails() {
        assertThatThrownBy(
            () -> loadoutService.updateCharacter("u1", "9999", new UpdateCharacterRequest()))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void updateLightConeLevelAndSuperimposition() {
        UpdateLightConeRequest request = new UpdateLightConeRequest();
        request.setLevel(80);
        request.setSuperimposition(5);
        request.setLock(true);

        LightCone updated = loadoutService.updateLightCone("u1", "lc1", request);

        assertThat(updated.getLevel()).isEqualTo(80);
        assertThat(updated.getSuperimposition()).isEqualTo(5);
        assertThat(updated.isLock()).isTrue();
    }

    @Test
    void updateLightConeIdSwapsIdentity() {
        UpdateLightConeRequest request = new UpdateLightConeRequest();
        request.setId("23001");

        LightCone updated = loadoutService.updateLightCone("u1", "lc1", request);

        assertThat(updated.getId()).isEqualTo("23001");
        assertThat(updated.getName()).isEqualTo("InTheNight");
    }

    @Test
    void updateLightConeUnknownIdFails() {
        UpdateLightConeRequest request = new UpdateLightConeRequest();
        request.setId("99999");
        assertThatThrownBy(() -> loadoutService.updateLightCone("u1", "lc1", request))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void updateLightConeBadSuperimpositionFails() {
        UpdateLightConeRequest request = new UpdateLightConeRequest();
        request.setSuperimposition(6);
        assertThatThrownBy(() -> loadoutService.updateLightCone("u1", "lc1", request))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void updateRelicLevelMainstatAndSubstats() {
        UpdateRelicRequest request = new UpdateRelicRequest();
        request.setLevel(15);
        request.setMainstat("CRIT DMG");
        SubStats sub = new SubStats();
        sub.setKey("CRIT Rate_");
        sub.setValue(10.3);
        request.setSubstats(List.of(sub));

        Relic updated = loadoutService.updateRelic("u1", "r1", request);

        assertThat(updated.getLevel()).isEqualTo(15);
        assertThat(updated.getMainstat()).isEqualTo("CRIT DMG");
        assertThat(updated.getSubstats()).hasSize(1);
    }

    @Test
    void updateRelicUnknownSubstatFails() {
        UpdateRelicRequest request = new UpdateRelicRequest();
        SubStats sub = new SubStats();
        sub.setKey("Nope");
        sub.setValue(1);
        request.setSubstats(List.of(sub));
        assertThatThrownBy(() -> loadoutService.updateRelic("u1", "r1", request))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
