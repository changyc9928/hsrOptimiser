package com.hsrOptimiser.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.lenient;

import com.hsrOptimiser.DTO.hsrScanner.Relic;
import com.hsrOptimiser.DTO.hsrScanner.ScannedData;
import com.hsrOptimiser.DTO.hsrScanner.Slot;
import com.hsrOptimiser.engine.strategies.SingleRelicMutationStrategy;
import java.util.List;
import java.util.Random;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class SimulatedAnnealingMutateSingleRelicTests {

    @Mock
    Random rand;

    private SingleRelicMutationStrategy strategy;

    @BeforeEach
    void setup() {
        lenient().when(rand.nextInt(anyInt())).thenReturn(0);
        strategy = new SingleRelicMutationStrategy();
    }

    private Relic relic(
        String location,
        Slot slot,
        String setId,
        int rarity) {

        Relic r = new Relic();
        r.setLocation(location);
        r.setSlot(slot);
        r.setSetId(setId);
        r.setMainstat("HP");
        r.setRarity(rarity);
        return r;
    }

    private ScannedData data(Relic... relics) {
        ScannedData d = new ScannedData();
        d.setRelics(List.of(relics));
        return d;
    }

    private void mutateSingleRelic(ScannedData data, String characterId, int abilityVersion,
        Set<String> allowed, Set<String> disallowed, Random random) {
        MutationContext context = new MutationContext(
            characterId, abilityVersion, allowed, disallowed, random);
        strategy.mutate(data, context);
    }

    @Test
    void doesNothingWhenCharacterOwnsNoRelics() {

        Relic relic = relic(
            "",
            Slot.Head,
            "setA",
            5);

        ScannedData data = data(relic);

        mutateSingleRelic(
            data,
            "8010",
            0,
            Set.of(),
            Set.of(), rand);

        assertEquals("", relic.getLocation());
    }

    @Test
    void doesNothingWhenSourceIsOnlyCandidate() {

        Relic source =
            relic("8010", Slot.Head, "setA", 5);

        ScannedData data = data(source);

        mutateSingleRelic(
            data,
            "8010",
            0,
            Set.of(),
            Set.of(), rand);
        assertEquals("8010", source.getLocation());
    }

    @Test
    void swapsOwnershipWithMatchingUnownedRelic() {

        Relic source =
            relic("8010", Slot.Head, "setA", 5);

        Relic candidate =
            relic("", Slot.Head, "setA", 5);

        ScannedData data = data(source, candidate);

        mutateSingleRelic(
            data,
            "8010",
            0,
            Set.of(),
            Set.of(), rand);

        assertEquals("", source.getLocation());
        assertEquals("8010", candidate.getLocation());
    }

    @Test
    void ignoresRelicsWithDifferentSlot() {

        Relic source =
            relic("8010", Slot.Head, "setA", 5);

        Relic candidate =
            relic("", Slot.Hands, "setA", 5);

        ScannedData data = data(source, candidate);

        mutateSingleRelic(
            data,
            "8010",
            0,
            Set.of(),
            Set.of(), rand);

        assertEquals("8010", source.getLocation());
        assertEquals("", candidate.getLocation());
    }

    @Test
    void ignoresRelicsWithDifferentSet() {

        Relic source =
            relic("8010", Slot.Head, "setA", 5);

        Relic candidate =
            relic("", Slot.Head, "setB", 5);

        ScannedData data = data(source, candidate);

        mutateSingleRelic(
            data,
            "8010",
            0,
            Set.of(),
            Set.of(), rand);

        assertEquals("8010", source.getLocation());
        assertEquals("", candidate.getLocation());
    }

    @Test
    void ignoresLowerRarityRelics() {

        Relic source =
            relic("8010", Slot.Head, "setA", 5);

        Relic candidate =
            relic("", Slot.Head, "setA", 4);

        ScannedData data = data(source, candidate);

        mutateSingleRelic(
            data,
            "8010",
            0,
            Set.of(),
            Set.of(), rand);

        assertEquals("8010", source.getLocation());
        assertEquals("", candidate.getLocation());
    }

    @Test
    void canScrapRelicsFromAllowedCharacters() {

        Relic source =
            relic("8010", Slot.Head, "setA", 5);

        Relic candidate =
            relic("char2", Slot.Head, "setA", 5);

        ScannedData data = data(source, candidate);

        mutateSingleRelic(
            data,
            "8010",
            0,
            Set.of("char2"),
            Set.of(), rand);

        assertEquals("", source.getLocation());
        assertEquals("8010", candidate.getLocation());
    }

    @Test
    void cannotScrapRelicsFromDisallowedCharacters() {

        Relic source =
            relic("8010", Slot.Head, "setA", 5);

        Relic candidate =
            relic("char2", Slot.Head, "setA", 5);

        ScannedData data = data(source, candidate);

        mutateSingleRelic(
            data,
            "8010",
            0,
            Set.of("char2"),
            Set.of("char2"), rand);

        assertEquals("8010", source.getLocation());
        assertEquals("char2", candidate.getLocation());
    }

    @Test
    void sourceRelicCanBeItsOwnCandidate() {

        Relic source =
            relic("8010", Slot.Head, "setA", 5);

        ScannedData data = data(source);

        mutateSingleRelic(
            data,
            "8010",
            0,
            Set.of(),
            Set.of(), rand);

        assertEquals("8010", source.getLocation());
    }

    @Test
    void ignoresSphereWithWrongMainstat() {

        Relic source =
            relic("8010", Slot.PlanarSphere, "setA", 5);
        source.setMainstat("Fire DMG Boost");

        Relic candidate =
            relic("", Slot.PlanarSphere, "setA", 5);
        candidate.setMainstat("Effect Hit Rate");

        ScannedData data = data(source, candidate);

        mutateSingleRelic(
            data,
            "8010",
            0,
            Set.of(),
            Set.of(), rand);

        assertEquals("8010", source.getLocation());
        assertEquals("", candidate.getLocation());
    }

    @Test
    void acceptsSphereWithCorrectMainstat() {

        Relic source =
            relic("8010", Slot.PlanarSphere, "setA", 5);
        source.setMainstat("Fire DMG Boost");

        Relic candidate =
            relic("", Slot.PlanarSphere, "setA", 5);
        candidate.setMainstat("ATK");

        ScannedData data = data(source, candidate);

        mutateSingleRelic(
            data,
            "8010",
            0,
            Set.of(),
            Set.of(), rand);

        assertEquals("", source.getLocation());
        assertEquals("8010", candidate.getLocation());
    }

    @Test
    void handlesMultipleCandidates() {

        Relic source =
            relic("8010", Slot.Head, "setA", 5);

        Relic candidate1 =
            relic("", Slot.Head, "setA", 5);

        Relic candidate2 =
            relic("", Slot.Head, "setA", 5);

        ScannedData data = data(source, candidate1, candidate2);

        mutateSingleRelic(
            data,
            "8010",
            0,
            Set.of(),
            Set.of(), rand);

        assertEquals("", source.getLocation());
        long assignedCount = data.getRelics().stream()
            .filter(r -> "8010".equals(r.getLocation()))
            .count();
        assertEquals(1, assignedCount);
    }

    @Test
    void handlesMixedAvailability() {

        Relic source =
            relic("8010", Slot.Head, "setA", 5);

        Relic available =
            relic("", Slot.Head, "setA", 5);

        Relic blocked =
            relic("blocked", Slot.Head, "setA", 5);

        ScannedData data = data(source, available, blocked);

        mutateSingleRelic(
            data,
            "8010",
            0,
            Set.of(),
            Set.of(), rand);

        assertEquals("", source.getLocation());
        assertEquals("8010", available.getLocation());
        assertEquals("blocked", blocked.getLocation());
    }

    @Test
    void handlesNullLocationAsUnequipped() {

        Relic source =
            relic("8010", Slot.Head, "setA", 5);

        Relic candidate =
            relic(null, Slot.Head, "setA", 5);

        ScannedData data = data(source, candidate);

        mutateSingleRelic(
            data,
            "8010",
            0,
            Set.of(),
            Set.of(), rand);

        assertEquals("", source.getLocation());
        assertEquals("8010", candidate.getLocation());
    }

    @Test
    void handlesEmptyLocationAsUnequipped() {

        Relic source =
            relic("8010", Slot.Head, "setA", 5);

        Relic candidate =
            relic("", Slot.Head, "setA", 5);

        ScannedData data = data(source, candidate);

        mutateSingleRelic(
            data,
            "8010",
            0,
            Set.of(),
            Set.of(), rand);

        assertEquals("", source.getLocation());
        assertEquals("8010", candidate.getLocation());
    }
}
