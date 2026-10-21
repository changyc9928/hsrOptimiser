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
 * Relic level must not be used as an eligibility filter. A level-0 relic has the same main stat
 * and its already-rolled substats; it is simply weaker than an enhanced copy. The optimizer is
 * expected to compare them through damage and pick the better one, not to refuse the candidate.
 */
@ExtendWith(MockitoExtension.class)
class RelicLevelEligibilityTest {

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
    @DisplayName("availability check ignores relic level")
    void isAvailable_doesNotFilterByLevel() {
        assertThat(RelicAvailabilityHelper.isAvailable(relic(Slot.Head, "A", 0, ""), Set.of(),
            Set.of())).isTrue();
        assertThat(RelicAvailabilityHelper.isAvailable(relic(Slot.Head, "A", 15, ""), Set.of(),
            Set.of())).isTrue();
        assertThat(RelicAvailabilityHelper.isAvailable(relic(Slot.Head, "A", 0, OTHER_CHARACTER),
            Set.of(CHARACTER_ID), Set.of())).isFalse();
    }

    @Test
    @DisplayName("Cavern set mutation can equip level-0 candidates")
    void cavernSetMutation_acceptsUnenhancedRelics() {
        ScannedData scannedData = data(
            relic(Slot.Head, "A", 0, ""), relic(Slot.Head, "B", 15, ""),
            relic(Slot.Hands, "A", 0, ""), relic(Slot.Hands, "B", 15, ""),
            relic(Slot.Body, "A", 0, ""), relic(Slot.Body, "B", 15, ""),
            relic(Slot.Feet, "A", 0, ""), relic(Slot.Feet, "B", 15, ""));

        new CavernSetMutationStrategy().mutate(scannedData,
            new MutationContext(CHARACTER_ID, 0, Set.of(), Set.of(), random));

        assertThat(scannedData.getRelics())
            .filteredOn(r -> CHARACTER_ID.equals(r.getLocation()))
            .hasSize(4)
            .allMatch(r -> r.getSetId().equals("A"))
            .allMatch(r -> r.getLevel() == 0);
    }

    @Test
    @DisplayName("Existing pair mutation can equip level-0 candidates")
    void existingPairMutation_acceptsUnenhancedRelics() {
        ScannedData scannedData = data(
            relic(Slot.Head, "OLD", 15, CHARACTER_ID), relic(Slot.Hands, "OLD", 15, CHARACTER_ID),
            relic(Slot.Head, "NEW", 0, ""), relic(Slot.Hands, "NEW", 0, ""));

        new ExistingPairMutationStrategy().mutate(scannedData,
            new MutationContext(CHARACTER_ID, 0, Set.of(), Set.of(), random));

        assertThat(scannedData.getRelics())
            .filteredOn(r -> CHARACTER_ID.equals(r.getLocation()))
            .hasSize(2)
            .allMatch(r -> r.getSetId().equals("NEW"))
            .allMatch(r -> r.getLevel() == 0);
    }

    @Test
    @DisplayName("Planar pair mutation can equip level-0 candidates")
    void planarPairMutation_acceptsUnenhancedRelics() {
        // Only set 300 forms a complete pair, so it is the only legal choice. If level were
        // used as a filter, the mutation would find nothing and leave the character unchanged.
        ScannedData scannedData = data(
            relic(Slot.PlanarSphere, "300", 0, ""), relic(Slot.LinkRope, "300", 0, ""),
            relic(Slot.PlanarSphere, "301", 15, ""));

        new PlanarPairMutationStrategy().mutate(scannedData,
            new MutationContext(CHARACTER_ID, 0, Set.of(), Set.of(), random));

        assertThat(scannedData.getRelics())
            .filteredOn(r -> CHARACTER_ID.equals(r.getLocation()))
            .hasSize(2)
            .allMatch(r -> r.getLevel() == 0);
    }

    @Test
    @DisplayName("Single relic mutation can swap in a level-0 relic")
    void singleRelicMutation_acceptsUnenhancedRelic() {
        ScannedData scannedData = data(
            relic(Slot.Head, "A", 15, CHARACTER_ID),
            relic(Slot.Head, "A", 0, ""));

        new SingleRelicMutationStrategy().mutate(scannedData,
            new MutationContext(CHARACTER_ID, 0, Set.of(), Set.of(), random));

        assertThat(scannedData.getRelics())
            .filteredOn(r -> CHARACTER_ID.equals(r.getLocation()))
            .hasSize(1)
            .allMatch(r -> r.getLevel() == 0);
    }
}
