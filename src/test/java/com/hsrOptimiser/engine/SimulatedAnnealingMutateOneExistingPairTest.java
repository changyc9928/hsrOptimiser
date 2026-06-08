package com.hsrOptimiser.engine;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.lenient;

import com.hsrOptimiser.DTO.hsrScanner.Relic;
import com.hsrOptimiser.DTO.hsrScanner.ScannedData;
import com.hsrOptimiser.DTO.hsrScanner.Slot;
import java.lang.reflect.Method;
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
class SimulatedAnnealingMutateOneExistingPairTest {

    private static final String CHARACTER_ID = "1001";

    @Mock
    private Random random;

    private static void invokeMutateOneExistingPair(
        ScannedData data,
        List<Slot> slots,
        List<Relic> currentEquipped,
        Set<String> allowed,
        Set<String> disallowed,
        Random random) throws Exception {

        Method method = SimulatedAnnealing.class.getDeclaredMethod(
            "mutateOneExistingPair",
            ScannedData.class,
            String.class,
            List.class,
            List.class,
            Set.class,
            Set.class,
            Random.class);

        method.setAccessible(true);

        method.invoke(
            null,
            data,
            SimulatedAnnealingMutateOneExistingPairTest.CHARACTER_ID,
            slots,
            currentEquipped,
            allowed,
            disallowed,
            random);
    }

    private static Relic relic(
        String id,
        Slot slot,
        String setId,
        String location) {

        Relic relic = new Relic();

        relic.setUid(id);
        relic.setSlot(slot);
        relic.setSetId(setId);
        relic.setLocation(location);
        relic.setRarity(5);

        return relic;
    }

    @BeforeEach
    void setUp() {
        lenient().when(random.nextInt(anyInt())).thenReturn(0);
    }

    @Test
    void shouldReplaceExistingPairWithNewSet() throws Exception {

        Relic headEquipped = relic(
            "old-head",
            Slot.Head,
            "SET_A",
            CHARACTER_ID
        );

        Relic handsEquipped = relic(
            "old-hands",
            Slot.Hands,
            "SET_A",
            CHARACTER_ID
        );

        Relic bodyEquipped = relic(
            "body",
            Slot.Body,
            "SET_X",
            CHARACTER_ID
        );

        Relic feetEquipped = relic(
            "feet",
            Slot.Feet,
            "SET_Y",
            CHARACTER_ID
        );

        Relic replacementHead = relic(
            "new-head",
            Slot.Head,
            "SET_B",
            ""
        );

        Relic replacementHands = relic(
            "new-hands",
            Slot.Hands,
            "SET_B",
            ""
        );

        ScannedData data = new ScannedData();
        data.setRelics(new ArrayList<>(List.of(
            headEquipped,
            handsEquipped,
            bodyEquipped,
            feetEquipped,
            replacementHead,
            replacementHands
        )));

        List<Relic> currentEquipped = List.of(
            headEquipped,
            handsEquipped,
            bodyEquipped,
            feetEquipped
        );

        invokeMutateOneExistingPair(
            data,
            List.of(
                Slot.Head,
                Slot.Hands,
                Slot.Body,
                Slot.Feet),
            currentEquipped,
            Set.of(),
            Set.of(),
            random);

        assertThat(headEquipped.getLocation()).isEmpty();
        assertThat(handsEquipped.getLocation()).isEmpty();

        assertThat(replacementHead.getLocation())
            .isEqualTo(CHARACTER_ID);

        assertThat(replacementHands.getLocation())
            .isEqualTo(CHARACTER_ID);

        assertThat(bodyEquipped.getLocation())
            .isEqualTo(CHARACTER_ID);

        assertThat(feetEquipped.getLocation())
            .isEqualTo(CHARACTER_ID);
    }

    @Test
    void shouldDoNothingWhenNoExistingPairFound() throws Exception {

        Relic head = relic(
            "head",
            Slot.Head,
            "SET_A",
            CHARACTER_ID
        );

        Relic hands = relic(
            "hands",
            Slot.Hands,
            "SET_B",
            CHARACTER_ID
        );

        ScannedData data = new ScannedData();
        data.setRelics(new ArrayList<>(List.of(head, hands)));

        invokeMutateOneExistingPair(
            data,
            List.of(Slot.Head, Slot.Hands),
            List.of(head, hands),
            Set.of(),
            Set.of(),
            random);

        assertThat(head.getLocation())
            .isEqualTo(CHARACTER_ID);

        assertThat(hands.getLocation())
            .isEqualTo(CHARACTER_ID);
    }

    @Test
    void shouldDoNothingWhenReplacementPairDoesNotExist() throws Exception {

        Relic headEquipped = relic(
            "head",
            Slot.Head,
            "SET_A",
            CHARACTER_ID
        );

        Relic handsEquipped = relic(
            "hands",
            Slot.Hands,
            "SET_A",
            CHARACTER_ID
        );

        Relic availableHead = relic(
            "available-head",
            Slot.Head,
            "SET_B",
            ""
        );

        // No matching SET_B hands relic
        Relic availableHands = relic(
            "available-hands",
            Slot.Hands,
            "SET_C",
            ""
        );

        ScannedData data = new ScannedData();
        data.setRelics(new ArrayList<>(List.of(
            headEquipped,
            handsEquipped,
            availableHead,
            availableHands
        )));

        invokeMutateOneExistingPair(
            data,
            List.of(Slot.Head, Slot.Hands),
            List.of(headEquipped, handsEquipped),
            Set.of(),
            Set.of(),
            random);

        assertThat(headEquipped.getLocation())
            .isEqualTo(CHARACTER_ID);

        assertThat(handsEquipped.getLocation())
            .isEqualTo(CHARACTER_ID);

        assertThat(availableHead.getLocation()).isEmpty();
        assertThat(availableHands.getLocation()).isEmpty();
    }

    @Test
    void shouldIgnoreRelicsOwnedByDisallowedCharacters() throws Exception {

        Relic headEquipped = relic(
            "head",
            Slot.Head,
            "SET_A",
            CHARACTER_ID
        );

        Relic handsEquipped = relic(
            "hands",
            Slot.Hands,
            "SET_A",
            CHARACTER_ID
        );

        Relic replacementHead = relic(
            "replacement-head",
            Slot.Head,
            "SET_B",
            "DISALLOWED"
        );

        Relic replacementHands = relic(
            "replacement-hands",
            Slot.Hands,
            "SET_B",
            "DISALLOWED"
        );

        ScannedData data = new ScannedData();
        data.setRelics(new ArrayList<>(List.of(
            headEquipped,
            handsEquipped,
            replacementHead,
            replacementHands
        )));

        invokeMutateOneExistingPair(
            data,
            List.of(Slot.Head, Slot.Hands),
            List.of(headEquipped, handsEquipped),
            Set.of("DISALLOWED"),
            Set.of("DISALLOWED"),
            random);

        assertThat(headEquipped.getLocation())
            .isEqualTo(CHARACTER_ID);

        assertThat(handsEquipped.getLocation())
            .isEqualTo(CHARACTER_ID);

        assertThat(replacementHead.getLocation())
            .isEqualTo("DISALLOWED");

        assertThat(replacementHands.getLocation())
            .isEqualTo("DISALLOWED");
    }

    @Test
    void shouldRemainFullyUnchangedWhenReplacementPairCannotBeBuilt() throws Exception {

        Relic head = relic("head", Slot.Head, "SET_A", CHARACTER_ID);
        Relic hands = relic("hands", Slot.Hands, "SET_A", CHARACTER_ID);

        Relic candidateHead = relic(
            "candidateHead",
            Slot.Head,
            "SET_B",
            ""
        );

        Relic candidateHands = relic(
            "candidateHands",
            Slot.Hands,
            "SET_C",
            ""
        );

        ScannedData data = new ScannedData();
        data.setRelics(new ArrayList<>(List.of(
            head,
            hands,
            candidateHead,
            candidateHands
        )));

        invokeMutateOneExistingPair(
            data,
            List.of(Slot.Head, Slot.Hands),
            List.of(head, hands),
            Set.of(),
            Set.of(),
            random);

        assertThat(head.getLocation()).isEqualTo(CHARACTER_ID);
        assertThat(hands.getLocation()).isEqualTo(CHARACTER_ID);

        assertThat(candidateHead.getLocation()).isEmpty();
        assertThat(candidateHands.getLocation()).isEmpty();
    }

    @Test
    void shouldOnlyUnequipTheMutatedPair() throws Exception {

        Relic head = relic("head", Slot.Head, "SET_A", CHARACTER_ID);
        Relic hands = relic("hands", Slot.Hands, "SET_A", CHARACTER_ID);

        Relic body = relic("body", Slot.Body, "SET_X", CHARACTER_ID);
        Relic feet = relic("feet", Slot.Feet, "SET_Y", CHARACTER_ID);

        Relic replacementHead =
            relic("newHead", Slot.Head, "SET_B", "");

        Relic replacementHands =
            relic("newHands", Slot.Hands, "SET_B", "");

        ScannedData data = new ScannedData();
        data.setRelics(new ArrayList<>(List.of(
            head,
            hands,
            body,
            feet,
            replacementHead,
            replacementHands
        )));

        invokeMutateOneExistingPair(
            data,
            List.of(
                Slot.Head,
                Slot.Hands,
                Slot.Body,
                Slot.Feet),
            List.of(head, hands, body, feet),
            Set.of(),
            Set.of(),
            random);

        long equippedCount =
            data.getRelics().stream()
                .filter(r -> CHARACTER_ID.equals(r.getLocation()))
                .count();

        assertThat(equippedCount).isEqualTo(4);

        assertThat(body.getLocation()).isEqualTo(CHARACTER_ID);
        assertThat(feet.getLocation()).isEqualTo(CHARACTER_ID);
    }

    @Test
    void shouldHaveExactlyOneEquippedRelicPerSlotAfterMutation() throws Exception {

        Relic head = relic("head", Slot.Head, "SET_A", CHARACTER_ID);
        Relic hands = relic("hands", Slot.Hands, "SET_A", CHARACTER_ID);

        Relic replacementHead =
            relic("newHead", Slot.Head, "SET_B", "");

        Relic replacementHands =
            relic("newHands", Slot.Hands, "SET_B", "");

        ScannedData data = new ScannedData();
        data.setRelics(new ArrayList<>(List.of(
            head,
            hands,
            replacementHead,
            replacementHands
        )));

        invokeMutateOneExistingPair(
            data,
            List.of(Slot.Head, Slot.Hands),
            List.of(head, hands),
            Set.of(),
            Set.of(),
            random);

        assertThat(
            data.getRelics().stream()
                .filter(r -> CHARACTER_ID.equals(r.getLocation()))
                .filter(r -> r.getSlot() == Slot.Head)
                .count())
            .isEqualTo(1);

        assertThat(
            data.getRelics().stream()
                .filter(r -> CHARACTER_ID.equals(r.getLocation()))
                .filter(r -> r.getSlot() == Slot.Hands)
                .count())
            .isEqualTo(1);
    }

    @Test
    void shouldNotStealRelicsFromCharactersOutsideAllowedList() throws Exception {

        Relic head = relic("head", Slot.Head, "SET_A", CHARACTER_ID);
        Relic hands = relic("hands", Slot.Hands, "SET_A", CHARACTER_ID);

        Relic foreignHead =
            relic("foreignHead", Slot.Head, "SET_B", "OTHER");

        Relic foreignHands =
            relic("foreignHands", Slot.Hands, "SET_B", "OTHER");

        ScannedData data = new ScannedData();
        data.setRelics(new ArrayList<>(List.of(
            head,
            hands,
            foreignHead,
            foreignHands
        )));

        invokeMutateOneExistingPair(
            data,
            List.of(Slot.Head, Slot.Hands),
            List.of(head, hands),
            Set.of(),
            Set.of(),
            random);

        assertThat(head.getLocation()).isEqualTo(CHARACTER_ID);
        assertThat(hands.getLocation()).isEqualTo(CHARACTER_ID);

        assertThat(foreignHead.getLocation()).isEqualTo("OTHER");
        assertThat(foreignHands.getLocation()).isEqualTo("OTHER");
    }

    @Test
    void shouldChooseOnlyOneReplacementSetAndEquipExactlyOnePair() throws Exception {

        Relic head = relic("head", Slot.Head, "SET_A", CHARACTER_ID);
        Relic hands = relic("hands", Slot.Hands, "SET_A", CHARACTER_ID);

        Relic setBHead = relic("setBHead", Slot.Head, "SET_B", "");
        Relic setBHands = relic("setBHands", Slot.Hands, "SET_B", "");

        Relic setCHead = relic("setCHead", Slot.Head, "SET_C", "");
        Relic setCHands = relic("setCHands", Slot.Hands, "SET_C", "");

        ScannedData data = new ScannedData();
        data.setRelics(new ArrayList<>(List.of(
            head,
            hands,
            setBHead,
            setBHands,
            setCHead,
            setCHands
        )));

        invokeMutateOneExistingPair(
            data,
            List.of(Slot.Head, Slot.Hands),
            List.of(head, hands),
            Set.of(),
            Set.of(),
            random);

        List<Relic> equipped =
            data.getRelics().stream()
                .filter(r -> CHARACTER_ID.equals(r.getLocation()))
                .toList();

        assertThat(equipped).hasSize(2);

        String equippedSet =
            equipped.get(0).getSetId();

        assertThat(equipped)
            .allMatch(r -> equippedSet.equals(r.getSetId()));
    }
}