package com.hsrOptimiser.engine;

import static com.hsrOptimiser.engine.SimulatedAnnealing.mutateSingleRelic;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.lenient;

import com.hsrOptimiser.DTO.hsrScanner.Relic;
import com.hsrOptimiser.DTO.hsrScanner.ScannedData;
import com.hsrOptimiser.DTO.hsrScanner.Slot;
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

    @BeforeEach
    void setup() {
        lenient().when(rand.nextInt(anyInt())).thenReturn(0);
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
    void ignoresRelicsFromDifferentSet() {

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
    void ignoresNonFiveStarRelics() {

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
    void canScrapRelicOwnedByAllowedCharacter() {

        Relic source =
            relic("8010", Slot.Head, "setA", 5);

        Relic candidate =
            relic("char2", Slot.Head, "setA", 5);

        mutateSingleRelic(
            data(source, candidate),
            "8010",
            0,
            Set.of("char2"),
            Set.of(), rand);

        assertEquals("", source.getLocation());
        assertEquals("8010", candidate.getLocation());
    }

    @Test
    void disallowedCharacterCannotBeScrappedEvenIfAllowed() {

        Relic source =
            relic("8010", Slot.Head, "setA", 5);

        Relic candidate =
            relic("char2", Slot.Head, "setA", 5);

        mutateSingleRelic(
            data(source, candidate),
            "8010",
            0,
            Set.of("char2"),
            Set.of("char2"), rand);

        assertEquals("8010", source.getLocation());
        assertEquals("char2", candidate.getLocation());
    }

    @Test
    void cannotScrapRelicOwnedByOtherCharacter() {

        Relic source =
            relic("8010", Slot.Head, "setA", 5);

        Relic candidate =
            relic("char2", Slot.Head, "setA", 5);

        mutateSingleRelic(
            data(source, candidate),
            "8010",
            0,
            Set.of(),
            Set.of(), rand);

        assertEquals("8010", source.getLocation());
        assertEquals("char2", candidate.getLocation());
    }

    @Test
    void blankLocationRelicsAreScrapable() {

        Relic source =
            relic("8010", Slot.Head, "setA", 5);

        Relic candidate =
            relic("   ", Slot.Head, "setA", 5);

        mutateSingleRelic(
            data(source, candidate),
            "8010",
            0,
            Set.of(),
            Set.of(), rand);

        assertEquals("8010", candidate.getLocation());
        assertEquals("", source.getLocation());
    }

    @Test
    void nullLocationRelicsAreScrapable() {

        Relic source =
            relic("8010", Slot.Head, "setA", 5);

        Relic candidate =
            relic(null, Slot.Head, "setA", 5);

        mutateSingleRelic(
            data(source, candidate),
            "8010",
            0,
            Set.of(),
            Set.of(), rand);

        assertEquals("8010", candidate.getLocation());
        assertEquals("", source.getLocation());
    }

    @Test
    void planarSphereAllowsAttackTypeDamageBoost() {
        Relic source =
            relic("8010", Slot.PlanarSphere, "setA", 5);

        Relic candidate =
            relic(null, Slot.PlanarSphere, "setA", 5);

        candidate.setMainstat("Lightning DMG Boost");

        mutateSingleRelic(
            data(source, candidate),
            "8010",
            0,
            Set.of(),
            Set.of(), rand);

        assertEquals("8010", candidate.getLocation());
        assertEquals("", source.getLocation());
    }

    @Test
    void planarSphereRejectsOtherMainstats() {
        Relic source =
            relic("8010", Slot.PlanarSphere, "setA", 5);

        Relic candidate =
            relic(null, Slot.PlanarSphere, "setA", 5);

        candidate.setMainstat("Crit Rate");

        mutateSingleRelic(
            data(source, candidate),
            "8010",
            0,
            Set.of(),
            Set.of(), rand);

        assertEquals("8010", source.getLocation());
        assertNull(candidate.getLocation());
    }

    @Test
    void returnsWhenNoEligibleCandidateExists() {

        Relic source =
            relic("8010", Slot.Head, "setA", 5);

        Relic candidate =
            relic("", Slot.Head, "setB", 5);

        mutateSingleRelic(
            data(source, candidate),
            "8010",
            0,
            Set.of(),
            Set.of(), rand);

        assertEquals("8010", source.getLocation());
        assertEquals("", candidate.getLocation());
    }
}
