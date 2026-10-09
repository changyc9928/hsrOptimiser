package com.hsrOptimiser.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.hsrOptimiser.DTO.LoadoutDTO;
import com.hsrOptimiser.DTO.hsrScanner.HSRCharacter;
import com.hsrOptimiser.DTO.hsrScanner.LightCone;
import com.hsrOptimiser.DTO.hsrScanner.Relic;
import com.hsrOptimiser.DTO.hsrScanner.ScannedData;
import com.hsrOptimiser.DTO.hsrScanner.Slot;
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
