package com.hsrOptimiser.engine.strategies;

import com.hsrOptimiser.DTO.hsrScanner.Relic;
import com.hsrOptimiser.DTO.hsrScanner.ScannedData;
import com.hsrOptimiser.DTO.hsrScanner.Slot;
import com.hsrOptimiser.engine.MutationContext;
import com.hsrOptimiser.engine.RelicAvailabilityHelper;
import com.hsrOptimiser.engine.RelicMutationStrategy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import org.springframework.stereotype.Component;

/**
 * Mutates all four cavern relic slots by selecting two matching pairs
 * from the available pool.
 */
@Component
public class CavernSetMutationStrategy implements RelicMutationStrategy {

    private static final List<Slot> CAVERN_SLOTS = List.of(
            Slot.Head, Slot.Hands, Slot.Body, Slot.Feet);

    @Override
    public void mutate(ScannedData data, MutationContext context) {
        Random random = context.random();
        String characterId = context.characterId();
        var allowedSet = context.allowedCharacters();
        var disallowedSet = context.disallowedCharacters();

        Set<Slot> slotSet = EnumSet.copyOf(CAVERN_SLOTS);

        Map<Slot, List<Relic>> availableBySlot = new EnumMap<>(Slot.class);

        for (Relic relic : data.getRelics()) {
            if (!slotSet.contains(relic.getSlot())) {
                continue;
            }
            if (relic.getRarity() != 5) {
                continue;
            }
            if (!RelicAvailabilityHelper.isAvailable(relic, allowedSet, disallowedSet)) {
                continue;
            }
            availableBySlot
                    .computeIfAbsent(relic.getSlot(), k -> new ArrayList<>())
                    .add(relic);
        }

        // Ensure every slot has at least one relic
        for (Slot slot : CAVERN_SLOTS) {
            List<Relic> pool = availableBySlot.get(slot);
            if (pool == null || pool.isEmpty()) {
                return;
            }
        }

        // Randomly partition the 4 slots into 2 pairs
        List<Slot> shuffledSlots = new ArrayList<>(CAVERN_SLOTS);
        Collections.shuffle(shuffledSlots, random);

        Slot pair1A = shuffledSlots.get(0);
        Slot pair1B = shuffledSlots.get(1);
        Slot pair2A = shuffledSlots.get(2);
        Slot pair2B = shuffledSlots.get(3);

        PairSelection pair1 = findMatchingPair(
                availableBySlot.get(pair1A),
                availableBySlot.get(pair1B),
                random);

        if (pair1 == null) {
            return;
        }

        PairSelection pair2 = findMatchingPair(
                availableBySlot.get(pair2A),
                availableBySlot.get(pair2B),
                random);

        if (pair2 == null) {
            return;
        }

        // Transaction commit phase
        List<Relic> currentEquipped = data.getRelics().stream()
                .filter(r -> characterId.equals(r.getLocation()))
                .toList();

        for (Relic relic : currentEquipped) {
            if (slotSet.contains(relic.getSlot())) {
                relic.setLocation("");
            }
        }

        pair1.relicA().setLocation(characterId);
        pair1.relicB().setLocation(characterId);
        pair2.relicA().setLocation(characterId);
        pair2.relicB().setLocation(characterId);
    }

    private static PairSelection findMatchingPair(
            List<Relic> poolA,
            List<Relic> poolB,
            Random random) {
        Set<String> setIdsInA = new HashSet<>();
        for (Relic relic : poolA) {
            String setId = relic.getSetId();
            if (setId != null) {
                setIdsInA.add(setId);
            }
        }

        List<String> matchingSetIds = new ArrayList<>();
        Set<String> seen = new HashSet<>();

        for (Relic relic : poolB) {
            String setId = relic.getSetId();
            if (setId != null && setIdsInA.contains(setId) && seen.add(setId)) {
                matchingSetIds.add(setId);
            }
        }

        if (matchingSetIds.isEmpty()) {
            return null;
        }

        String chosenSetId = matchingSetIds.get(random.nextInt(matchingSetIds.size()));

        Relic selectedA = RelicAvailabilityHelper.reservoirSample(
                poolA, r -> chosenSetId.equals(r.getSetId()), random);
        Relic selectedB = RelicAvailabilityHelper.reservoirSample(
                poolB, r -> chosenSetId.equals(r.getSetId()), random);

        return new PairSelection(selectedA, selectedB);
    }

    private record PairSelection(Relic relicA, Relic relicB) {
    }
}
