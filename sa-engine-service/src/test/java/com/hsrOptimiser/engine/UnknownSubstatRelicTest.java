package com.hsrOptimiser.engine;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.lenient;

import com.hsrOptimiser.DTO.hsrScanner.Relic;
import com.hsrOptimiser.DTO.hsrScanner.ScannedData;
import com.hsrOptimiser.DTO.hsrScanner.Slot;
import com.hsrOptimiser.engine.strategies.CavernSetMutationStrategy;
import com.hsrOptimiser.engine.strategies.ExistingPairMutationStrategy;
import com.hsrOptimiser.engine.strategies.PlanarPairMutationStrategy;
import com.hsrOptimiser.engine.strategies.SingleRelicMutationStrategy;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Scanner exports contain far more unenhanced relics than enhanced ones, and their substats are
 * only partially readable. The optimizer must never equip one, otherwise it hands back builds that
 * cannot exist in game.
 */
@ExtendWith(MockitoExtension.class)
class UnknownSubstatRelicTest {

    private static final String CHARACTER_ID = "1502";
    private static final String OTHER_CHARACTER = "8010";

    @Mock
    private Random random;

    @BeforeEach
    void setUp() {
        lenient().when(random.nextInt(anyInt())).thenReturn(0);
    }

    private static Relic relic(Slot slot, String setId, int level, String location) {
        Relic relic = new Relic();
        relic.setUid(slot + "-" + setId + "-" + level);
        relic.setSlot(slot);
        relic.setSetId(setId);
        relic.setLevel(level);
        relic.setRarity(5);
        relic.setMainstat("ATK");
        relic.setLocation(location);
        return relic;
    }

    private static ScannedData data(Relic... relics) {
        ScannedData scannedData = new ScannedData();
        scannedData.setRelics(new ArrayList<>(List.of(relics)));
        return scannedData;
    }

    @Test
    @DisplayName("hasKnownStats only accepts enhanced relics")
    void hasKnownStats_rejectsUnenhancedRelics() {
        assertThat(RelicAvailabilityHelper.hasKnownStats(relic(Slot.Head, "A", 0, ""))).isFalse();
        assertThat(RelicAvailabilityHelper.hasKnownStats(relic(Slot.Head, "A", 1, ""))).isTrue();
        assertThat(RelicAvailabilityHelper.hasKnownStats(relic(Slot.Head, "A", 15, ""))).isTrue();
    }

    @Test
    @DisplayName("isOptimizable combines availability with readable stats")
    void isOptimizable_requiresBoth() {
        assertThat(RelicAvailabilityHelper.isOptimizable(relic(Slot.Head, "A", 0, ""), Set.of(),
            Set.of())).isFalse();
        assertThat(RelicAvailabilityHelper.isOptimizable(relic(Slot.Head, "A", 15, ""), Set.of(),
            Set.of())).isTrue();
        assertThat(RelicAvailabilityHelper.isOptimizable(relic(Slot.Head, "A", 15, OTHER_CHARACTER),
            Set.of(CHARACTER_ID), Set.of())).isFalse();
    }

    @Test
    @DisplayName("Cavern set mutation skips unenhanced candidates")
    void cavernSetMutation_skipsUnenhancedRelics() {
        ScannedData scannedData = data(
            relic(Slot.Head, "A", 15, ""), relic(Slot.Head, "B", 0, ""),
            relic(Slot.Hands, "A", 15, ""), relic(Slot.Hands, "B", 0, ""),
            relic(Slot.Body, "A", 15, ""), relic(Slot.Body, "B", 0, ""),
            relic(Slot.Feet, "A", 15, ""), relic(Slot.Feet, "B", 0, ""));

        new CavernSetMutationStrategy().mutate(scannedData,
            new MutationContext(CHARACTER_ID, 0, Set.of(), Set.of(), random));

        assertThat(scannedData.getRelics())
            .filteredOn(r -> CHARACTER_ID.equals(r.getLocation()))
            .hasSize(4)
            .allMatch(r -> r.getLevel() > 0);
    }

    @Test
    @DisplayName("Existing pair mutation skips unenhanced candidates")
    void existingPairMutation_skipsUnenhancedRelics() {
        ScannedData scannedData = data(
            relic(Slot.Head, "OLD", 15, CHARACTER_ID), relic(Slot.Hands, "OLD", 15, CHARACTER_ID),
            relic(Slot.Head, "NEW", 15, ""), relic(Slot.Head, "NEW", 0, ""),
            relic(Slot.Hands, "NEW", 15, ""), relic(Slot.Hands, "NEW", 0, ""));

        new ExistingPairMutationStrategy().mutate(scannedData,
            new MutationContext(CHARACTER_ID, 0, Set.of(), Set.of(), random));

        assertThat(scannedData.getRelics())
            .filteredOn(r -> CHARACTER_ID.equals(r.getLocation()))
            .hasSize(2)
            .allMatch(r -> r.getSetId().equals("NEW"))
            .allMatch(r -> r.getLevel() > 0);
    }

    @Test
    @DisplayName("Planar pair mutation skips unenhanced candidates")
    void planarPairMutation_skipsUnenhancedRelics() {
        ScannedData scannedData = data(
            relic(Slot.PlanarSphere, "300", 0, ""), relic(Slot.LinkRope, "300", 0, ""),
            relic(Slot.PlanarSphere, "301", 15, ""), relic(Slot.LinkRope, "301", 15, ""));

        new PlanarPairMutationStrategy().mutate(scannedData,
            new MutationContext(CHARACTER_ID, 0, Set.of(), Set.of(), random));

        assertThat(scannedData.getRelics())
            .filteredOn(r -> CHARACTER_ID.equals(r.getLocation()))
            .hasSize(2)
            .allMatch(r -> r.getLevel() > 0);
    }

    @Test
    @DisplayName("Single relic mutation never swaps in an unenhanced relic")
    void singleRelicMutation_skipsUnenhancedRelics() {
        ScannedData scannedData = data(
            relic(Slot.Head, "A", 15, CHARACTER_ID),
            relic(Slot.Head, "A", 0, ""),
            relic(Slot.Head, "B", 15, ""));

        new SingleRelicMutationStrategy().mutate(scannedData,
            new MutationContext(CHARACTER_ID, 0, Set.of(), Set.of(), random));

        assertThat(scannedData.getRelics())
            .filteredOn(r -> CHARACTER_ID.equals(r.getLocation()))
            .hasSize(1)
            .allMatch(r -> r.getLevel() > 0);
    }
}