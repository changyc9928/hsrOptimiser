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
import com.hsrOptimiser.utils.SubStatAggregator;
import com.hsrOptimiser.clientConfig.AsagiCharacterMetadata;
import com.hsrOptimiser.engine.strategies.FullLoadoutMutationStrategy;
import com.hsrOptimiser.mapper.AsagiCharactersItemMapper;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.SerializationUtils;

@Component
@Slf4j
@RequiredArgsConstructor
public class SimulatedAnnealing {

    private final AsagiCharactersItemMapper asagiCharacterMapper;
    private final MocRequestFactory mocRequestFactory;
    private final MutationStrategySelector mutationSelector;
    private final FullLoadoutMutationStrategy fullLoadoutMutationStrategy;

    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;

    @Value("${simulated-annealing.initial-temperature-factor:0.05}")
    private double initialTemperatureFactor;

    @Value("${simulated-annealing.cooling-rate}")
    private double tempCoolingRate;

    @Value("${simulated-annealing.epoch}")
    private int epoch;

    @Value("${simulated-annealing.steps-per-epoch:100}")
    private int stepsPerEpoch;

    @Value("${simulated-annealing.min-steps:120}")
    private int minSteps;

    @Value("${simulated-annealing.patience:100}")
    private int patience;

    @Value("${simulated-annealing.polish-temperature-factor:0.002}")
    private double polishTemperatureFactor;

    @Value("${simulated-annealing.search-round:3}")
    private int searchRound;

    @Value("${simulated-annealing.final-round:6}")
    private int finalRound;

    @Value("${simulated-annealing.refine-fraction:0.25}")
    private double refineFraction;

    public void publishProgress(String jobId, AnnealingState state, double bestDamage) {
        if (jobId == null || redisTemplate == null) {
            return;
        }
        try {
            var entries = new java.util.LinkedHashMap<String, Object>();
            entries.put("total_steps", state.getTotalSteps());
            entries.put("current_epoch", state.getCurrentEpoch());
            entries.put("total_epochs", state.getTotalEpochs());
            entries.put("temperature", state.getTemperature());
            entries.put("current_damage", state.getCurrentDamage());
            // The run walks downhill on purpose, so the peak is reported separately
            // from the live value to avoid a finished job looking like a regression.
            entries.put("best_damage", Double.isFinite(bestDamage) ? bestDamage : 0);
            redisTemplate.opsForHash().putAll("evaluate:progress:" + jobId, entries);
        } catch (Exception e) {
            log.warn("Failed to publish SA progress: {}", e.getMessage());
        }
    }

    /**
     * Metropolis acceptance criterion for Simulated Annealing.
     */
    static boolean decideToAccept(
        double newDamage,
        double currentDamage,
        double temperature
    ) {
        // Always accept better solutions
        if (newDamage > currentDamage) {
            return true;
        }

        double deltaE = newDamage - currentDamage;

        // Avoid division by zero / freezing issues
        if (temperature <= 1e-12) {
            return false;
        }

        double probability = Math.exp(deltaE / temperature);

        log.debug("Temperature: {}, deltaE: {}, Probability: {}", temperature, deltaE, probability);

        return ThreadLocalRandom.current().nextDouble() < probability;
    }

    private static int clampRound(int round) {
        return Math.max(1, Math.min(8, round));
    }

    /**
     * Chooses the simulation round for a step. The search runs on a short horizon to keep each
     * remote evaluation cheap, then the last {@code refineFraction} of the budget switches back to
     * the full horizon so the final candidates are judged on the real objective.
     */
    static int roundForStep(int step, int totalSteps, double refineFraction, int searchRound,
        int finalRound) {
        double fraction = Math.max(0, Math.min(1, refineFraction));
        int refineStart = (int) Math.floor(totalSteps * (1 - fraction));
        return clampRound(step > refineStart ? finalRound : searchRound);
    }

    /**
     * Canonical signature of the equipped relics for the team. Two states with the same signature
     * produce the same request, so the second one can reuse the first damage response.
     */
    static String buildRelicSignature(ScannedData data, List<String> characterIds) {
        Map<String, List<String>> entriesByCharacter = new LinkedHashMap<>();
        for (String characterId : characterIds) {
            entriesByCharacter.put(characterId, new ArrayList<>());
        }
        for (Relic relic : data.getRelics()) {
            List<String> entries = entriesByCharacter.get(relic.getLocation());
            if (entries == null) {
                continue;
            }
            String identity = relic.getUid() != null
                ? relic.getUid()
                : relic.getSlot() + ":" + relic.getSetId() + ":" + relic.getMainstat();
            entries.add(identity);
        }
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, List<String>> entry : entriesByCharacter.entrySet()) {
            entry.getValue().sort(Comparator.naturalOrder());
            builder.append(entry.getKey()).append(':')
                .append(String.join(",", entry.getValue())).append(';');
        }
        return builder.toString();
    }

    private record CachedEvaluation(double damage, MocResponse response) {

    }

    private static void validateRelicSets(MocRequest mocRequest) {
        if (!isRelicSetValid(mocRequest)) {
            throw new IllegalStateException(
                "Invalid relic configuration: One or more character items contain null relic sets.");
        }
    }

    private static boolean isRelicSetValid(MocRequest mocRequest) {
        return mocRequest.getCharacters().stream()
            .allMatch(item -> item.getRelicSet() != null
                && item.getRelicSet().getOrnament() != null
                && item.getRelicSet().getSet1() != null
                && item.getRelicSet().getSet2() != null);
    }

    static List<String> findCharactersWithoutCones(MocRequest mocRequest,
        List<String> characterIds) {
        List<String> missing = new ArrayList<>();
        for (int i = 0; i < mocRequest.getCharacters().size() && i < characterIds.size(); i++) {
            if (mocRequest.getCharacters().get(i).getLightCones() == null) {
                missing.add(characterIds.get(i));
            }
        }
        return missing;
    }

    private static final Pattern REJECTED_CONE = Pattern.compile(
        "\\{\"type\":\"enum\",\"loc\":\\[\"body\",\"characters\",(\\d+),\"light_cones\"\\]"
            + "[^}]*?\"input\":\"([^\"]+)\"");

    private static String truncate(String message, int maxLength) {
        if (message == null) {
            return "unknown error";
        }
        return message.length() <= maxLength ? message
            : message.substring(0, maxLength) + "...";
    }

    /**
     * Runs one damage evaluation before the annealing loop so broken input
     * (e.g. a light cone the simulator does not support) fails the job
     * immediately with a readable error instead of spinning forever.
     */
    private MocResponse preflightCheck(MocRequest mocRequest, AsagiClient asagiClient,
        List<String> characterIds, ScannedData data) {
        if (!isRelicSetValid(mocRequest)) {
            log.info("Skipping pre-flight damage check: initial relic sets incomplete");
            return null;
        }
        List<String> missingCones = findCharactersWithoutCones(mocRequest, characterIds);
        if (!missingCones.isEmpty()) {
            throw new IllegalStateException(
                "Pre-flight damage check failed: no light cone equipped on character(s): "
                    + String.join(", ", missingCones)
                    + ". Equip a light cone on each team member and retry.");
        }
        try {
            MocResponse response = asagiClient.calculateDamage(mocRequest);
            log.info("Pre-flight damage check passed");
            return response;
        } catch (Exception e) {
            log.warn("Pre-flight request cones: {}", mocRequest.getCharacters().stream()
                .map(item -> item.getKey() + "=" + item.getLightCones())
                .toList());
            throw new IllegalStateException(
                describeRejection(e.getMessage(), characterIds, data), e);
        }
    }

    static String describeRejection(String message, List<String> characterIds,
        ScannedData data) {
        StringBuilder details = new StringBuilder();
        if (message != null) {
            Matcher matcher = REJECTED_CONE.matcher(message);
            while (matcher.find()) {
                int index = Integer.parseInt(matcher.group(1));
                String rejectedCone = matcher.group(2);
                String characterId = index < characterIds.size()
                    ? characterIds.get(index)
                    : "index " + index;
                String equipped = "";
                if (data != null && data.getLightCones() != null) {
                    equipped = data.getLightCones().stream()
                        .filter(cone -> characterId.equals(cone.getLocation()))
                        .map(cone -> cone.getName() == null
                            ? cone.getId()
                            : cone.getName() + " (" + cone.getId() + ")")
                        .findFirst()
                        .map(name -> ", equipped cone: " + name)
                        .orElse("");
                }
                details.append("Character ").append(characterId)
                    .append(" sends light cone '").append(rejectedCone)
                    .append("' which the damage simulator rejected").append(equipped)
                    .append(". ");
            }
        }
        if (details.length() == 0) {
            return "Pre-flight damage check failed: " + truncate(message, 500);
        }
        return "Pre-flight damage check failed. " + details
            + "Unequip or replace the rejected light cone(s) and retry.";
    }

    /**
     * Runs the simulated annealing optimization loop.
     */
    public SimulationResult simulateAnnealing(
        ScannedData data,
        AsagiClient asagiClient,
        List<String> characterIds,
        List<String> fixedCharacterIds,
        List<String> allowedToScrapRelicsCharacterIds,
        List<String> disallowedToScrapRelicsCharacterIds,
        String jobId) {
        int totalBudget = Math.max(1, epoch) * Math.max(1, stepsPerEpoch);
        int refineStart = (int) Math.floor(
            totalBudget * (1 - Math.max(0, Math.min(1, refineFraction))));
        if (clampRound(searchRound) == clampRound(finalRound)) {
            log.info("Starting Simulated Annealing optimization. Budget: {} steps @ round {} | "
                    + "Initial Temp: {}% of team damage",
                totalBudget, clampRound(finalRound), initialTemperatureFactor * 100);
        } else {
            log.info(
                "Starting Simulated Annealing optimization. Budget: {} steps ({} search @ round {} "
                    + "+ {} refine @ round {}) | Initial Temp: {}% of team damage",
                totalBudget, refineStart, clampRound(searchRound),
                totalBudget - refineStart, clampRound(finalRound),
                initialTemperatureFactor * 100);
        }

        ScannedData workingData = SerializationUtils.clone(data);
        repairIncompleteLoadouts(workingData, characterIds,
            allowedToScrapRelicsCharacterIds, disallowedToScrapRelicsCharacterIds);

        MocRequest mocRequest = mocRequestFactory.createBaseRequest();
        mocRequest.setRound(clampRound(searchRound));

        // Capture in a final variable for the lambda; workingData itself
        // will be reassigned during rollback/retry below.
        final ScannedData initialData = workingData;
        mocRequest.setCharacters(characterIds.stream()
            .map(characterId -> asagiCharacterMapper.map(initialData, characterId))
            .toList());

        updateRequestWithSubstats(mocRequest, workingData, characterIds);
        long preflightStart = System.nanoTime();
        MocResponse initialResponse = preflightCheck(mocRequest, asagiClient, characterIds,
            initialData);
        if (initialResponse != null) {
            log.info("Initial team damage: {} (pre-flight {} ms)",
                totalDamage(initialResponse),
                (System.nanoTime() - preflightStart) / 1_000_000);
        }

        log.info("Relic candidates: {} relics in the scan", workingData.getRelics().size());

        AnnealingState state = new AnnealingState(epoch, stepsPerEpoch, initialTemperatureFactor,
            tempCoolingRate);
        RetryPolicy retryPolicy = new RetryPolicy();
        ScannedData snapshot = SerializationUtils.clone(workingData);
        ScannedData bestSnapshot = SerializationUtils.clone(workingData);
        // Seed the peak with the equipped build so a failed search can never return something
        // worse than what the user already owns.
        MocResponse bestResponse = initialResponse;
        double bestDamage = initialResponse == null
            ? Double.NEGATIVE_INFINITY
            : totalDamage(initialResponse);
        int bestRound = initialResponse == null ? -1 : clampRound(searchRound);

        Map<String, CachedEvaluation> evaluationCache = new HashMap<>();
        int remoteEvaluations = 0;
        int cacheHits = 0;
        long remoteNanos = 0;
        int lastImprovementStep = 0;
        boolean polishing = false;
        String stopReason = "budget exhausted";

        while (state.hasRemainingEpochs()) {
            state.incrementSteps();

            int round = roundForStep(state.getTotalSteps(), totalBudget, refineFraction,
                searchRound, finalRound);
            mocRequest.setRound(round);

            updateRequestWithSubstats(mocRequest, workingData, characterIds);
            applyRelicsToRequest(mocRequest, workingData);

            if (!isRelicSetValid(mocRequest)) {
                log.debug("Skipping MoC evaluation: incomplete relic set configuration after mutation");
                workingData = SerializationUtils.clone(snapshot);
                retryPolicy.recordSuccess();
                performMutation(
                    workingData,
                    characterIds,
                    fixedCharacterIds,
                    allowedToScrapRelicsCharacterIds,
                    disallowedToScrapRelicsCharacterIds);
                continue;
            }

            try {
                validateRelicSets(mocRequest);

                String cacheKey = round + "|" + buildRelicSignature(workingData, characterIds);
                CachedEvaluation cached = evaluationCache.get(cacheKey);
                MocResponse mocResponse;
                double totalDamage;
                if (cached != null) {
                    mocResponse = cached.response();
                    totalDamage = cached.damage();
                    cacheHits++;
                } else {
                    long remoteStart = System.nanoTime();
                    mocResponse = asagiClient.calculateDamage(mocRequest);
                    remoteNanos += System.nanoTime() - remoteStart;
                    totalDamage = totalDamage(mocResponse);
                    evaluationCache.put(cacheKey, new CachedEvaluation(totalDamage, mocResponse));
                    remoteEvaluations++;
                }

                state.calibrate(initialTemperatureFactor, totalDamage);

                boolean accepted = decideToAccept(totalDamage, state.getCurrentDamage(),
                    state.getTemperature());

                if (accepted) {
                    handleAcceptedState(state, totalDamage);
                    snapshot = SerializationUtils.clone(workingData);
                } else {
                    handleRejectedState(state);
                    workingData = SerializationUtils.clone(snapshot);
                }

                // Annealing deliberately wanders downhill, so the last accepted state is
                // usually not the best one seen. Keep the peak separately and return it.
                if (totalDamage > bestDamage) {
                    boolean meaningful = totalDamage - bestDamage
                        > Math.max(1.0, bestDamage * 0.0001);
                    bestDamage = totalDamage;
                    bestSnapshot = SerializationUtils.clone(workingData);
                    bestResponse = mocResponse;
                    bestRound = round;
                    if (meaningful) {
                        lastImprovementStep = state.getTotalSteps();
                    }
                }

                retryPolicy.recordSuccess();

            } catch (Exception e) {
                retryPolicy.recordFailure();
                if (retryPolicy.hasExceededMaxFailures()) {
                    throw new IllegalStateException(
                        "Aborting optimization after "
                            + RetryPolicy.MAX_CONSECUTIVE_FAILURES
                            + " consecutive damage-simulator failures. Last error: "
                            + truncate(e.getMessage(), 500), e);
                }
                log.warn("Simulation step dropped due to error: {}. Consecutive failures: {}",
                    truncate(e.getMessage(), 300), retryPolicy.getConsecutiveFailures());
                workingData = SerializationUtils.clone(snapshot);
                retryPolicy.applyBackoff();
            }

            publishProgress(jobId, state, bestDamage);

            if (state.getTotalSteps() >= minSteps
                && state.getTotalSteps() - lastImprovementStep >= patience) {
                if (!polishing && bestResponse != null) {
                    // Keep the exploratory phase, then spend one final window refining the best
                    // build at a low temperature instead of returning immediately.
                    workingData = SerializationUtils.clone(bestSnapshot);
                    snapshot = SerializationUtils.clone(bestSnapshot);
                    state.accept(bestDamage);
                    state.setTemperature(Math.max(1.0, bestDamage * polishTemperatureFactor));
                    lastImprovementStep = state.getTotalSteps();
                    polishing = true;
                    log.info(
                        "No improvement for {} steps; polishing best build at temperature {}",
                        patience, state.getTemperature());
                } else {
                    stopReason = "no meaningful improvement for " + patience + " steps";
                    break;
                }
            }

            performMutation(
                workingData,
                characterIds,
                fixedCharacterIds,
                allowedToScrapRelicsCharacterIds,
                disallowedToScrapRelicsCharacterIds);
        }

        long averageRemoteMs = remoteEvaluations == 0
            ? 0
            : remoteNanos / 1_000_000 / remoteEvaluations;
        log.info(
            "Search completed: {} steps ({}), {} remote evaluations (avg {} ms), {} cache hits",
            state.getTotalSteps(), stopReason, remoteEvaluations, averageRemoteMs, cacheHits);

        if (bestResponse == null) {
            throw new IllegalStateException(
                "Optimization produced no valid build: every candidate was rejected by the "
                    + "damage simulator.");
        }

        MocResponse finalResponse = bestResponse;
        double finalDamage = bestDamage;
        int finalRoundNumber = clampRound(finalRound);
        final ScannedData finalSnapshot = bestSnapshot;
        if (bestRound == finalRoundNumber) {
            log.info("Best build already validated at round {}; skipping duplicate final request",
                finalRoundNumber);
        } else {
            try {
                MocRequest finalRequest = mocRequestFactory.createBaseRequest();
                finalRequest.setRound(finalRoundNumber);
                finalRequest.setCharacters(characterIds.stream()
                    .map(characterId -> asagiCharacterMapper.map(finalSnapshot, characterId))
                    .toList());
                updateRequestWithSubstats(finalRequest, finalSnapshot, characterIds);
                applyRelicsToRequest(finalRequest, finalSnapshot);

                long finalStart = System.nanoTime();
                finalResponse = asagiClient.calculateDamage(finalRequest);
                finalDamage = totalDamage(finalResponse);
                log.info("Final round-{} validation: {} damage (search best: {}) in {} ms",
                    finalRoundNumber, finalDamage, bestDamage,
                    (System.nanoTime() - finalStart) / 1_000_000);
            } catch (Exception e) {
                log.warn("Final validation failed; returning best search result: {}",
                    truncate(e.getMessage(), 300));
            }
        }

        publishProgress(jobId, state, finalDamage);

        log.info("================================================================");
        log.info(String.format(
            "Optimization Completed! Best Damage: %.2f (last accepted: %.2f) after %d total mutations.",
            finalDamage, state.getCurrentDamage(), state.getTotalSteps()));
        log.info("================================================================");

        return new SimulationResult(bestSnapshot, finalResponse, finalDamage);
    }

    private static double totalDamage(MocResponse response) {
        return response.getT().stream()
            .mapToDouble(TItem::getTotal)
            .sum();
    }

    /**
     * Characters with no relics (or an incomplete set) cannot be evaluated because Asagi needs a
     * complete cavern + planar configuration. Fill every empty slot before the search starts so
     * the optimizer can work from a valid state instead of rolling back every mutation forever.
     */
    private void repairIncompleteLoadouts(ScannedData data, List<String> characterIds,
        List<String> allowedToScrapRelicsCharacterIds,
        List<String> disallowedToScrapRelicsCharacterIds) {
        Set<Slot> requiredSlots = EnumSet.allOf(Slot.class);
        Set<String> allowed = new HashSet<>(allowedToScrapRelicsCharacterIds);
        Set<String> disallowed = new HashSet<>(disallowedToScrapRelicsCharacterIds);
        Random random = ThreadLocalRandom.current();

        for (String characterId : characterIds) {
            Set<Slot> equippedSlots = EnumSet.noneOf(Slot.class);
            for (Relic relic : data.getRelics()) {
                if (characterId.equals(relic.getLocation())) {
                    equippedSlots.add(relic.getSlot());
                }
            }
            if (equippedSlots.containsAll(requiredSlots)) {
                continue;
            }

            HSRCharacter character = data.getCharacters().stream()
                .filter(c -> characterId.equals(c.getId()))
                .findFirst()
                .orElseThrow();
            MutationContext context = new MutationContext(
                characterId, character.getAbilityVersion(), allowed, disallowed, random);
            fullLoadoutMutationStrategy.mutate(data, context);

            Set<Slot> slotsAfter = EnumSet.noneOf(Slot.class);
            for (Relic relic : data.getRelics()) {
                if (characterId.equals(relic.getLocation())) {
                    slotsAfter.add(relic.getSlot());
                }
            }
            log.info("Repaired incomplete loadout for {}: {} slots equipped", characterId,
                slotsAfter.size());
            if (!slotsAfter.containsAll(requiredSlots)) {
                throw new IllegalStateException(
                    "Cannot build a complete relic loadout for character " + characterId
                        + ": not enough available relics of the required slots/sets.");
            }
        }
    }

    private void handleAcceptedState(
        AnnealingState state,
        double totalDamage) {
        double delta = totalDamage - state.getCurrentDamage();
        state.accept(totalDamage);

        log.info(String.format(
            "Epoch [%d/%d], Step [%d] | Temp: %.4f | Accepted Damage: %.2f (Delta: %+.2f)",
            state.getCurrentEpoch(), state.getTotalEpochs(),
            state.getTotalSteps(), state.getTemperature(),
            state.getCurrentDamage(), delta));
    }

    private void handleRejectedState(AnnealingState state) {
        if (state.getTotalSteps() % 50 == 0) {
            log.info(String.format(
                "Epoch [%d/%d], Step [%d] | Temp: %.4f | [Skipped] Current Baseline: %.2f",
                state.getCurrentEpoch() + 1, state.getTotalEpochs(),
                state.getTotalSteps(), state.getTemperature(),
                state.getCurrentDamage()));
        }
    }

    private void performMutation(
        ScannedData scannedData,
        List<String> characterIds,
        List<String> fixedCharacterIds,
        List<String> allowedToScrapRelicsCharacterIds,
        List<String> disallowedToScrapRelicsCharacterIds) {
        List<String> filteredCharacters = characterIds.stream()
            .filter(id -> !fixedCharacterIds.contains(id))
            .toList();

        Random random = ThreadLocalRandom.current();
        String randomChar = filteredCharacters.get(random.nextInt(filteredCharacters.size()));

        HSRCharacter character = scannedData.getCharacters().stream()
            .filter(c -> c.getId().equals(randomChar))
            .findFirst()
            .orElseThrow();

        Set<String> allowed = new HashSet<>(allowedToScrapRelicsCharacterIds);
        Set<String> disallowed = new HashSet<>(disallowedToScrapRelicsCharacterIds);

        MutationContext context = new MutationContext(
            randomChar,
            character.getAbilityVersion(),
            allowed,
            disallowed,
            random);

        RelicMutationStrategy strategy = mutationSelector.select(random);
        strategy.mutate(scannedData, context);
    }

    private void updateRequestWithSubstats(
        MocRequest mocRequest,
        ScannedData scannedData,
        List<String> characterIds) {
        mocRequest.setTotalSubStatus(
            characterIds.stream()
                .map(characterId -> getTotalSubStats(characterId, scannedData))
                .toList());
    }

    private TotalSubStats getTotalSubStats(String characterId, ScannedData scannedData) {
        HSRCharacter hsrCharacter = scannedData.getCharacters().stream()
            .filter(c -> c.getId().equals(characterId))
            .findFirst()
            .orElseThrow();

        TotalSubStats totalSubStats = new TotalSubStats();
        totalSubStats.setKey(
            AsagiCharacterMetadata.getInfoById(characterId, hsrCharacter.getAbilityVersion())
                .getDisplayName());

        SubStatAggregator.accumulateFromRelics(
            totalSubStats,
            scannedData.getRelics(),
            characterId);

        return totalSubStats;
    }

    private void applyRelicsToRequest(MocRequest mocRequest, ScannedData scannedData) {
        for (CharactersItem c : mocRequest.getCharacters()) {
            asagiCharacterMapper.applyRelics(c, scannedData, c.getId());
        }
    }

}
