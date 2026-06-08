package com.hsrOptimiser.engine;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.lenient;

import com.hsrOptimiser.DTO.hsrScanner.Relic;
import com.hsrOptimiser.DTO.hsrScanner.ScannedData;
import com.hsrOptimiser.DTO.hsrScanner.Slot;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SimulatedAnnealingMutateCavernSetsTest {

    private static final String CHARACTER_ID = "1001";

    @Mock
    private Random random;

    @BeforeEach
    void setUp() {
        lenient().when(random.nextInt(anyInt())).thenReturn(0);
    }

    private Relic relic(
        String setId,
        Slot slot,
        String location
    ) {
        Relic relic = new Relic();
        relic.setSetId(setId);
        relic.setSlot(slot);
        relic.setLocation(location);
        relic.setRarity(5);
        return relic;
    }

    /**
     * Creates a pool where EVERY slot has both set A and set B.
     * <p>
     * This guarantees that regardless of shuffle order: Head+Body Hands+Feet Head+Feet Body+Hands
     * etc...
     * <p>
     * findMatchingPair() can always succeed.
     */
    private List<Relic> fullyMatchablePool() {

        return List.of(
            relic("A", Slot.Head, ""),
            relic("B", Slot.Head, ""),

            relic("A", Slot.Hands, ""),
            relic("B", Slot.Hands, ""),

            relic("A", Slot.Body, ""),
            relic("B", Slot.Body, ""),

            relic("A", Slot.Feet, ""),
            relic("B", Slot.Feet, "")
        );
    }

    @Test
    void shouldEquipFourRelicsWhenTwoMatchingPairsExist() {

        ScannedData data = new ScannedData();
        data.setRelics(new ArrayList<>(fullyMatchablePool()));

        SimulatedAnnealing.mutateCavernSets(
            data,
            CHARACTER_ID,
            List.of(Slot.Head, Slot.Hands, Slot.Body, Slot.Feet),
            List.of(),
            Set.of(),
            Set.of(),
            random
        );

        long equippedCount =
            data.getRelics().stream()
                .filter(r -> CHARACTER_ID.equals(r.getLocation()))
                .count();

        assertThat(equippedCount).isEqualTo(4);
    }

    @Test
    void shouldClearExistingCavernRelicsBeforeEquippingNewOnes() {

        Relic oldHead = relic("OLD", Slot.Head, CHARACTER_ID);
        Relic oldHands = relic("OLD", Slot.Hands, CHARACTER_ID);
        Relic oldBody = relic("OLD2", Slot.Body, CHARACTER_ID);
        Relic oldFeet = relic("OLD2", Slot.Feet, CHARACTER_ID);

        List<Relic> relics = new ArrayList<>();

        relics.add(oldHead);
        relics.add(oldHands);
        relics.add(oldBody);
        relics.add(oldFeet);

        relics.addAll(fullyMatchablePool());

        ScannedData data = new ScannedData();
        data.setRelics(relics);

        SimulatedAnnealing.mutateCavernSets(
            data,
            CHARACTER_ID,
            List.of(Slot.Head, Slot.Hands, Slot.Body, Slot.Feet),
            List.of(oldHead, oldHands, oldBody, oldFeet),
            Set.of(),
            Set.of(),
            random
        );

        assertThat(oldHead.getLocation()).isEmpty();
        assertThat(oldHands.getLocation()).isEmpty();
        assertThat(oldBody.getLocation()).isEmpty();
        assertThat(oldFeet.getLocation()).isEmpty();

        long equippedCount =
            relics.stream()
                .filter(r -> CHARACTER_ID.equals(r.getLocation()))
                .count();

        assertThat(equippedCount).isEqualTo(4);
    }

    @Test
    void shouldReturnWhenOneSlotHasNoAvailableRelics() {

        Relic head = relic("A", Slot.Head, "");
        Relic hands = relic("A", Slot.Hands, "");
        Relic body = relic("A", Slot.Body, "");

        ScannedData data = new ScannedData();
        data.setRelics(List.of(head, hands, body));

        SimulatedAnnealing.mutateCavernSets(
            data,
            CHARACTER_ID,
            List.of(Slot.Head, Slot.Hands, Slot.Body, Slot.Feet),
            List.of(),
            Set.of(),
            Set.of(),
            random
        );

        assertThat(
            data.getRelics().stream()
                .noneMatch(r -> CHARACTER_ID.equals(r.getLocation()))
        ).isTrue();
    }

    @Test
    void shouldIgnoreNonFiveStarRelics() {

        Relic head = relic("A", Slot.Head, "");
        head.setRarity(4);

        Relic hands = relic("A", Slot.Hands, "");
        Relic body = relic("A", Slot.Body, "");
        Relic feet = relic("A", Slot.Feet, "");

        ScannedData data = new ScannedData();
        data.setRelics(List.of(head, hands, body, feet));

        SimulatedAnnealing.mutateCavernSets(
            data,
            CHARACTER_ID,
            List.of(Slot.Head, Slot.Hands, Slot.Body, Slot.Feet),
            List.of(),
            Set.of(),
            Set.of(),
            random
        );

        assertThat(
            data.getRelics().stream()
                .noneMatch(r -> CHARACTER_ID.equals(r.getLocation()))
        ).isTrue();
    }

    @Test
    void shouldAllowRelicsOwnedByAllowedCharacters() {

        List<Relic> relics = new ArrayList<>();

        relics.add(relic("A", Slot.Head, "OTHER"));
        relics.add(relic("B", Slot.Head, "OTHER"));

        relics.add(relic("A", Slot.Hands, "OTHER"));
        relics.add(relic("B", Slot.Hands, "OTHER"));

        relics.add(relic("A", Slot.Body, ""));
        relics.add(relic("B", Slot.Body, ""));

        relics.add(relic("A", Slot.Feet, ""));
        relics.add(relic("B", Slot.Feet, ""));

        ScannedData data = new ScannedData();
        data.setRelics(relics);

        SimulatedAnnealing.mutateCavernSets(
            data,
            CHARACTER_ID,
            List.of(Slot.Head, Slot.Hands, Slot.Body, Slot.Feet),
            List.of(),
            Set.of("OTHER"),
            Set.of(),
            random
        );

        long equippedCount =
            data.getRelics().stream()
                .filter(r -> CHARACTER_ID.equals(r.getLocation()))
                .count();

        assertThat(equippedCount).isEqualTo(4);
    }

    @Test
    void shouldRejectRelicsOwnedByDisallowedCharacters() {

        List<Relic> relics = new ArrayList<>();

        relics.add(relic("A", Slot.Head, "OTHER"));
        relics.add(relic("A", Slot.Hands, "OTHER"));

        relics.add(relic("B", Slot.Body, ""));
        relics.add(relic("B", Slot.Feet, ""));

        ScannedData data = new ScannedData();
        data.setRelics(relics);

        SimulatedAnnealing.mutateCavernSets(
            data,
            CHARACTER_ID,
            List.of(Slot.Head, Slot.Hands, Slot.Body, Slot.Feet),
            List.of(),
            Set.of("OTHER"),
            Set.of("OTHER"),
            random
        );

        assertThat(
            data.getRelics().stream()
                .noneMatch(r -> CHARACTER_ID.equals(r.getLocation()))
        ).isTrue();

        assertThat(relics.get(0).getLocation()).isEqualTo("OTHER");
        assertThat(relics.get(1).getLocation()).isEqualTo("OTHER");
    }

    @Test
    void shouldReturnWhenNoMatchingSetsExist() {

        ScannedData data = new ScannedData();

        data.setRelics(List.of(
            relic("A", Slot.Head, ""),
            relic("B", Slot.Hands, ""),
            relic("C", Slot.Body, ""),
            relic("D", Slot.Feet, "")
        ));

        SimulatedAnnealing.mutateCavernSets(
            data,
            CHARACTER_ID,
            List.of(Slot.Head, Slot.Hands, Slot.Body, Slot.Feet),
            List.of(),
            Set.of(),
            Set.of(),
            random
        );

        assertThat(
            data.getRelics().stream()
                .noneMatch(r -> CHARACTER_ID.equals(r.getLocation()))
        ).isTrue();
    }

    @Test
    void shouldReturnWhenFirstPairExistsButSecondPairDoesNot() {

        ScannedData data = new ScannedData();

        data.setRelics(List.of(
            relic("A", Slot.Head, ""),
            relic("A", Slot.Hands, ""),
            relic("B", Slot.Body, ""),
            relic("C", Slot.Feet, "")
        ));

        SimulatedAnnealing.mutateCavernSets(
            data,
            CHARACTER_ID,
            List.of(Slot.Head, Slot.Hands, Slot.Body, Slot.Feet),
            List.of(),
            Set.of(),
            Set.of(),
            random
        );

        assertThat(
            data.getRelics().stream()
                .noneMatch(r -> CHARACTER_ID.equals(r.getLocation()))
        ).isTrue();
    }

    @Test
    void shouldDoNothingWhenOnlyCurrentRelicsExist() {

        Relic head = relic("A", Slot.Head, CHARACTER_ID);
        Relic hands = relic("A", Slot.Hands, CHARACTER_ID);
        Relic body = relic("B", Slot.Body, CHARACTER_ID);
        Relic feet = relic("B", Slot.Feet, CHARACTER_ID);

        ScannedData data = new ScannedData();
        data.setRelics(List.of(head, hands, body, feet));

        SimulatedAnnealing.mutateCavernSets(
            data,
            CHARACTER_ID,
            List.of(Slot.Head, Slot.Hands, Slot.Body, Slot.Feet),
            List.of(head, hands, body, feet),
            Set.of(),
            Set.of(),
            random
        );

        assertThat(head.getLocation()).isEqualTo(CHARACTER_ID);
        assertThat(hands.getLocation()).isEqualTo(CHARACTER_ID);
        assertThat(body.getLocation()).isEqualTo(CHARACTER_ID);
        assertThat(feet.getLocation()).isEqualTo(CHARACTER_ID);
    }

    @Test
    void shouldAllowFourPieceSetWhenAllSlotsMatchSameSet() {

        ScannedData data = new ScannedData();

        Relic head = relic("SET_A", Slot.Head, "");
        Relic hands = relic("SET_A", Slot.Hands, "");
        Relic body = relic("SET_A", Slot.Body, "");
        Relic feet = relic("SET_A", Slot.Feet, "");

        data.setRelics(List.of(head, hands, body, feet));

        SimulatedAnnealing.mutateCavernSets(
            data,
            CHARACTER_ID,
            List.of(Slot.Head, Slot.Hands, Slot.Body, Slot.Feet),
            List.of(),
            Set.of(),
            Set.of(),
            random
        );

        long equipped =
            data.getRelics().stream()
                .filter(r -> CHARACTER_ID.equals(r.getLocation()))
                .count();

        assertThat(equipped).isEqualTo(4);

        assertThat(head.getLocation()).isEqualTo(CHARACTER_ID);
        assertThat(hands.getLocation()).isEqualTo(CHARACTER_ID);
        assertThat(body.getLocation()).isEqualTo(CHARACTER_ID);
        assertThat(feet.getLocation()).isEqualTo(CHARACTER_ID);
    }
}