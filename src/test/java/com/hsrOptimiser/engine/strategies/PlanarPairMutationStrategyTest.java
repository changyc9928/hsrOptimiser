package com.hsrOptimiser.engine.strategies;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.lenient;

import com.hsrOptimiser.DTO.hsrScanner.Relic;
import com.hsrOptimiser.DTO.hsrScanner.ScannedData;
import com.hsrOptimiser.DTO.hsrScanner.Slot;
import com.hsrOptimiser.engine.MutationContext;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Comprehensive tests for {@link PlanarPairMutationStrategy}.
 *
 * <p>
 * These tests cover the bug where the strategy was unequipping ALL relics
 * instead of only planar relics (PlanarSphere + LinkRope), plus edge cases
 * and optimizer invariants.
 */
@ExtendWith(MockitoExtension.class)
class PlanarPairMutationStrategyTest {

    private static final String TARGET_CHARACTER = "1502";
    private static final String OTHER_CHARACTER = "8010";

    @Mock
    private Random random;

    private PlanarPairMutationStrategy strategy;

    @BeforeEach
    void setUp() {
        // Default: always pick the first element from any list
        lenient().when(random.nextInt(anyInt())).thenReturn(0);
        strategy = new PlanarPairMutationStrategy();
    }

    // -----------------------------------------------------------------------
    // Helper methods
    // -----------------------------------------------------------------------

    private Relic relic(String uid, String setId, Slot slot, String location) {
        Relic relic = new Relic();
        relic.setUid(uid);
        relic.setSetId(setId);
        relic.setSlot(slot);
        relic.setLocation(location);
        relic.setRarity(5);
        return relic;
    }

    private Relic relicWithMainStat(String uid, String setId, Slot slot,
            String location, String mainStat) {
        Relic r = relic(uid, setId, slot, location);
        r.setMainstat(mainStat);
        return r;
    }

    private ScannedData dataWithRelics(List<Relic> relics) {
        ScannedData data = new ScannedData();
        data.setRelics(new ArrayList<>(relics));
        return data;
    }

    private MutationContext defaultContext(String characterId) {
        return new MutationContext(characterId, 0, Set.of(), Set.of(), random);
    }

    private List<Relic> equippedRelics(ScannedData data, String characterId) {
        return data.getRelics().stream()
                .filter(r -> characterId.equals(r.getLocation()))
                .toList();
    }

    private Set<Slot> equippedSlots(ScannedData data, String characterId) {
        return equippedRelics(data, characterId).stream()
                .map(Relic::getSlot)
                .collect(Collectors.toSet());
    }

    // -----------------------------------------------------------------------
    // Core bug-fix tests
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Should only replace planar slots, leaving cavern relics untouched")
    void mutate_shouldOnlyReplacePlanarSlots() {
        // Character 1502 fully equipped with 6 relics
        Relic head = relic("h1", "115", Slot.Head, TARGET_CHARACTER);
        Relic hands = relic("ha1", "115", Slot.Hands, TARGET_CHARACTER);
        Relic body = relic("b1", "130", Slot.Body, TARGET_CHARACTER);
        Relic feet = relic("f1", "130", Slot.Feet, TARGET_CHARACTER);
        Relic oldSphere = relicWithMainStat("s1", "317", Slot.PlanarSphere, TARGET_CHARACTER, "ATK");
        Relic oldRope = relic("r1", "317", Slot.LinkRope, TARGET_CHARACTER);

        // Available replacement planar set
        Relic newSphere = relicWithMainStat("s100", "999", Slot.PlanarSphere, "", "ATK");
        Relic newRope = relic("r100", "999", Slot.LinkRope, "");

        ScannedData data = dataWithRelics(List.of(
                head, hands, body, feet, oldSphere, oldRope, newSphere, newRope));

        strategy.mutate(data, defaultContext(TARGET_CHARACTER));

        // Cavern relics must remain equipped
        assertThat(head.getLocation()).isEqualTo(TARGET_CHARACTER);
        assertThat(hands.getLocation()).isEqualTo(TARGET_CHARACTER);
        assertThat(body.getLocation()).isEqualTo(TARGET_CHARACTER);
        assertThat(feet.getLocation()).isEqualTo(TARGET_CHARACTER);

        // Old planar relics must be unequipped
        assertThat(oldSphere.getLocation()).isEmpty();
        assertThat(oldRope.getLocation()).isEmpty();

        // New planar relics must be equipped
        assertThat(newSphere.getLocation()).isEqualTo(TARGET_CHARACTER);
        assertThat(newRope.getLocation()).isEqualTo(TARGET_CHARACTER);
    }

    @Test
    @DisplayName("Character must always end with exactly 6 equipped relics")
    void mutate_shouldNeverLeaveCharacterWithLessThanSixRelics() {
        Relic head = relic("h1", "115", Slot.Head, TARGET_CHARACTER);
        Relic hands = relic("ha1", "115", Slot.Hands, TARGET_CHARACTER);
        Relic body = relic("b1", "130", Slot.Body, TARGET_CHARACTER);
        Relic feet = relic("f1", "130", Slot.Feet, TARGET_CHARACTER);
        Relic oldSphere = relicWithMainStat("s1", "317", Slot.PlanarSphere, TARGET_CHARACTER, "ATK");
        Relic oldRope = relic("r1", "317", Slot.LinkRope, TARGET_CHARACTER);

        Relic newSphere = relicWithMainStat("s100", "999", Slot.PlanarSphere, "", "ATK");
        Relic newRope = relic("r100", "999", Slot.LinkRope, "");

        ScannedData data = dataWithRelics(List.of(
                head, hands, body, feet, oldSphere, oldRope, newSphere, newRope));

        strategy.mutate(data, defaultContext(TARGET_CHARACTER));

        assertThat(equippedRelics(data, TARGET_CHARACTER)).hasSize(6);
    }

    @Test
    @DisplayName("All six slots must still be present after mutation")
    void mutate_shouldPreserveAllSlots() {
        Relic head = relic("h1", "115", Slot.Head, TARGET_CHARACTER);
        Relic hands = relic("ha1", "115", Slot.Hands, TARGET_CHARACTER);
        Relic body = relic("b1", "130", Slot.Body, TARGET_CHARACTER);
        Relic feet = relic("f1", "130", Slot.Feet, TARGET_CHARACTER);
        Relic oldSphere = relicWithMainStat("s1", "317", Slot.PlanarSphere, TARGET_CHARACTER, "ATK");
        Relic oldRope = relic("r1", "317", Slot.LinkRope, TARGET_CHARACTER);

        Relic newSphere = relicWithMainStat("s100", "999", Slot.PlanarSphere, "", "ATK");
        Relic newRope = relic("r100", "999", Slot.LinkRope, "");

        ScannedData data = dataWithRelics(List.of(
                head, hands, body, feet, oldSphere, oldRope, newSphere, newRope));

        strategy.mutate(data, defaultContext(TARGET_CHARACTER));

        Set<Slot> slots = equippedSlots(data, TARGET_CHARACTER);
        assertThat(slots).containsExactlyInAnyOrder(
                Slot.Head, Slot.Hands, Slot.Body, Slot.Feet,
                Slot.PlanarSphere, Slot.LinkRope);
    }

    // -----------------------------------------------------------------------
    // Cross-character isolation tests
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Mutation should not modify other characters' relics")
    void mutate_shouldNotModifyOtherCharacters() {
        // Target character 1502
        Relic head = relic("h1", "115", Slot.Head, TARGET_CHARACTER);
        Relic hands = relic("ha1", "115", Slot.Hands, TARGET_CHARACTER);
        Relic body = relic("b1", "130", Slot.Body, TARGET_CHARACTER);
        Relic feet = relic("f1", "130", Slot.Feet, TARGET_CHARACTER);
        Relic oldSphere = relicWithMainStat("s1", "317", Slot.PlanarSphere, TARGET_CHARACTER, "ATK");
        Relic oldRope = relic("r1", "317", Slot.LinkRope, TARGET_CHARACTER);

        // Other character 8010
        Relic otherHead = relic("oh1", "200", Slot.Head, OTHER_CHARACTER);
        Relic otherSphere = relicWithMainStat("os1", "200", Slot.PlanarSphere, OTHER_CHARACTER, "HP");
        Relic otherRope = relic("or1", "200", Slot.LinkRope, OTHER_CHARACTER);
        Relic otherBody = relic("ob1", "200", Slot.Body, OTHER_CHARACTER);

        // Available replacement for 1502
        Relic newSphere = relicWithMainStat("s100", "999", Slot.PlanarSphere, "", "ATK");
        Relic newRope = relic("r100", "999", Slot.LinkRope, "");

        ScannedData data = dataWithRelics(List.of(
                head, hands, body, feet, oldSphere, oldRope,
                otherHead, otherSphere, otherRope, otherBody,
                newSphere, newRope));

        strategy.mutate(data, defaultContext(TARGET_CHARACTER));

        // Other character's relics must be untouched
        assertThat(otherHead.getLocation()).isEqualTo(OTHER_CHARACTER);
        assertThat(otherSphere.getLocation()).isEqualTo(OTHER_CHARACTER);
        assertThat(otherRope.getLocation()).isEqualTo(OTHER_CHARACTER);
        assertThat(otherBody.getLocation()).isEqualTo(OTHER_CHARACTER);
    }

    @Test
    @DisplayName("Mutation should not create duplicate ownership of the same relic")
    void mutate_shouldMaintainSingleOwnerPerRelic() {
        Relic head = relic("h1", "115", Slot.Head, TARGET_CHARACTER);
        Relic hands = relic("ha1", "115", Slot.Hands, TARGET_CHARACTER);
        Relic body = relic("b1", "130", Slot.Body, TARGET_CHARACTER);
        Relic feet = relic("f1", "130", Slot.Feet, TARGET_CHARACTER);
        Relic oldSphere = relicWithMainStat("s1", "317", Slot.PlanarSphere, TARGET_CHARACTER, "ATK");
        Relic oldRope = relic("r1", "317", Slot.LinkRope, TARGET_CHARACTER);

        Relic newSphere = relicWithMainStat("s100", "999", Slot.PlanarSphere, "", "ATK");
        Relic newRope = relic("r100", "999", Slot.LinkRope, "");

        ScannedData data = dataWithRelics(List.of(
                head, hands, body, feet, oldSphere, oldRope, newSphere, newRope));

        strategy.mutate(data, defaultContext(TARGET_CHARACTER));

        // Each relic uid should appear exactly once in the list
        long s100Count = data.getRelics().stream()
                .filter(r -> "s100".equals(r.getUid()))
                .count();
        assertThat(s100Count).isEqualTo(1);
        assertThat(newSphere.getLocation()).isEqualTo(TARGET_CHARACTER);
    }

    @Test
    @DisplayName("No relic should be assigned to multiple slots")
    void mutate_shouldNotAssignSameRelicToMultipleSlots() {
        Relic head = relic("h1", "115", Slot.Head, TARGET_CHARACTER);
        Relic hands = relic("ha1", "115", Slot.Hands, TARGET_CHARACTER);
        Relic body = relic("b1", "130", Slot.Body, TARGET_CHARACTER);
        Relic feet = relic("f1", "130", Slot.Feet, TARGET_CHARACTER);
        Relic oldSphere = relicWithMainStat("s1", "317", Slot.PlanarSphere, TARGET_CHARACTER, "ATK");
        Relic oldRope = relic("r1", "317", Slot.LinkRope, TARGET_CHARACTER);

        Relic newSphere = relicWithMainStat("s100", "999", Slot.PlanarSphere, "", "ATK");
        Relic newRope = relic("r100", "999", Slot.LinkRope, "");

        ScannedData data = dataWithRelics(List.of(
                head, hands, body, feet, oldSphere, oldRope, newSphere, newRope));

        strategy.mutate(data, defaultContext(TARGET_CHARACTER));

        List<String> equippedUids = equippedRelics(data, TARGET_CHARACTER).stream()
                .map(Relic::getUid)
                .toList();

        assertThat(equippedUids).hasSameSizeAs(new HashSet<>(equippedUids));
    }

    // -----------------------------------------------------------------------
    // No-valid-pair edge cases
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Should do nothing when no valid planar pair exists (only spheres)")
    void mutate_shouldDoNothingWhenNoValidPlanarPairExists() {
        Relic head = relic("h1", "115", Slot.Head, TARGET_CHARACTER);
        Relic hands = relic("ha1", "115", Slot.Hands, TARGET_CHARACTER);
        Relic body = relic("b1", "130", Slot.Body, TARGET_CHARACTER);
        Relic feet = relic("f1", "130", Slot.Feet, TARGET_CHARACTER);
        Relic oldSphere = relicWithMainStat("s1", "317", Slot.PlanarSphere, TARGET_CHARACTER, "ATK");
        Relic oldRope = relic("r1", "317", Slot.LinkRope, TARGET_CHARACTER);

        // Only spheres available, no ropes
        Relic orphanSphere = relicWithMainStat("s100", "999", Slot.PlanarSphere, "", "ATK");

        ScannedData data = dataWithRelics(List.of(
                head, hands, body, feet, oldSphere, oldRope, orphanSphere));

        // Snapshot before mutation
        String oldSphereLoc = oldSphere.getLocation();
        String oldRopeLoc = oldRope.getLocation();

        strategy.mutate(data, defaultContext(TARGET_CHARACTER));

        // Nothing should have changed
        assertThat(oldSphere.getLocation()).isEqualTo(oldSphereLoc);
        assertThat(oldRope.getLocation()).isEqualTo(oldRopeLoc);
        assertThat(equippedRelics(data, TARGET_CHARACTER)).hasSize(6);
    }

    @Test
    @DisplayName("Should do nothing when no valid planar pair exists (only ropes)")
    void mutate_shouldDoNothingWhenOnlyRopesExist() {
        Relic head = relic("h1", "115", Slot.Head, TARGET_CHARACTER);
        Relic hands = relic("ha1", "115", Slot.Hands, TARGET_CHARACTER);
        Relic body = relic("b1", "130", Slot.Body, TARGET_CHARACTER);
        Relic feet = relic("f1", "130", Slot.Feet, TARGET_CHARACTER);
        Relic oldSphere = relicWithMainStat("s1", "317", Slot.PlanarSphere, TARGET_CHARACTER, "ATK");
        Relic oldRope = relic("r1", "317", Slot.LinkRope, TARGET_CHARACTER);

        // Only ropes available, no spheres
        Relic orphanRope = relic("r100", "999", Slot.LinkRope, "");

        ScannedData data = dataWithRelics(List.of(
                head, hands, body, feet, oldSphere, oldRope, orphanRope));

        strategy.mutate(data, defaultContext(TARGET_CHARACTER));

        assertThat(oldSphere.getLocation()).isEqualTo(TARGET_CHARACTER);
        assertThat(oldRope.getLocation()).isEqualTo(TARGET_CHARACTER);
        assertThat(equippedRelics(data, TARGET_CHARACTER)).hasSize(6);
    }

    @Test
    @DisplayName("Should not corrupt state when no compatible sphere main stat exists")
    void mutate_shouldNotCorruptStateWhenNoCompatibleSphereExists() {
        Relic head = relic("h1", "115", Slot.Head, TARGET_CHARACTER);
        Relic hands = relic("ha1", "115", Slot.Hands, TARGET_CHARACTER);
        Relic body = relic("b1", "130", Slot.Body, TARGET_CHARACTER);
        Relic feet = relic("f1", "130", Slot.Feet, TARGET_CHARACTER);
        Relic oldSphere = relicWithMainStat("s1", "317", Slot.PlanarSphere, TARGET_CHARACTER, "ATK");
        Relic oldRope = relic("r1", "317", Slot.LinkRope, TARGET_CHARACTER);

        // Spheres exist but none have a compatible main stat
        // (pickSphere filters by attackMainStat/HP/ATK/DEF)
        Relic fireSphere = relicWithMainStat("s100", "999", Slot.PlanarSphere, "", "Fire DMG Boost");
        Relic iceSphere = relicWithMainStat("s101", "999", Slot.PlanarSphere, "", "Ice DMG Boost");
        Relic rope = relic("r100", "999", Slot.LinkRope, "");

        ScannedData data = dataWithRelics(List.of(
                head, hands, body, feet, oldSphere, oldRope,
                fireSphere, iceSphere, rope));

        strategy.mutate(data, defaultContext(TARGET_CHARACTER));

        // State should be unchanged
        assertThat(oldSphere.getLocation()).isEqualTo(TARGET_CHARACTER);
        assertThat(oldRope.getLocation()).isEqualTo(TARGET_CHARACTER);
        assertThat(equippedRelics(data, TARGET_CHARACTER)).hasSize(6);
    }

    // -----------------------------------------------------------------------
    // Additional edge cases
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Should equip planars when character currently has none")
    void mutate_shouldEquipPlanarsWhenCharacterHasNone() {
        // Character only has cavern relics equipped
        Relic head = relic("h1", "115", Slot.Head, TARGET_CHARACTER);
        Relic hands = relic("ha1", "115", Slot.Hands, TARGET_CHARACTER);
        Relic body = relic("b1", "130", Slot.Body, TARGET_CHARACTER);
        Relic feet = relic("f1", "130", Slot.Feet, TARGET_CHARACTER);

        Relic newSphere = relicWithMainStat("s100", "999", Slot.PlanarSphere, "", "ATK");
        Relic newRope = relic("r100", "999", Slot.LinkRope, "");

        ScannedData data = dataWithRelics(List.of(
                head, hands, body, feet, newSphere, newRope));

        strategy.mutate(data, defaultContext(TARGET_CHARACTER));

        // Should now have 6 relics
        assertThat(equippedRelics(data, TARGET_CHARACTER)).hasSize(6);
        assertThat(newSphere.getLocation()).isEqualTo(TARGET_CHARACTER);
        assertThat(newRope.getLocation()).isEqualTo(TARGET_CHARACTER);
        // Cavern relics still equipped
        assertThat(head.getLocation()).isEqualTo(TARGET_CHARACTER);
        assertThat(hands.getLocation()).isEqualTo(TARGET_CHARACTER);
        assertThat(body.getLocation()).isEqualTo(TARGET_CHARACTER);
        assertThat(feet.getLocation()).isEqualTo(TARGET_CHARACTER);
    }

    @Test
    @DisplayName("Should handle already-equipped planar pair gracefully")
    void mutate_shouldHandleAlreadyEquippedPlanarPair() {
        Relic head = relic("h1", "115", Slot.Head, TARGET_CHARACTER);
        Relic hands = relic("ha1", "115", Slot.Hands, TARGET_CHARACTER);
        Relic body = relic("b1", "130", Slot.Body, TARGET_CHARACTER);
        Relic feet = relic("f1", "130", Slot.Feet, TARGET_CHARACTER);
        Relic oldSphere = relicWithMainStat("s1", "317", Slot.PlanarSphere, TARGET_CHARACTER, "ATK");
        Relic oldRope = relic("r1", "317", Slot.LinkRope, TARGET_CHARACTER);

        // The "new" set is the same as the old set (same set id)
        Relic sameSphere = relicWithMainStat("s100", "317", Slot.PlanarSphere, "", "ATK");
        Relic sameRope = relic("r100", "317", Slot.LinkRope, "");

        ScannedData data = dataWithRelics(List.of(
                head, hands, body, feet, oldSphere, oldRope, sameSphere, sameRope));

        strategy.mutate(data, defaultContext(TARGET_CHARACTER));

        // Should still have 6 relics equipped
        assertThat(equippedRelics(data, TARGET_CHARACTER)).hasSize(6);
        // The new sphere and rope should be equipped
        assertThat(sameSphere.getLocation()).isEqualTo(TARGET_CHARACTER);
        assertThat(sameRope.getLocation()).isEqualTo(TARGET_CHARACTER);
    }

    @Test
    @DisplayName("Should not equip non-5-star planar relics")
    void mutate_shouldIgnoreNonFiveStarPlanarRelics() {
        Relic head = relic("h1", "115", Slot.Head, TARGET_CHARACTER);
        Relic hands = relic("ha1", "115", Slot.Hands, TARGET_CHARACTER);
        Relic body = relic("b1", "130", Slot.Body, TARGET_CHARACTER);
        Relic feet = relic("f1", "130", Slot.Feet, TARGET_CHARACTER);
        Relic oldSphere = relicWithMainStat("s1", "317", Slot.PlanarSphere, TARGET_CHARACTER, "ATK");
        Relic oldRope = relic("r1", "317", Slot.LinkRope, TARGET_CHARACTER);

        // 4-star sphere and rope (should be ignored)
        Relic fourStarSphere = relicWithMainStat("s100", "999", Slot.PlanarSphere, "", "ATK");
        fourStarSphere.setRarity(4);
        Relic fourStarRope = relic("r100", "999", Slot.LinkRope, "");
        fourStarRope.setRarity(4);

        ScannedData data = dataWithRelics(List.of(
                head, hands, body, feet, oldSphere, oldRope,
                fourStarSphere, fourStarRope));

        strategy.mutate(data, defaultContext(TARGET_CHARACTER));

        // Nothing should change since no valid 5-star pair exists
        assertThat(oldSphere.getLocation()).isEqualTo(TARGET_CHARACTER);
        assertThat(oldRope.getLocation()).isEqualTo(TARGET_CHARACTER);
    }

    @Test
    @DisplayName("Should ignore relics with null set id")
    void mutate_shouldIgnoreRelicsWithNullSetId() {
        Relic head = relic("h1", "115", Slot.Head, TARGET_CHARACTER);
        Relic hands = relic("ha1", "115", Slot.Hands, TARGET_CHARACTER);
        Relic body = relic("b1", "130", Slot.Body, TARGET_CHARACTER);
        Relic feet = relic("f1", "130", Slot.Feet, TARGET_CHARACTER);
        Relic oldSphere = relicWithMainStat("s1", "317", Slot.PlanarSphere, TARGET_CHARACTER, "ATK");
        Relic oldRope = relic("r1", "317", Slot.LinkRope, TARGET_CHARACTER);

        // Relics with null set id
        Relic nullSetSphere = relicWithMainStat("s100", null, Slot.PlanarSphere, "", "ATK");
        Relic nullSetRope = relic("r100", null, Slot.LinkRope, "");

        ScannedData data = dataWithRelics(List.of(
                head, hands, body, feet, oldSphere, oldRope,
                nullSetSphere, nullSetRope));

        strategy.mutate(data, defaultContext(TARGET_CHARACTER));

        // Nothing should change
        assertThat(oldSphere.getLocation()).isEqualTo(TARGET_CHARACTER);
        assertThat(oldRope.getLocation()).isEqualTo(TARGET_CHARACTER);
    }

    // -----------------------------------------------------------------------
    // Optimizer invariant test (MOST IMPORTANT)
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Should preserve optimizer invariants after mutation")
    void mutate_shouldPreserveOptimizerInvariants() {
        // Set up a realistic scenario with multiple characters
        Relic h1 = relic("h1", "115", Slot.Head, TARGET_CHARACTER);
        Relic ha1 = relic("ha1", "115", Slot.Hands, TARGET_CHARACTER);
        Relic b1 = relic("b1", "130", Slot.Body, TARGET_CHARACTER);
        Relic f1 = relic("f1", "130", Slot.Feet, TARGET_CHARACTER);
        Relic s1 = relicWithMainStat("s1", "317", Slot.PlanarSphere, TARGET_CHARACTER, "ATK");
        Relic r1 = relic("r1", "317", Slot.LinkRope, TARGET_CHARACTER);

        Relic oh1 = relic("oh1", "200", Slot.Head, OTHER_CHARACTER);
        Relic oha1 = relic("oha1", "200", Slot.Hands, OTHER_CHARACTER);
        Relic ob1 = relic("ob1", "200", Slot.Body, OTHER_CHARACTER);
        Relic of1 = relic("of1", "200", Slot.Feet, OTHER_CHARACTER);
        Relic os1 = relicWithMainStat("os1", "200", Slot.PlanarSphere, OTHER_CHARACTER, "HP");
        Relic or1 = relic("or1", "200", Slot.LinkRope, OTHER_CHARACTER);

        Relic newSphere = relicWithMainStat("s100", "999", Slot.PlanarSphere, "", "ATK");
        Relic newRope = relic("r100", "999", Slot.LinkRope, "");

        ScannedData data = dataWithRelics(List.of(
                h1, ha1, b1, f1, s1, r1,
                oh1, oha1, ob1, of1, os1, or1,
                newSphere, newRope));

        strategy.mutate(data, defaultContext(TARGET_CHARACTER));

        // --- Invariant: every character has exactly 6 equipped relics ---
        assertThat(equippedRelics(data, TARGET_CHARACTER)).hasSize(6);
        assertThat(equippedRelics(data, OTHER_CHARACTER)).hasSize(6);

        // --- Invariant: every character has all 6 slots ---
        assertThat(equippedSlots(data, TARGET_CHARACTER)).containsExactlyInAnyOrder(
                Slot.Head, Slot.Hands, Slot.Body, Slot.Feet,
                Slot.PlanarSphere, Slot.LinkRope);
        assertThat(equippedSlots(data, OTHER_CHARACTER)).containsExactlyInAnyOrder(
                Slot.Head, Slot.Hands, Slot.Body, Slot.Feet,
                Slot.PlanarSphere, Slot.LinkRope);

        // --- Invariant: no duplicate relic uids across equipped relics ---
        List<String> allEquippedUids = new ArrayList<>();
        allEquippedUids.addAll(
                equippedRelics(data, TARGET_CHARACTER).stream().map(Relic::getUid).toList());
        allEquippedUids.addAll(
                equippedRelics(data, OTHER_CHARACTER).stream().map(Relic::getUid).toList());
        assertThat(allEquippedUids).hasSameSizeAs(new HashSet<>(allEquippedUids));

        // --- Invariant: each relic has at most one owner ---
        for (Relic relic : data.getRelics()) {
            long ownerCount = data.getRelics().stream()
                    .filter(r -> r == relic)
                    .count();
            assertThat(ownerCount).isEqualTo(1);
        }
    }

    // -----------------------------------------------------------------------
    // Allowed / disallowed character tests
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Should use relics owned by allowed characters")
    void mutate_shouldUseRelicsFromAllowedCharacters() {
        Relic head = relic("h1", "115", Slot.Head, TARGET_CHARACTER);
        Relic hands = relic("ha1", "115", Slot.Hands, TARGET_CHARACTER);
        Relic body = relic("b1", "130", Slot.Body, TARGET_CHARACTER);
        Relic feet = relic("f1", "130", Slot.Feet, TARGET_CHARACTER);
        Relic oldSphere = relicWithMainStat("s1", "317", Slot.PlanarSphere, TARGET_CHARACTER, "ATK");
        Relic oldRope = relic("r1", "317", Slot.LinkRope, TARGET_CHARACTER);

        // New planar relics owned by an allowed character
        Relic newSphere = relicWithMainStat("s100", "999", Slot.PlanarSphere, OTHER_CHARACTER, "ATK");
        Relic newRope = relic("r100", "999", Slot.LinkRope, OTHER_CHARACTER);

        ScannedData data = dataWithRelics(List.of(
                head, hands, body, feet, oldSphere, oldRope, newSphere, newRope));

        MutationContext context = new MutationContext(
                TARGET_CHARACTER, 0, Set.of(OTHER_CHARACTER), Set.of(), random);

        strategy.mutate(data, context);

        // Should have taken the relics from the allowed character
        assertThat(newSphere.getLocation()).isEqualTo(TARGET_CHARACTER);
        assertThat(newRope.getLocation()).isEqualTo(TARGET_CHARACTER);
        // Old planars unequipped
        assertThat(oldSphere.getLocation()).isEmpty();
        assertThat(oldRope.getLocation()).isEmpty();
        // Cavern relics untouched
        assertThat(head.getLocation()).isEqualTo(TARGET_CHARACTER);
    }

    @Test
    @DisplayName("Should not use relics owned by disallowed characters")
    void mutate_shouldNotUseRelicsFromDisallowedCharacters() {
        Relic head = relic("h1", "115", Slot.Head, TARGET_CHARACTER);
        Relic hands = relic("ha1", "115", Slot.Hands, TARGET_CHARACTER);
        Relic body = relic("b1", "130", Slot.Body, TARGET_CHARACTER);
        Relic feet = relic("f1", "130", Slot.Feet, TARGET_CHARACTER);
        Relic oldSphere = relicWithMainStat("s1", "317", Slot.PlanarSphere, TARGET_CHARACTER, "ATK");
        Relic oldRope = relic("r1", "317", Slot.LinkRope, TARGET_CHARACTER);

        // New planar relics owned by a disallowed character
        Relic newSphere = relicWithMainStat("s100", "999", Slot.PlanarSphere, OTHER_CHARACTER, "ATK");
        Relic newRope = relic("r100", "999", Slot.LinkRope, OTHER_CHARACTER);

        ScannedData data = dataWithRelics(List.of(
                head, hands, body, feet, oldSphere, oldRope, newSphere, newRope));

        MutationContext context = new MutationContext(
                TARGET_CHARACTER, 0, Set.of(), Set.of(OTHER_CHARACTER), random);

        strategy.mutate(data, context);

        // Should NOT have taken the relics from the disallowed character
        assertThat(newSphere.getLocation()).isEqualTo(OTHER_CHARACTER);
        assertThat(newRope.getLocation()).isEqualTo(OTHER_CHARACTER);
        // Original planars should remain equipped
        assertThat(oldSphere.getLocation()).isEqualTo(TARGET_CHARACTER);
        assertThat(oldRope.getLocation()).isEqualTo(TARGET_CHARACTER);
    }

    // -----------------------------------------------------------------------
    // Stale snapshot regression test
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Should use live relic state, not a stale snapshot")
    void mutate_shouldUseLiveStateNotStaleSnapshot() {
        // This test verifies that the strategy iterates over data.getRelics()
        // directly rather than a cached snapshot taken before mutation.

        Relic head = relic("h1", "115", Slot.Head, TARGET_CHARACTER);
        Relic hands = relic("ha1", "115", Slot.Hands, TARGET_CHARACTER);
        Relic body = relic("b1", "130", Slot.Body, TARGET_CHARACTER);
        Relic feet = relic("f1", "130", Slot.Feet, TARGET_CHARACTER);
        Relic oldSphere = relicWithMainStat("s1", "317", Slot.PlanarSphere, TARGET_CHARACTER, "ATK");
        Relic oldRope = relic("r1", "317", Slot.LinkRope, TARGET_CHARACTER);

        Relic newSphere = relicWithMainStat("s100", "999", Slot.PlanarSphere, "", "ATK");
        Relic newRope = relic("r100", "999", Slot.LinkRope, "");

        ScannedData data = dataWithRelics(List.of(
                head, hands, body, feet, oldSphere, oldRope, newSphere, newRope));

        strategy.mutate(data, defaultContext(TARGET_CHARACTER));

        // If a stale snapshot were used, the cavern relics would have been
        // unequipped (the original bug). Verify they are still equipped.
        assertThat(head.getLocation()).isEqualTo(TARGET_CHARACTER);
        assertThat(hands.getLocation()).isEqualTo(TARGET_CHARACTER);
        assertThat(body.getLocation()).isEqualTo(TARGET_CHARACTER);
        assertThat(feet.getLocation()).isEqualTo(TARGET_CHARACTER);
    }

    // -----------------------------------------------------------------------
    // Multiple valid sets — ensure we pick one and equip correctly
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Should pick a valid set and equip both sphere and rope")
    void mutate_shouldPickValidSetAndEquipBoth() {
        Relic head = relic("h1", "115", Slot.Head, TARGET_CHARACTER);
        Relic hands = relic("ha1", "115", Slot.Hands, TARGET_CHARACTER);
        Relic body = relic("b1", "130", Slot.Body, TARGET_CHARACTER);
        Relic feet = relic("f1", "130", Slot.Feet, TARGET_CHARACTER);
        Relic oldSphere = relicWithMainStat("s1", "317", Slot.PlanarSphere, TARGET_CHARACTER, "ATK");
        Relic oldRope = relic("r1", "317", Slot.LinkRope, TARGET_CHARACTER);

        // Two valid planar sets available
        Relic sphereA = relicWithMainStat("sa", "A", Slot.PlanarSphere, "", "ATK");
        Relic ropeA = relic("ra", "A", Slot.LinkRope, "");
        Relic sphereB = relicWithMainStat("sb", "B", Slot.PlanarSphere, "", "HP");
        Relic ropeB = relic("rb", "B", Slot.LinkRope, "");

        ScannedData data = dataWithRelics(List.of(
                head, hands, body, feet, oldSphere, oldRope,
                sphereA, ropeA, sphereB, ropeB));

        strategy.mutate(data, defaultContext(TARGET_CHARACTER));

        // Should have equipped exactly one sphere and one rope from the same set
        List<Relic> equipped = equippedRelics(data, TARGET_CHARACTER);
        assertThat(equipped).hasSize(6);

        List<Relic> equippedPlanars = equipped.stream()
                .filter(r -> r.getSlot() == Slot.PlanarSphere || r.getSlot() == Slot.LinkRope)
                .toList();
        assertThat(equippedPlanars).hasSize(2);

        // Both should be from the same set
        String equippedSetId = equippedPlanars.get(0).getSetId();
        assertThat(equippedPlanars.get(1).getSetId()).isEqualTo(equippedSetId);

        // Old planars should be unequipped
        assertThat(oldSphere.getLocation()).isEmpty();
        assertThat(oldRope.getLocation()).isEmpty();
    }

    // -----------------------------------------------------------------------
    // Empty data edge cases
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Should handle empty relic list gracefully")
    void mutate_shouldHandleEmptyRelicList() {
        ScannedData data = dataWithRelics(List.of());

        // Should not throw
        strategy.mutate(data, defaultContext(TARGET_CHARACTER));

        assertThat(data.getRelics()).isEmpty();
    }

    @Test
    @DisplayName("Should handle character with no equipped relics at all")
    void mutate_shouldHandleCharacterWithNoEquippedRelics() {
        // Character has no relics equipped, but planars are available
        Relic newSphere = relicWithMainStat("s100", "999", Slot.PlanarSphere, "", "ATK");
        Relic newRope = relic("r100", "999", Slot.LinkRope, "");

        ScannedData data = dataWithRelics(List.of(newSphere, newRope));

        strategy.mutate(data, defaultContext(TARGET_CHARACTER));

        // Should equip the planars
        assertThat(newSphere.getLocation()).isEqualTo(TARGET_CHARACTER);
        assertThat(newRope.getLocation()).isEqualTo(TARGET_CHARACTER);
    }

    // -----------------------------------------------------------------------
    // Nested: Slot-level invariant tests
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("Slot completeness invariants")
    class SlotInvariants {

        @Test
        @DisplayName("Should have exactly one Head after mutation")
        void mutate_shouldHaveExactlyOneHead() {
            Relic head = relic("h1", "115", Slot.Head, TARGET_CHARACTER);
            Relic hands = relic("ha1", "115", Slot.Hands, TARGET_CHARACTER);
            Relic body = relic("b1", "130", Slot.Body, TARGET_CHARACTER);
            Relic feet = relic("f1", "130", Slot.Feet, TARGET_CHARACTER);
            Relic oldSphere = relicWithMainStat("s1", "317", Slot.PlanarSphere, TARGET_CHARACTER, "ATK");
            Relic oldRope = relic("r1", "317", Slot.LinkRope, TARGET_CHARACTER);
            Relic newSphere = relicWithMainStat("s100", "999", Slot.PlanarSphere, "", "ATK");
            Relic newRope = relic("r100", "999", Slot.LinkRope, "");

            ScannedData data = dataWithRelics(List.of(
                    head, hands, body, feet, oldSphere, oldRope, newSphere, newRope));

            strategy.mutate(data, defaultContext(TARGET_CHARACTER));

            long headCount = equippedRelics(data, TARGET_CHARACTER).stream()
                    .filter(r -> r.getSlot() == Slot.Head)
                    .count();
            assertThat(headCount).isEqualTo(1);
        }

        @Test
        @DisplayName("Should have exactly one PlanarSphere after mutation")
        void mutate_shouldHaveExactlyOnePlanarSphere() {
            Relic head = relic("h1", "115", Slot.Head, TARGET_CHARACTER);
            Relic hands = relic("ha1", "115", Slot.Hands, TARGET_CHARACTER);
            Relic body = relic("b1", "130", Slot.Body, TARGET_CHARACTER);
            Relic feet = relic("f1", "130", Slot.Feet, TARGET_CHARACTER);
            Relic oldSphere = relicWithMainStat("s1", "317", Slot.PlanarSphere, TARGET_CHARACTER, "ATK");
            Relic oldRope = relic("r1", "317", Slot.LinkRope, TARGET_CHARACTER);
            Relic newSphere = relicWithMainStat("s100", "999", Slot.PlanarSphere, "", "ATK");
            Relic newRope = relic("r100", "999", Slot.LinkRope, "");

            ScannedData data = dataWithRelics(List.of(
                    head, hands, body, feet, oldSphere, oldRope, newSphere, newRope));

            strategy.mutate(data, defaultContext(TARGET_CHARACTER));

            long sphereCount = equippedRelics(data, TARGET_CHARACTER).stream()
                    .filter(r -> r.getSlot() == Slot.PlanarSphere)
                    .count();
            assertThat(sphereCount).isEqualTo(1);
        }

        @Test
        @DisplayName("Should have exactly one LinkRope after mutation")
        void mutate_shouldHaveExactlyOneLinkRope() {
            Relic head = relic("h1", "115", Slot.Head, TARGET_CHARACTER);
            Relic hands = relic("ha1", "115", Slot.Hands, TARGET_CHARACTER);
            Relic body = relic("b1", "130", Slot.Body, TARGET_CHARACTER);
            Relic feet = relic("f1", "130", Slot.Feet, TARGET_CHARACTER);
            Relic oldSphere = relicWithMainStat("s1", "317", Slot.PlanarSphere, TARGET_CHARACTER, "ATK");
            Relic oldRope = relic("r1", "317", Slot.LinkRope, TARGET_CHARACTER);
            Relic newSphere = relicWithMainStat("s100", "999", Slot.PlanarSphere, "", "ATK");
            Relic newRope = relic("r100", "999", Slot.LinkRope, "");

            ScannedData data = dataWithRelics(List.of(
                    head, hands, body, feet, oldSphere, oldRope, newSphere, newRope));

            strategy.mutate(data, defaultContext(TARGET_CHARACTER));

            long ropeCount = equippedRelics(data, TARGET_CHARACTER).stream()
                    .filter(r -> r.getSlot() == Slot.LinkRope)
                    .count();
            assertThat(ropeCount).isEqualTo(1);
        }
    }
}
