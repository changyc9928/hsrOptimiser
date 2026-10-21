package com.hsrOptimiser.engine;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.lenient;

import com.hsrOptimiser.DTO.hsrScanner.Relic;
import com.hsrOptimiser.DTO.hsrScanner.ScannedData;
import com.hsrOptimiser.DTO.hsrScanner.Slot;
import com.hsrOptimiser.engine.strategies.CavernSetMutationStrategy;
import com.hsrOptimiser.engine.strategies.FullLoadoutMutationStrategy;
import com.hsrOptimiser.engine.strategies.PlanarPairMutationStrategy;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FullLoadoutMutationStrategyTest {

    private static final String CHARACTER_ID = "1308";

    @Mock
    private Random random;

    private FullLoadoutMutationStrategy strategy;

    @BeforeEach
    void setUp() {
        lenient().when(random.nextInt(anyInt())).thenReturn(0);
        strategy = new FullLoadoutMutationStrategy(
            new CavernSetMutationStrategy(), new PlanarPairMutationStrategy());
    }

    @Test
    void equipsAllSixSlotsForAnEmptyCharacter() {
        ScannedData data = new ScannedData();
        data.setRelics(new ArrayList<>(List.of(
            relic("A", Slot.Head), relic("B", Slot.Head),
            relic("A", Slot.Hands), relic("B", Slot.Hands),
            relic("A", Slot.Body), relic("B", Slot.Body),
            relic("A", Slot.Feet), relic("B", Slot.Feet),
            relic("P", Slot.PlanarSphere), relic("P", Slot.LinkRope))));

        strategy.mutate(data, new MutationContext(
            CHARACTER_ID, 0, Set.of(), Set.of(), random));

        List<Relic> equipped = data.getRelics().stream()
            .filter(r -> CHARACTER_ID.equals(r.getLocation()))
            .toList();
        assertThat(equipped).hasSize(6);
        assertThat(EnumSet.allOf(Slot.class))
            .allMatch(slot -> equipped.stream().anyMatch(r -> r.getSlot() == slot));
    }

    private static Relic relic(String setId, Slot slot) {
        Relic relic = new Relic();
        relic.setSetId(setId);
        relic.setSlot(slot);
        relic.setLocation("");
        relic.setRarity(5);
        relic.setLevel(15);
        relic.setMainstat(slot == Slot.PlanarSphere ? "ATK" : "HP");
        return relic;
    }
}
