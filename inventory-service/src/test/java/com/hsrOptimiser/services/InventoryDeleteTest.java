package com.hsrOptimiser.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.hsrOptimiser.DTO.hsrScanner.HSRCharacter;
import com.hsrOptimiser.DTO.hsrScanner.LightCone;
import com.hsrOptimiser.DTO.hsrScanner.Relic;
import com.hsrOptimiser.DTO.hsrScanner.ScannedData;
import com.hsrOptimiser.DTO.hsrScanner.Slot;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class InventoryDeleteTest {

    private static final String USER = "u1";
    private static final String CHARACTER = "1308";

    private Memory memory;
    private TeamService teamService;
    private LoadoutService loadoutService;

    private static HSRCharacter character(String id) {
        HSRCharacter character = new HSRCharacter();
        character.setId(id);
        character.setName("Character " + id);
        return character;
    }

    private static Relic relic(String uid, String location) {
        Relic relic = new Relic();
        relic.setUid(uid);
        relic.setSlot(Slot.Head);
        relic.setRarity(5);
        relic.setLocation(location);
        return relic;
    }

    private static LightCone cone(String uid, String location) {
        LightCone cone = new LightCone();
        cone.setUid(uid);
        cone.setLocation(location);
        return cone;
    }

    @BeforeEach
    void setUp() {
        memory = new MemoryImpl();
        teamService = new TeamServiceImpl(memory);
        loadoutService = new LoadoutServiceImpl(memory, teamService);

        ScannedData data = new ScannedData();
        data.setCharacters(new ArrayList<>(List.of(character(CHARACTER), character("1310"))));
        data.setRelics(new ArrayList<>(List.of(
            relic("r1", CHARACTER), relic("r2", CHARACTER), relic("r3", ""))));
        data.setLightCones(new ArrayList<>(List.of(
            cone("c1", CHARACTER), cone("c2", ""))));
        memory.insertMemory(USER, data);
        teamService.saveTeam(USER, List.of(CHARACTER, "1310"));
    }

    @Test
    @DisplayName("deleting a relic removes it from the inventory")
    void deleteRelic_removesRelic() {
        loadoutService.deleteRelic(USER, "r3");

        assertThat(memory.getMemory(USER).getRelics())
            .extracting(Relic::getUid)
            .containsExactly("r1", "r2");
    }

    @Test
    @DisplayName("deleting a light cone removes it from the inventory")
    void deleteLightCone_removesCone() {
        loadoutService.deleteLightCone(USER, "c2");

        assertThat(memory.getMemory(USER).getLightCones())
            .extracting(LightCone::getUid)
            .containsExactly("c1");
    }

    @Test
    @DisplayName("deleting a character also unequips their gear and drops them from the team")
    void deleteCharacter_cleansUpLoadoutAndTeam() {
        loadoutService.deleteCharacter(USER, CHARACTER);

        ScannedData data = memory.getMemory(USER);
        assertThat(data.getCharacters()).extracting(HSRCharacter::getId)
            .containsExactly("1310");
        assertThat(data.getRelics()).filteredOn(r -> CHARACTER.equals(r.getLocation())).isEmpty();
        assertThat(data.getLightCones()).filteredOn(c -> CHARACTER.equals(c.getLocation())).isEmpty();
        assertThat(teamService.getTeam(USER)).extracting(HSRCharacter::getId)
            .containsExactly("1310");
    }

    @Test
    @DisplayName("deleting an unknown entity fails")
    void deleteUnknown_throws() {
        assertThatThrownBy(() -> loadoutService.deleteRelic(USER, "missing"))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> loadoutService.deleteLightCone(USER, "missing"))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> loadoutService.deleteCharacter(USER, "missing"))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
