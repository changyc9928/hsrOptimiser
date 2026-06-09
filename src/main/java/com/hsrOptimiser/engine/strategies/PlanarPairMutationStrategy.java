package com.hsrOptimiser.engine.strategies;

import com.hsrOptimiser.DTO.hsrScanner.Relic;
import com.hsrOptimiser.DTO.hsrScanner.ScannedData;
import com.hsrOptimiser.DTO.hsrScanner.Slot;
import com.hsrOptimiser.clientConfig.AsagiCharacterMetadata;
import com.hsrOptimiser.engine.MutationContext;
import com.hsrOptimiser.engine.RelicAvailabilityHelper;
import com.hsrOptimiser.engine.RelicMutationStrategy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.springframework.stereotype.Component;

/**
 * Mutates the planar ornament pair (sphere + rope) by selecting a random valid
 * set that has both a
 * sphere and a rope available.
 */
@Component
public class PlanarPairMutationStrategy implements RelicMutationStrategy {

    private static Relic pickSphere(List<Relic> spheres, String attackMainStat, Random rand) {
        return RelicAvailabilityHelper.reservoirSample(
                spheres,
                r -> {
                    String stat = r.getMainstat();
                    return stat.equals(attackMainStat)
                            || stat.equals("HP")
                            || stat.equals("ATK")
                            || stat.equals("DEF");
                },
                rand);
    }

    private static Relic pickRope(List<Relic> ropes, Random rand) {
        if (ropes.isEmpty()) {
            return null;
        }
        return ropes.get(rand.nextInt(ropes.size()));
    }

    @Override
    public void mutate(ScannedData data, MutationContext context) {
        Random rand = context.random();
        String characterId = context.characterId();
        int abilityVersion = context.abilityVersion();
        var allowed = context.allowedCharacters();
        var disallowed = context.disallowedCharacters();

        String attackMainStat = AsagiCharacterMetadata
                .getInfoById(characterId, abilityVersion)
                .getAttackType() + " DMG Boost";

        // Build set pools
        Map<String, PlanarSet> sets = new HashMap<>();

        for (Relic r : data.getRelics()) {
            if (r.getSetId() == null || r.getRarity() != 5) {
                continue;
            }
            if (r.getSlot() != Slot.PlanarSphere && r.getSlot() != Slot.LinkRope) {
                continue;
            }
            if (!RelicAvailabilityHelper.isAvailable(r, allowed, disallowed)) {
                continue;
            }

            PlanarSet set = sets.computeIfAbsent(r.getSetId(), k -> new PlanarSet());
            if (r.getSlot() == Slot.PlanarSphere) {
                set.spheres().add(r);
            } else {
                set.ropes().add(r);
            }
        }

        List<String> validSets = new ArrayList<>();
        for (var e : sets.entrySet()) {
            if (!e.getValue().spheres().isEmpty() && !e.getValue().ropes().isEmpty()) {
                validSets.add(e.getKey());
            }
        }

        Collections.shuffle(validSets, rand);

        for (String setId : validSets) {
            PlanarSet set = sets.get(setId);

            Relic sphere = pickSphere(set.spheres(), attackMainStat, rand);
            Relic rope = pickRope(set.ropes(), rand);

            if (sphere == null || rope == null) {
                continue;
            }

            replacePlanarPair(data, characterId, sphere, rope);

            return;
        }
    }

    /**
     * Unequips only the planar relics (PlanarSphere and LinkRope) currently
     * equipped by the given character, then equips the new sphere and rope.
     *
     * <p>
     * This method operates on the live relic list so it never uses a stale
     * snapshot, and it only touches planar slots so cavern relics (Head/Hands/
     * Body/Feet) are never disturbed.
     */
    private void replacePlanarPair(
            ScannedData data,
            String characterId,
            Relic newSphere,
            Relic newRope) {

        for (Relic r : data.getRelics()) {
            if (!characterId.equals(r.getLocation())) {
                continue;
            }
            if (r.getSlot() == Slot.PlanarSphere || r.getSlot() == Slot.LinkRope) {
                r.setLocation("");
            }
        }

        newSphere.setLocation(characterId);
        newRope.setLocation(characterId);
    }

    private record PlanarSet(
            List<Relic> spheres,
            List<Relic> ropes) {

        PlanarSet() {
            this(new ArrayList<>(), new ArrayList<>());
        }
    }
}
