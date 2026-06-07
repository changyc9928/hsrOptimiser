package com.hsrOptimiser.engine;

import com.hsrOptimiser.DTO.asagi.CharactersItem;
import com.hsrOptimiser.DTO.asagi.MocRequest;
import com.hsrOptimiser.DTO.asagi.MocResponse;
import com.hsrOptimiser.DTO.asagi.TItem;
import com.hsrOptimiser.DTO.asagi.TotalSubStats;
import com.hsrOptimiser.DTO.hsrScanner.HSRCharacter;
import com.hsrOptimiser.DTO.hsrScanner.Relic;
import com.hsrOptimiser.DTO.hsrScanner.ScannedData;
import com.hsrOptimiser.DTO.hsrScanner.Slot;
import com.hsrOptimiser.client.AsagiClient;
import com.hsrOptimiser.clientConfig.AsagiCharacterMetadata;
import com.hsrOptimiser.mapper.AsagiCharactersItemMapper;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.SerializationUtils;
import org.springframework.util.StringUtils;

@Component
@Slf4j
@RequiredArgsConstructor
public class SimulatedAnnealing {

    private static final int MAX_CONSECUTIVE_FAILURES = 100;
    // Converted to Set for O(1) performance lookup
    // Map for mapping sub-stat strings to their target builder methods cleanly
    private static final Map<String, BiConsumer<TotalSubStats, Double>> STAT_MAPPERS = Map.ofEntries(
        Map.entry("ATK", TotalSubStats::addAtkFlat),
        Map.entry("HP", TotalSubStats::addHpFlat),
        Map.entry("DEF", TotalSubStats::addDefFlat),
        Map.entry("SPD", TotalSubStats::addSpdFlat),
        Map.entry("ATK_", TotalSubStats::addAtkRate),
        Map.entry("HP_", TotalSubStats::addHpRate),
        Map.entry("DEF_", TotalSubStats::addDefRate),
        Map.entry("CRIT Rate_", TotalSubStats::addCritRate),
        Map.entry("CRIT DMG_", TotalSubStats::addCritDmg),
        Map.entry("Effect RES_", TotalSubStats::addEffectRes),
        Map.entry("Effect Hit Rate_", TotalSubStats::addEffectHit),
        Map.entry("Break Effect_", TotalSubStats::addBreakRate)
    );
    private final AsagiCharactersItemMapper asagiCharacterMapper;
    @Value("${simulated-annealing.initial-temperature}")
    private double initialTemperature;
    @Value("${simulated-annealing.cooling-rate}")
    private double tempCoolingRate;
    @Value("${simulated-annealing.epoch}")
    private int epoch;

    private static boolean decideToAccept(double totalDamage, double currentDamage,
        double temperature) {
        double deltaE = totalDamage - currentDamage;
        if (deltaE > 0) {
            return true; // Always accept damage gains
        }

        // Scale raw damage delta against a standard optimization unit (e.g., 100,000 damage)
        // Adjust 100000.0 to match your preferred tolerance threshold
        double scaledDelta = deltaE / 100000.0;

        double probability = Math.exp(scaledDelta / temperature);
        return ThreadLocalRandom.current().nextDouble() < probability;
    }

    private static void mutateRelics(ScannedData data, String characterId, int abilityVersion,
        List<String> allowedToScrapRelicsCharacterIds,
        List<String> disallowedToScrapRelicsCharacterIds) {
        List<Relic> currentEquipped = data.getRelics().stream()
            .filter(r -> characterId.equals(r.getLocation()))
            .toList();

        List<Slot> cavernSlots = List.of(Slot.Head, Slot.Hands, Slot.Body, Slot.Feet);
        List<Slot> planarSlots = List.of(Slot.PlanarSphere, Slot.LinkRope);

        double roll = ThreadLocalRandom.current().nextDouble();
        if (roll < 0.33) {
            mutateSingleRelic(data, characterId, abilityVersion,
                new HashSet<>(allowedToScrapRelicsCharacterIds),
                new HashSet<>(disallowedToScrapRelicsCharacterIds));
        } else if (roll < 0.66) {
            mutatePlanarPair(data, characterId, abilityVersion, planarSlots, currentEquipped,
                allowedToScrapRelicsCharacterIds, disallowedToScrapRelicsCharacterIds);
        } else {
            mutateCavernSets(data, characterId, cavernSlots, currentEquipped,
                allowedToScrapRelicsCharacterIds, disallowedToScrapRelicsCharacterIds);
        }
    }

    static void mutateSingleRelic(
        ScannedData data,
        String characterId,
        int abilityVersion,
        Set<String> allowedToScrapRelicsCharacterIds,
        Set<String> disallowedToScrapRelicsCharacterIds) {

        mutateSingleRelic(
            data,
            characterId,
            abilityVersion,
            allowedToScrapRelicsCharacterIds,
            disallowedToScrapRelicsCharacterIds,
            ThreadLocalRandom.current());
    }

    static void mutateSingleRelic(
        ScannedData data,
        String characterId,
        int abilityVersion,
        Set<String> allowedToScrapRelicsCharacterIds,
        Set<String> disallowedToScrapRelicsCharacterIds,
        Random rand) {

        List<Relic> relics = data.getRelics();

        Relic sourceRelic = null;
        int ownedCount = 0;

        for (Relic r : relics) {
            if (characterId.equals(r.getLocation())) {
                ownedCount++;
                if (rand.nextInt(ownedCount) == 0) {
                    sourceRelic = r;
                }
            }
        }

        if (sourceRelic == null) {
            return;
        }

        String setId = sourceRelic.getSetId();
        Slot slot = sourceRelic.getSlot();

        String mainstat =
            AsagiCharacterMetadata.getInfoById(characterId, abilityVersion)
                .getAttackType() + " DMG Boost";

        Set<String> allowedSphereStats =
            Set.of(mainstat, "ATK", "DEF", "HP");

        Relic candidate = null;
        int matches = 0;

        for (Relic r : relics) {

            // Allow the source relic itself to participate in the pool.
            boolean scrapable =
                r == sourceRelic
                    || r.getLocation() == null
                    || r.getLocation().isBlank()
                    || (allowedToScrapRelicsCharacterIds.contains(r.getLocation())
                    && !disallowedToScrapRelicsCharacterIds.contains(r.getLocation()));

            if (!scrapable) {
                continue;
            }

            if (r.getSlot() != slot) {
                continue;
            }

            if (slot == Slot.PlanarSphere
                && !allowedSphereStats.contains(r.getMainstat())) {
                continue;
            }

            if (r.getRarity() != 5) {
                continue;
            }

            if (!Objects.equals(r.getSetId(), setId)) {
                continue;
            }

            matches++;
            if (rand.nextInt(matches) == 0) {
                candidate = r;
            }
        }

        if (candidate == null || candidate == sourceRelic) {
            return;
        }

        sourceRelic.setLocation("");
        candidate.setLocation(characterId);
    }

    private static void mutatePlanarPair(ScannedData data, String characterId, int abilityVersion,
        List<Slot> slots,
        List<Relic> currentEquipped,
        List<String> allowedToScrapRelicsCharacterIds,
        List<String> disallowedToScrapRelicsCharacterIds) {
        ThreadLocalRandom rand = ThreadLocalRandom.current();

        // 1. Snapshot original locations
        Map<Relic, String> originalLocations = currentEquipped.stream()
            .filter(r -> slots.contains(r.getSlot()))
            .collect(Collectors.toMap(r -> r, Relic::getLocation, (v1, v2) -> v1));

        // 2. Clear current equipment tags to free up room
        originalLocations.keySet().forEach(r -> r.setLocation(""));

        // 3. Build the pool grouping by SetId
        Map<String, List<Relic>> planarPoolBySet = data.getRelics().stream()
            .filter(r -> slots.contains(r.getSlot()) && r.getSetId() != null)
            .filter(r -> (!StringUtils.hasText(r.getLocation()) || (
                allowedToScrapRelicsCharacterIds.contains(r.getLocation()) &&
                    !disallowedToScrapRelicsCharacterIds.contains(r.getLocation())))
                && r.getRarity() == 5)
            .collect(Collectors.groupingBy(Relic::getSetId));

        // 4. Find valid candidate sets containing both pieces
        List<String> validSetIds = planarPoolBySet.entrySet().stream()
            .filter(entry -> hasSlot(entry.getValue(), Slot.PlanarSphere) &&
                hasSlot(entry.getValue(), Slot.LinkRope))
            .map(Map.Entry::getKey)
            .collect(Collectors.toCollection(ArrayList::new)); // Mutable array list for shuffling

        if (validSetIds.isEmpty()) {
            originalLocations.forEach(Relic::setLocation);
            return;
        }

        // 5. Shuffle the pool of set IDs so we can step through them pseudo-randomly
        Collections.shuffle(validSetIds, rand);
        boolean mutationSuccessful = false;

        for (String chosenSetId : validSetIds) {
            List<Relic> setPool = planarPoolBySet.get(chosenSetId);

            // Attempt to equip both slots using our updated boolean method
            boolean sphereSuccess = equipRandomForSlot(setPool, Slot.PlanarSphere, characterId,
                abilityVersion);
            boolean ropeSuccess = equipRandomForSlot(setPool, Slot.LinkRope, characterId,
                abilityVersion);

            if (sphereSuccess && ropeSuccess) {
                mutationSuccessful = true;
                break; // Found a valid set matching your main stat constraints!
            } else {
                // Partial failure clean-up: clear out anything allocated from this failed set
                setPool.stream()
                    .filter(r -> characterId.equals(r.getLocation()))
                    .forEach(r -> r.setLocation(""));
            }
        }

        // 6. Absolute Fallback: If no sets had pieces with your allowed main stats, restore the original setup
        if (!mutationSuccessful) {
            originalLocations.forEach(Relic::setLocation);
        }
    }

    private static void mutateCavernSets(ScannedData data, String characterId, List<Slot> slots,
        List<Relic> currentEquipped,
        List<String> allowedToScrapRelicsCharacterIds,
        List<String> disallowedToScrapRelicsCharacterIds) {
        ThreadLocalRandom rand = ThreadLocalRandom.current();
        Map<Relic, String> originalLocations = currentEquipped.stream()
            .filter(r -> slots.contains(r.getSlot()))
            .collect(Collectors.toMap(r -> r, Relic::getLocation, (v1, v2) -> v1));

        originalLocations.keySet().forEach(r -> r.setLocation(""));

        Map<Slot, List<Relic>> availableBySlot = data.getRelics().stream()
            .filter(r -> slots.contains(r.getSlot()))
            .filter(
                r -> (!StringUtils.hasText(r.getLocation()) || (
                    allowedToScrapRelicsCharacterIds.contains(
                        r.getLocation()) && !disallowedToScrapRelicsCharacterIds.contains(
                        r.getLocation()))) && r.getRarity() == 5)
            .collect(Collectors.groupingBy(Relic::getSlot));

        for (Slot slot : slots) {
            if (!availableBySlot.containsKey(slot) || availableBySlot.get(slot).isEmpty()) {
                originalLocations.forEach(Relic::setLocation);
                return;
            }
        }

        List<Slot> shuffledSlots = new ArrayList<>(slots);
        Collections.shuffle(shuffledSlots, rand);

        Slot pair1A = shuffledSlots.get(0);
        Slot pair1B = shuffledSlots.get(1);
        Slot pair2A = shuffledSlots.get(2);
        Slot pair2B = shuffledSlots.get(3);

        boolean pair1Success = equipMatchingPair(availableBySlot.get(pair1A),
            availableBySlot.get(pair1B), characterId);
        boolean pair2Success = equipMatchingPair(availableBySlot.get(pair2A),
            availableBySlot.get(pair2B), characterId);

        if (!pair1Success) {
            equipAnyRandom(availableBySlot.get(pair1A), characterId);
            equipAnyRandom(availableBySlot.get(pair1B), characterId);
        }
        if (!pair2Success) {
            equipAnyRandom(availableBySlot.get(pair2A), characterId);
            equipAnyRandom(availableBySlot.get(pair2B), characterId);
        }
    }

    private static boolean equipMatchingPair(List<Relic> poolA, List<Relic> poolB,
        String characterId) {
        ThreadLocalRandom rand = ThreadLocalRandom.current();
        Set<String> setsInA = poolA.stream().map(Relic::getSetId).filter(Objects::nonNull)
            .collect(Collectors.toSet());
        List<String> matchingSets = poolB.stream().map(Relic::getSetId)
            .filter(id -> id != null && setsInA.contains(id)).distinct().toList();

        if (matchingSets.isEmpty()) {
            return false;
        }

        String chosenSetId = matchingSets.get(rand.nextInt(matchingSets.size()));
        List<Relic> filteredA = poolA.stream().filter(r -> chosenSetId.equals(r.getSetId()))
            .toList();
        List<Relic> filteredB = poolB.stream().filter(r -> chosenSetId.equals(r.getSetId()))
            .toList();

        filteredA.get(rand.nextInt(filteredA.size())).setLocation(characterId);
        filteredB.get(rand.nextInt(filteredB.size())).setLocation(characterId);
        return true;
    }

    private static boolean equipRandomForSlot(List<Relic> pool, Slot slot, String characterId,
        int abilityVersion) {
        List<Relic> filtered = pool.stream().filter(r -> {
            if (slot == Slot.PlanarSphere) {
                String mainstat =
                    AsagiCharacterMetadata.getInfoById(characterId, abilityVersion).getAttackType()
                        + " DMG Boost";
                return r.getSlot() == slot && List.of(mainstat, "HP", "ATK", "DEF")
                    .contains(r.getMainstat());
            }
            return r.getSlot() == slot;
        }).toList();

        if (filtered.isEmpty()) {
            return false; // Tell caller the mutation cannot proceed cleanly
        }

        filtered.get(ThreadLocalRandom.current().nextInt(filtered.size())).setLocation(characterId);
        return true;
    }

    private static void equipAnyRandom(List<Relic> pool, String characterId) {
        pool.get(ThreadLocalRandom.current().nextInt(pool.size())).setLocation(characterId);
    }

    private static boolean hasSlot(List<Relic> relics, Slot slot) {
        return relics.stream().anyMatch(r -> r.getSlot() == slot);
    }

    private static TotalSubStats getTotalSubStatsOfACharacter(String characterId,
        ScannedData scannedData) {
        HSRCharacter hsrCharacter = scannedData.getCharacters().stream()
            .filter(hsrCharacter1 -> hsrCharacter1.getId().equals(characterId)).findFirst()
            .orElseThrow();
        TotalSubStats totalSubStats = new TotalSubStats();
        totalSubStats.setKey(
            AsagiCharacterMetadata.getInfoById(characterId, hsrCharacter.getAbilityVersion())
                .getDisplayName());

        scannedData.getRelics().stream()
            .filter(r -> characterId.equals(r.getLocation()))
            .flatMap(r -> r.getSubstats().stream())
            .forEach(subStat -> {
                BiConsumer<TotalSubStats, Double> mapper = STAT_MAPPERS.get(subStat.getKey());
                if (mapper != null) {
                    mapper.accept(totalSubStats, subStat.getValue());
                } else {
                    log.warn("Unexpected substat key encountered: {}", subStat.getKey());
                }
            });

        scannedData.getRelics().stream()
            .filter(r -> characterId.equals(r.getLocation()) && r.getPreviewSubstats() != null)
            .flatMap(r -> r.getPreviewSubstats().stream())
            .forEach(subStat -> {
                BiConsumer<TotalSubStats, Double> mapper = STAT_MAPPERS.get(subStat.getKey());
                if (mapper != null) {
                    mapper.accept(totalSubStats, subStat.getValue());
                } else {
                    log.warn("Unexpected substat key encountered: {}", subStat.getKey());
                }
            });

        return totalSubStats;
    }

    public SimulationResult simulateAnnealing(ScannedData data, AsagiClient asagiClient,
        List<String> characterIds, List<String> fixedCharacterIds,
        List<String> allowedToScrapRelicsCharacterIds,
        List<String> disallowedToScrapRelicsCharacterIds) {
        log.info("Starting Simulated Annealing optimization. Target Epochs: {}, Initial Temp: {}",
            epoch, initialTemperature);

        ScannedData scannedData = SerializationUtils.clone(data);
        MocRequest mocRequest = createBaseMocRequest();

        ScannedData finalScannedData = scannedData;

        mocRequest.setCharacters(characterIds.stream()
            .map(characterId -> asagiCharacterMapper.map(finalScannedData, characterId)).toList());

        int i = 0;
        int totalSteps = 0;
        int consecutiveFailures = 0;
        double currentDamage = 0;
        double temperature = initialTemperature;

        ScannedData snapshot = SerializationUtils.clone(scannedData);

        while (i < epoch) {
            totalSteps++;
            ScannedData finalScannedData1 = scannedData;
            mocRequest.setTotalSubStatus(
                characterIds.stream()
                    .map(
                        characterId -> getTotalSubStatsOfACharacter(characterId, finalScannedData1))
                    .toList()
            );

            for (CharactersItem c : mocRequest.getCharacters()) {
                asagiCharacterMapper.applyRelics(c, scannedData, c.getId());
            }

            long startTime = System.currentTimeMillis();

            try {
                validateRelicSets(mocRequest);

                MocResponse mocResponse = asagiClient.calculateDamage(mocRequest);
                double totalDamage = mocResponse.getT().stream().mapToDouble(TItem::getTotal).sum();

                double delta = totalDamage - currentDamage;
                double activeTemp = temperature;

                if (decideToAccept(totalDamage, currentDamage, activeTemp)) {
                    i++;
                    consecutiveFailures = 0;
                    currentDamage = totalDamage;

                    temperature *= tempCoolingRate;
                    snapshot = SerializationUtils.clone(scannedData);

                    log.info(String.format(
                        "Epoch [%d/%d], Step [%d] | Temp: %.4f | Accepted Damage: %.2f (Delta: %+.2f)",
                        i, epoch, totalSteps, activeTemp, currentDamage, delta));

                    if (i >= epoch) {
                        log.info(
                            "================================================================");
                        log.info(String.format(
                            "Optimization Completed! Final Damage: %.2f after %d total mutations.",
                            currentDamage, totalSteps));
                        log.info(
                            "================================================================");
                        return new SimulationResult(snapshot, mocResponse);
                    }
                } else {
                    scannedData = SerializationUtils.clone(snapshot);

                    if (totalSteps % 50
                        == 0) { // Increased visibility for skips since steps are slower
                        log.info(String.format(
                            "Epoch [%d/%d], Step [%d] | Temp: %.4f | [Skipped] Current Baseline: %.2f",
                            i + 1, epoch, totalSteps, activeTemp, currentDamage));
                    }
                }

                // Adaptive Throttling: If downstream calculator took > 1.5s, give it breathing room
                long duration = System.currentTimeMillis() - startTime;
                if (duration > 1500) {
                    Thread.sleep(150);
                }

            } catch (Exception e) {
                consecutiveFailures++;
                log.warn(String.format(
                    "Simulation step dropped due to error: %s. Consecutive failures: %d",
                    e.getMessage(), consecutiveFailures));
                scannedData = SerializationUtils.clone(snapshot);

                // Exponential backoff on server error to let the external API stabilize
                try {
                    long backoffTime = Math.min(2000, 200L * consecutiveFailures);
                    Thread.sleep(backoffTime);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }

            // Inline ThreadLocalRandom allocation to prevent thread-locality context leak
            List<String> filteredCharacters = characterIds.stream().filter(
                characterId -> !fixedCharacterIds.contains(characterId)).toList();
            String randomChar = filteredCharacters.get(
                ThreadLocalRandom.current().nextInt(filteredCharacters.size()));
            HSRCharacter character = scannedData.getCharacters().stream()
                .filter(hsrCharacter -> hsrCharacter.getId().equals(randomChar)).findFirst()
                .orElseThrow();
            mutateRelics(scannedData, randomChar, character.getAbilityVersion(),
                allowedToScrapRelicsCharacterIds,
                disallowedToScrapRelicsCharacterIds);
        }

        log.error("Simulation completed without meeting termination conditions.");
        return null;
    }

    private void validateRelicSets(MocRequest mocRequest) {
        boolean missingSets = mocRequest.getCharacters().stream().anyMatch(item ->
            item.getRelicSet() == null || item.getRelicSet().getOrnament() == null ||
                item.getRelicSet().getSet1() == null || item.getRelicSet().getSet2() == null
        );

        if (missingSets) {
            throw new IllegalStateException(
                "Invalid relic configuration: One or more character items contain null relic sets.");
        }
    }

    private MocRequest createBaseMocRequest() {
        MocRequest request = new MocRequest();
        request.setBattleMode("multi");
        request.setRound(6);
//        request.setSimMode("moc");
//        request.setMocBuff("MocStage250");
//        request.setMocScenario("12b");
//        request.setWaveSet(List.of(
//            List.of("SilvermaneCannoneer", "BlazeOfSpace", "SilvermaneLieutenant",
//                "SilvermaneCannoneer"),
//            List.of("AventurineEnemy")
//        ));

        List<String> universalWeaks = List.of("Physical", "Fire", "Ice", "Wind", "Lightning",
            "Imaginary", "Quantum");
        request.setBossWeaks(universalWeaks);
        request.setBoss2Weaks(universalWeaks);
        request.setFollowerWeaks(universalWeaks);

        request.setStartSp(3);
        request.setDistributeEp(true);
        request.setHealerSpeed(134);
        request.setEnemyLevel("base90");
        request.setInputActualNumber(true);
        request.setBronyaStrategy(1);
        request.setTopazStrategy(0);
        request.setBoothillStrategy(2);
        request.setEnableBreak(true);
        request.setFixBreak(true);

        return request;
    }

    public record SimulationResult(ScannedData data, MocResponse mocResponse) {

    }
}