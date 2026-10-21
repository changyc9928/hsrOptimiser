package com.hsrOptimiser.engine;

import com.hsrOptimiser.DTO.asagi.CharactersItem;
import com.hsrOptimiser.DTO.asagi.MocRequest;
import com.hsrOptimiser.DTO.asagi.MocResponse;
import com.hsrOptimiser.DTO.asagi.TItem;
import com.hsrOptimiser.DTO.asagi.TotalSubStats;
import com.hsrOptimiser.DTO.hsrScanner.HSRCharacter;
import com.hsrOptimiser.DTO.hsrScanner.ScannedData;
import com.hsrOptimiser.client.AsagiClient;
import com.hsrOptimiser.utils.SubStatAggregator;
import com.hsrOptimiser.clientConfig.AsagiCharacterMetadata;
import com.hsrOptimiser.mapper.AsagiCharactersItemMapper;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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

    private static void applyAdaptiveThrottling(long startTime) {
        long duration = System.currentTimeMillis() - startTime;
        if (duration > 1500) {
            try {
                Thread.sleep(150);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        }
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
    private void preflightCheck(MocRequest mocRequest, AsagiClient asagiClient,
        List<String> characterIds, ScannedData data) {
        if (!isRelicSetValid(mocRequest)) {
            log.info("Skipping pre-flight damage check: initial relic sets incomplete");
            return;
        }
        List<String> missingCones = findCharactersWithoutCones(mocRequest, characterIds);
        if (!missingCones.isEmpty()) {
            throw new IllegalStateException(
                "Pre-flight damage check failed: no light cone equipped on character(s): "
                    + String.join(", ", missingCones)
                    + ". Equip a light cone on each team member and retry.");
        }
        try {
            asagiClient.calculateDamage(mocRequest);
            log.info("Pre-flight damage check passed");
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
        log.info("Starting Simulated Annealing optimization. Epochs: {} x {} steps | Initial Temp: {}% of team damage",
            epoch, stepsPerEpoch, initialTemperatureFactor * 100);

        ScannedData workingData = SerializationUtils.clone(data);
        MocRequest mocRequest = mocRequestFactory.createBaseRequest();

        // Capture in a final variable for the lambda; workingData itself
        // will be reassigned during rollback/retry below.
        final ScannedData initialData = workingData;
        mocRequest.setCharacters(characterIds.stream()
            .map(characterId -> asagiCharacterMapper.map(initialData, characterId))
            .toList());

        updateRequestWithSubstats(mocRequest, workingData, characterIds);
        preflightCheck(mocRequest, asagiClient, characterIds, initialData);

        log.info("Relic candidates: {} relics in the scan", workingData.getRelics().size());

        AnnealingState state = new AnnealingState(epoch, stepsPerEpoch, initialTemperatureFactor,
            tempCoolingRate);
RetryPolicy retryPolicy = new RetryPolicy();
ScannedData snapshot = SerializationUtils.clone(workingData);
MocResponse bestResponse = null;
ScannedData bestSnapshot = SerializationUtils.clone(workingData);
double bestDamage = Double.NEGATIVE_INFINITY;

        while (state.hasRemainingEpochs()) {
            state.incrementSteps();

            updateRequestWithSubstats(mocRequest, workingData, characterIds);
            applyRelicsToRequest(mocRequest, workingData);

            long startTime = System.currentTimeMillis();

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

                MocResponse mocResponse = asagiClient.calculateDamage(mocRequest);
                double totalDamage = mocResponse.getT().stream()
                    .mapToDouble(TItem::getTotal)
                    .sum();

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
                    bestDamage = totalDamage;
                    bestSnapshot = SerializationUtils.clone(workingData);
                    bestResponse = mocResponse;
                }

                retryPolicy.recordSuccess();
                applyAdaptiveThrottling(startTime);

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

            performMutation(
                workingData,
                characterIds,
                fixedCharacterIds,
                allowedToScrapRelicsCharacterIds,
                disallowedToScrapRelicsCharacterIds);
        }

        log.info("================================================================");
        log.info(String.format(
            "Optimization Completed! Best Damage: %.2f (last accepted: %.2f) after %d total mutations.",
            bestDamage, state.getCurrentDamage(), state.getTotalSteps()));
        log.info("================================================================");

        if (bestResponse == null) {
            throw new IllegalStateException(
                "Optimization produced no valid build: every candidate was rejected by the "
                    + "damage simulator.");
        }

        return new SimulationResult(bestSnapshot, bestResponse, bestDamage);
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
