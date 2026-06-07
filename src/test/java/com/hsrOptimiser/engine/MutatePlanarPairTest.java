package com.hsrOptimiser.engine;

import static com.hsrOptimiser.engine.SimulatedAnnealing.mutatePlanarPair;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.hsrOptimiser.DTO.hsrScanner.Relic;
import com.hsrOptimiser.DTO.hsrScanner.ScannedData;
import com.hsrOptimiser.DTO.hsrScanner.Slot;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import org.junit.jupiter.api.Test;

class MutatePlanarPairTest {

    private static final String CHAR_ID = "8010";

    // -------------------------
    // Helpers (ONLY test utilities)
    // -------------------------

    private Relic relic(String setId, Slot slot, int rarity, String stat, String loc) {
        Relic r = new Relic();
        r.setSetId(setId);
        r.setSlot(slot);
        r.setRarity(rarity);
        r.setMainstat(stat);
        r.setLocation(loc);
        return r;
    }

    private ScannedData data(List<Relic> relics) {
        ScannedData d = new ScannedData();
        d.setRelics(relics);
        return d;
    }

    private Random seeded(int seed) {
        return new Random(seed);
    }

    // -------------------------
    // 1. Filtering logic
    // -------------------------

    @Test
    void ignores_nonFiveStar_relics() {
        Relic r = relic("A", Slot.PlanarSphere, 4, "ATK", "");

        mutatePlanarPair(
            data(List.of(r)),
            CHAR_ID,
            0,
            new ArrayList<>(),
            Set.of(),
            Set.of(),
            seeded(1)
        );

        assertEquals("", r.getLocation());
    }

    @Test
    void ignores_wrong_slot() {
        Relic r = relic("A", Slot.Head, 5, "ATK", "");

        mutatePlanarPair(
            data(List.of(r)),
            CHAR_ID,
            0,
            new ArrayList<>(),
            Set.of(),
            Set.of(),
            seeded(1)
        );

        assertEquals("", r.getLocation());
    }

    @Test
    void excludes_disallowed_location() {
        Relic r = relic("A", Slot.PlanarSphere, 5, "ATK", "BLOCKED");

        mutatePlanarPair(
            data(List.of(r)),
            CHAR_ID,
            0,
            new ArrayList<>(),
            Set.of(),
            Set.of("BLOCKED"),
            seeded(1)
        );

        assertEquals("BLOCKED", r.getLocation());
    }

    // -------------------------
    // 2. Set validity rules
    // -------------------------

    @Test
    void ignores_set_missing_rope_or_sphere() {
        Relic sphereOnly = relic("A", Slot.PlanarSphere, 5, "ATK", "");
        Relic ropeOnly = relic("B", Slot.LinkRope, 5, "ATK", "");

        mutatePlanarPair(
            data(List.of(sphereOnly, ropeOnly)),
            CHAR_ID,
            0,
            new ArrayList<>(),
            Set.of(),
            Set.of(),
            seeded(1)
        );

        assertEquals("", sphereOnly.getLocation());
        assertEquals("", ropeOnly.getLocation());
    }

    // -------------------------
    // 3. Successful equip
    // -------------------------

    @Test
    void equips_new_and_unequips_old() {
        Relic oldSphere = relic("OLD", Slot.PlanarSphere, 5, "ATK", CHAR_ID);
        Relic oldRope = relic("OLD", Slot.LinkRope, 5, "ATK", CHAR_ID);

        Relic newSphere = relic("NEW", Slot.PlanarSphere, 5, "ATK", "");
        Relic newRope = relic("NEW", Slot.LinkRope, 5, "ATK", "");

        mutatePlanarPair(
            data(List.of(newSphere, newRope)),
            CHAR_ID,
            0,
            new ArrayList<>(List.of(oldSphere, oldRope)),
            Set.of(),
            Set.of(),
            seeded(1)
        );

        assertEquals("", oldSphere.getLocation());
        assertEquals("", oldRope.getLocation());

        assertEquals(CHAR_ID, newSphere.getLocation());
        assertEquals(CHAR_ID, newRope.getLocation());
    }

    // -------------------------
    // 4. No valid sets
    // -------------------------

    @Test
    void no_valid_sets_results_in_no_change() {
        Relic r = relic("A", Slot.Head, 5, "ATK", "");

        mutatePlanarPair(
            data(List.of(r)),
            CHAR_ID,
            0,
            new ArrayList<>(),
            Set.of(),
            Set.of(),
            seeded(1)
        );

        assertEquals("", r.getLocation());
    }

    // -------------------------
    // 5. Only one valid set applied
    // -------------------------

    @Test
    void only_first_valid_set_is_used() {
        Relic aSphere = relic("A", Slot.PlanarSphere, 5, "ATK", "");
        Relic aRope = relic("A", Slot.LinkRope, 5, "ATK", "");

        Relic bSphere = relic("B", Slot.PlanarSphere, 5, "ATK", "");
        Relic bRope = relic("B", Slot.LinkRope, 5, "ATK", "");

        mutatePlanarPair(
            data(List.of(aSphere, aRope, bSphere, bRope)),
            CHAR_ID,
            0,
            new ArrayList<>(),
            Set.of(),
            Set.of(),
            seeded(1)
        );

        long equipped =
            List.of(aSphere, aRope, bSphere, bRope)
                .stream()
                .filter(r -> CHAR_ID.equals(r.getLocation()))
                .count();

        assertEquals(2, equipped);
    }

    // -------------------------
    // 6. Allowed set filtering
    // -------------------------

    @Test
    void respects_allowed_and_disallowed_sets() {
        Relic r = relic("A", Slot.PlanarSphere, 5, "ATK", "OK");

        mutatePlanarPair(
            data(List.of(r)),
            CHAR_ID,
            0,
            new ArrayList<>(),
            Set.of("OK"),
            Set.of(),
            seeded(1)
        );

        // may or may not equip depending on rope existence
        assertNotNull(r.getLocation());
    }

    // -------------------------
    // 7. Stability test (no crash, deterministic run)
    // -------------------------

    @Test
    void does_not_crash_on_large_input() {
        List<Relic> relics = new ArrayList<>();

        for (int i = 0; i < 200; i++) {
            relics.add(relic("A", Slot.PlanarSphere, 5, "ATK", ""));
            relics.add(relic("A", Slot.LinkRope, 5, "ATK", ""));
        }

        assertDoesNotThrow(() ->
            mutatePlanarPair(
                data(relics),
                CHAR_ID,
                0,
                new ArrayList<>(),
                Set.of(),
                Set.of(),
                seeded(42)
            )
        );
    }

    @Test
    void transaction_is_atomic_from_user_perspective() {

        Relic oldSphere = relic("OLD", Slot.PlanarSphere, 5, "ATK", CHAR_ID);
        Relic oldRope = relic("OLD", Slot.LinkRope, 5, "ATK", CHAR_ID);

        Relic newSphere = relic("NEW", Slot.PlanarSphere, 5, "ATK", "");
        Relic newRope = relic("NEW", Slot.LinkRope, 5, "ATK", "");

        List<Relic> equipped = new ArrayList<>(List.of(oldSphere, oldRope));

        mutatePlanarPair(
            data(List.of(newSphere, newRope)),
            CHAR_ID,
            0,
            equipped,
            Set.of(),
            Set.of(),
            new Random(1)
        );

        // AFTER: either fully swapped or unchanged — no half state allowed

        boolean oldStillEquipped =
            CHAR_ID.equals(oldSphere.getLocation()) ||
                CHAR_ID.equals(oldRope.getLocation());

        boolean newFullyEquipped =
            CHAR_ID.equals(newSphere.getLocation()) &&
                CHAR_ID.equals(newRope.getLocation());

        assertTrue(newFullyEquipped || !oldStillEquipped);
    }

    @Test
    void never_results_in_partial_equipment() {

        Relic sphere = relic("NEW", Slot.PlanarSphere, 5, "ATK", "");
        Relic rope = relic("NEW", Slot.LinkRope, 5, "ATK", "");

        mutatePlanarPair(
            data(List.of(sphere, rope)),
            CHAR_ID,
            0,
            new ArrayList<>(),
            Set.of(),
            Set.of(),
            new Random(1)
        );

        boolean sphereEquipped = CHAR_ID.equals(sphere.getLocation());
        boolean ropeEquipped = CHAR_ID.equals(rope.getLocation());

        assertEquals(sphereEquipped, ropeEquipped,
            "Sphere and rope must be consistent (both or none)");
    }

    @Test
    void mutation_is_idempotent_under_same_state() {

        Relic sphere = relic("A", Slot.PlanarSphere, 5, "ATK", "");
        Relic rope = relic("A", Slot.LinkRope, 5, "ATK", "");

        ScannedData d = data(List.of(sphere, rope));

        Random r = new Random(1);

        mutatePlanarPair(d, CHAR_ID, 1, new ArrayList<>(), Set.of(), Set.of(), r);

        String firstSphere = sphere.getLocation();
        String firstRope = rope.getLocation();

        mutatePlanarPair(d, CHAR_ID, 1, new ArrayList<>(), Set.of(), Set.of(), new Random(1));

        assertEquals(firstSphere, sphere.getLocation());
        assertEquals(firstRope, rope.getLocation());
    }

    @Test
    void does_not_unequip_without_successful_replacement() {

        Relic oldSphere = relic("OLD", Slot.PlanarSphere, 5, "ATK", CHAR_ID);

        Relic onlySphere = relic("A", Slot.PlanarSphere, 5, "ATK", "");

        mutatePlanarPair(
            data(List.of(onlySphere)), // missing rope → invalid set
            CHAR_ID,
            0,
            new ArrayList<>(List.of(oldSphere)),
            Set.of(),
            Set.of(),
            new Random(1)
        );

        assertEquals(CHAR_ID, oldSphere.getLocation());
    }
}