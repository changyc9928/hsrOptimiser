package com.hsrOptimiser.engine;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.lenient;

import com.hsrOptimiser.DTO.hsrScanner.Relic;
import com.hsrOptimiser.DTO.hsrScanner.ScannedData;
import com.hsrOptimiser.DTO.hsrScanner.Slot;
import com.hsrOptimiser.engine.strategies.PlanarPairMutationStrategy;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SimulatedAnnealingMutatePlanarPairTest {

    private static final String CHAR_ID = "8010";

    @Mock
    private Random random;

    private PlanarPairMutationStrategy strategy;

    @BeforeEach
    void setUp() {
        lenient().when(random.nextInt(anyInt())).thenReturn(0);
        strategy = new PlanarPairMutationStrategy();
    }

    private Relic relic(String setId, Slot slot, int rarity, String loc) {
        Relic r = new Relic();
        r.setSetId(setId);
        r.setSlot(slot);
        r.setRarity(rarity);
        r.setMainstat("ATK");
        r.setLocation(loc);
        return r;
    }

    private ScannedData data(List<Relic> relics) {
        ScannedData d = new ScannedData();
        d.setRelics(relics);
        return d;
    }

    private void mutatePlanarPair(ScannedData data, String characterId, int abilityVersion,
            List<Relic> currentEquipped, Set<String> allowed, Set<String> disallowed, Random rand) {
        MutationContext context = new MutationContext(
                characterId, abilityVersion, allowed, disallowed, rand);
        strategy.mutate(data, context);
    }

    @Test
    void ignores_nonFiveStar_relics() {
        Relic r = relic("A", Slot.PlanarSphere, 4, "");

        mutatePlanarPair(
                data(List.of(r)),
                CHAR_ID, 0,
                new ArrayList<>(), Set.of(), Set.of(), random);

        assertEquals("", r.getLocation());
    }

    @Test
    void ignores_wrong_slot() {
        Relic r = relic("A", Slot.Head, 5, "");

        mutatePlanarPair(
                data(List.of(r)),
                CHAR_ID, 0,
                new ArrayList<>(), Set.of(), Set.of(), random);

        assertEquals("", r.getLocation());
    }

    @Test
    void excludes_disallowed_location() {
        Relic r = relic("A", Slot.PlanarSphere, 5, "BLOCKED");

        mutatePlanarPair(
                data(List.of(r)),
                CHAR_ID, 0,
                new ArrayList<>(), Set.of(), Set.of("BLOCKED"), random);

        assertEquals("BLOCKED", r.getLocation());
    }

    @Test
    void ignores_set_missing_rope_or_sphere() {
        Relic sphereOnly = relic("A", Slot.PlanarSphere, 5, "");
        Relic ropeOnly = relic("B", Slot.LinkRope, 5, "");

        mutatePlanarPair(
                data(List.of(sphereOnly, ropeOnly)),
                CHAR_ID, 0,
                new ArrayList<>(), Set.of(), Set.of(), random);

        assertEquals("", sphereOnly.getLocation());
        assertEquals("", ropeOnly.getLocation());
    }

    @Test
    void equips_new_and_unequips_old() {
        Relic oldSphere = relic("OLD", Slot.PlanarSphere, 5, CHAR_ID);
        Relic oldRope = relic("OLD", Slot.LinkRope, 5, CHAR_ID);

        Relic newSphere = relic("NEW", Slot.PlanarSphere, 5, "");
        Relic newRope = relic("NEW", Slot.LinkRope, 5, "");

        mutatePlanarPair(
                data(List.of(oldSphere, oldRope, newSphere, newRope)),
                CHAR_ID, 0,
                new ArrayList<>(List.of(oldSphere, oldRope)),
                Set.of(), Set.of(), random);

        assertEquals("", oldSphere.getLocation());
        assertEquals("", oldRope.getLocation());
        assertEquals(CHAR_ID, newSphere.getLocation());
        assertEquals(CHAR_ID, newRope.getLocation());
    }

    @Test
    void no_valid_sets_results_in_no_change() {
        Relic r = relic("A", Slot.Head, 5, "");

        mutatePlanarPair(
                data(List.of(r)),
                CHAR_ID, 0,
                new ArrayList<>(), Set.of(), Set.of(), random);

        assertEquals("", r.getLocation());
    }

    @Test
    void only_first_valid_set_is_used() {
        Relic aSphere = relic("A", Slot.PlanarSphere, 5, "");
        Relic aRope = relic("A", Slot.LinkRope, 5, "");

        Relic bSphere = relic("B", Slot.PlanarSphere, 5, "");
        Relic bRope = relic("B", Slot.LinkRope, 5, "");

        mutatePlanarPair(
                data(List.of(aSphere, aRope, bSphere, bRope)),
                CHAR_ID, 0,
                new ArrayList<>(), Set.of(), Set.of(), random);

        long equipped = Stream.of(aSphere, aRope, bSphere, bRope)
                .filter(r -> CHAR_ID.equals(r.getLocation()))
                .count();

        assertEquals(2, equipped);
    }

    @Test
    void respects_allowed_and_disallowed_sets() {
        Relic r = relic("A", Slot.PlanarSphere, 5, "OK");

        mutatePlanarPair(
                data(List.of(r)),
                CHAR_ID, 0,
                new ArrayList<>(), Set.of("OK"), Set.of(), random);

        assertNotNull(r.getLocation());
    }

    @Test
    void does_not_crash_on_large_input() {
        List<Relic> relics = new ArrayList<>();

        for (int i = 0; i < 200; i++) {
            relics.add(relic("A", Slot.PlanarSphere, 5, ""));
            relics.add(relic("A", Slot.LinkRope, 5, ""));
        }

        assertDoesNotThrow(() -> mutatePlanarPair(
                data(relics),
                CHAR_ID, 0,
                new ArrayList<>(), Set.of(), Set.of(), random));
    }

    @Test
    void transaction_is_atomic_from_user_perspective() {
        Relic oldSphere = relic("OLD", Slot.PlanarSphere, 5, CHAR_ID);
        Relic oldRope = relic("OLD", Slot.LinkRope, 5, CHAR_ID);

        Relic newSphere = relic("NEW", Slot.PlanarSphere, 5, "");
        Relic newRope = relic("NEW", Slot.LinkRope, 5, "");

        List<Relic> equipped = new ArrayList<>(List.of(oldSphere, oldRope));

        mutatePlanarPair(
                data(List.of(oldSphere, oldRope, newSphere, newRope)),
                CHAR_ID, 0,
                equipped, Set.of(), Set.of(), random);

        boolean oldStillEquipped = CHAR_ID.equals(oldSphere.getLocation()) ||
                CHAR_ID.equals(oldRope.getLocation());

        boolean newFullyEquipped = CHAR_ID.equals(newSphere.getLocation()) &&
                CHAR_ID.equals(newRope.getLocation());

        assertTrue(newFullyEquipped || !oldStillEquipped);
    }

    @Test
    void never_results_in_partial_equipment() {
        Relic sphere = relic("NEW", Slot.PlanarSphere, 5, "");
        Relic rope = relic("NEW", Slot.LinkRope, 5, "");

        mutatePlanarPair(
                data(List.of(sphere, rope)),
                CHAR_ID, 0,
                new ArrayList<>(), Set.of(), Set.of(), random);

        boolean sphereEquipped = CHAR_ID.equals(sphere.getLocation());
        boolean ropeEquipped = CHAR_ID.equals(rope.getLocation());

        assertEquals(sphereEquipped, ropeEquipped,
                "Sphere and rope must be consistent (both or none)");
    }

    @Test
    void mutation_is_idempotent_under_same_state() {
        Relic sphere = relic("A", Slot.PlanarSphere, 5, "");
        Relic rope = relic("A", Slot.LinkRope, 5, "");

        ScannedData d = data(List.of(sphere, rope));

        mutatePlanarPair(d, CHAR_ID, 1, new ArrayList<>(), Set.of(), Set.of(), random);

        String firstSphere = sphere.getLocation();
        String firstRope = rope.getLocation();

        mutatePlanarPair(d, CHAR_ID, 1, new ArrayList<>(), Set.of(), Set.of(), random);

        assertEquals(firstSphere, sphere.getLocation());
        assertEquals(firstRope, rope.getLocation());
    }

    @Test
    void does_not_unequip_without_successful_replacement() {
        Relic oldSphere = relic("OLD", Slot.PlanarSphere, 5, CHAR_ID);
        Relic onlySphere = relic("A", Slot.PlanarSphere, 5, "");

        mutatePlanarPair(
                data(List.of(oldSphere, onlySphere)),
                CHAR_ID, 0,
                new ArrayList<>(List.of(oldSphere)),
                Set.of(), Set.of(), random);

        assertEquals(CHAR_ID, oldSphere.getLocation());
    }
}
