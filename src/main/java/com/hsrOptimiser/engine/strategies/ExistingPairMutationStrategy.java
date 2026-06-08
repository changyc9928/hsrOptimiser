package com.hsrOptimiser.engine.strategies;

import com.hsrOptimiser.DTO.hsrScanner.Relic;
import com.hsrOptimiser.DTO.hsrScanner.ScannedData;
import com.hsrOptimiser.DTO.hsrScanner.Slot;
import com.hsrOptimiser.engine.MutationContext;
import com.hsrOptimiser.engine.RelicAvailabilityHelper;
import com.hsrOptimiser.engine.RelicMutationStrategy;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/**
 * Mutates one existing matched pair of cavern relics (Head/Hands/Body/Feet)
 * by replacing it with a different matching pair from the available pool.
 */
@Component
public class ExistingPairMutationStrategy implements RelicMutationStrategy {

    private static final List<Slot> CAVERN_SLOTS = List.of(
            Slot.Head, Slot.Hands, Slot.Body, Slot.Feet);

    @Override
    public void mutate(ScannedData data, MutationContext context) {
        var random = context.random();
        var characterId = context.characterId();
        var allowed = context.allowedCharacters();
        var disallowed = context.disallowedCharacters();

        List<Relic> currentEquipped = data.getRelics().stream()
                .filter(r -> characterId.equals(r.getLocation()))
                .toList();

        Set<Slot> slotSet = EnumSet.copyOf(CAVERN_SLOTS);

        Map<Slot, Relic> equippedBySlot = currentEquipped.stream()
                .filter(r -> slotSet.contains(r.getSlot()))
                .collect(Collectors.toMap(Relic::getSlot, Function.identity()));

        List<SlotPair> mutablePairs = findExistingPairs(CAVERN_SLOTS, equippedBySlot);

        if (mutablePairs.isEmpty()) {
            return;
        }

        SlotPair pairToMutate = mutablePairs.get(random.nextInt(mutablePairs.size()));

        Map<Slot, List<Relic>> availableBySlot = buildAvailableBySlot(data, slotSet, allowed, disallowed);

        List<Relic> poolA = availableBySlot.get(pairToMutate.slotA());
        List<Relic> poolB = availableBySlot.get(pairToMutate.slotB());

        if (poolA == null || poolB == null) {
            return;
        }

        PairSelection replacement = findReplacementPair(
                poolA, poolB, pairToMutate.currentSetId(), random);

        if (replacement == null) {
            return;
        }

        equippedBySlot.get(pairToMutate.slotA()).setLocation("");
        equippedBySlot.get(pairToMutate.slotB()).setLocation("");

        replacement.relicA().setLocation(characterId);
        replacement.relicB().setLocation(characterId);
    }

    private static Map<Slot, List<Relic>> buildAvailableBySlot(
            ScannedData data,
            Set<Slot> slotSet,
            Set<String> allowedSet,
            Set<String> disallowedSet) {
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

        return availableBySlot;
    }

    private static List<SlotPair> findExistingPairs(
            List<Slot> slots,
            Map<Slot, Relic> equippedBySlot) {
        List<SlotPair> result = new ArrayList<>();

        for (int i = 0; i < slots.size(); i++) {
            for (int j = i + 1; j < slots.size(); j++) {
                Relic relicA = equippedBySlot.get(slots.get(i));
                Relic relicB = equippedBySlot.get(slots.get(j));

                if (relicA == null || relicB == null) {
                    continue;
                }

                String setA = relicA.getSetId();
                if (setA != null && setA.equals(relicB.getSetId())) {
                    result.add(new SlotPair(slots.get(i), slots.get(j), setA));
                }
            }
        }

        return result;
    }

    private static PairSelection findReplacementPair(
            List<Relic> poolA,
            List<Relic> poolB,
            String currentSetId,
            java.util.Random random) {
        Set<String> setsInA = new HashSet<>();
        for (Relic relic : poolA) {
            String setId = relic.getSetId();
            if (setId != null && !setId.equals(currentSetId)) {
                setsInA.add(setId);
            }
        }

        List<String> candidateSets = new ArrayList<>();
        Set<String> seen = new HashSet<>();

        for (Relic relic : poolB) {
            String setId = relic.getSetId();
            if (setId == null
                    || setId.equals(currentSetId)
                    || !setsInA.contains(setId)
                    || !seen.add(setId)) {
                continue;
            }
            candidateSets.add(setId);
        }

        if (candidateSets.isEmpty()) {
            return null;
        }

        String chosenSet = candidateSets.get(random.nextInt(candidateSets.size()));

        Relic relicA = selectRandomRelicWithSet(poolA, chosenSet, random);
        Relic relicB = selectRandomRelicWithSet(poolB, chosenSet, random);

        return new PairSelection(relicA, relicB);
    }

    private static Relic selectRandomRelicWithSet(List<Relic> pool, String setId, java.util.Random random) {
        return RelicAvailabilityHelper.reservoirSample(
                pool,
                r -> setId.equals(r.getSetId()),
                random);
    }

    private record SlotPair(Slot slotA, Slot slotB, String currentSetId) {
    }

    private record PairSelection(Relic relicA, Relic relicB) {
    }
}
