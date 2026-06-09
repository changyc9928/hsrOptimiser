package com.hsrOptimiser.engine;

import com.hsrOptimiser.DTO.asagi.CharactersItem;
import com.hsrOptimiser.DTO.asagi.MocRequest;
import com.hsrOptimiser.DTO.asagi.MocResponse;
import com.hsrOptimiser.DTO.asagi.TItem;
import com.hsrOptimiser.DTO.asagi.TotalSubStats;
import com.hsrOptimiser.DTO.hsrScanner.HSRCharacter;
import com.hsrOptimiser.DTO.hsrScanner.ScannedData;
import com.hsrOptimiser.client.AsagiClient;
import com.hsrOptimiser.clientConfig.AsagiCharacterMetadata;
import com.hsrOptimiser.mapper.AsagiCharactersItemMapper;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.SerializationUtils;

@Component
@Slf4j
@RequiredArgsConstructor
public class SimulatedAnnealing {

    private final AsagiCharactersItemMapper asagiCharacterMapper;
    private final MocRequestFactory mocRequestFactory;
    private final MutationStrategySelector mutationSelector;

    @Value("${simulated-annealing.initial-temperature}")
    private double initialTemperature;

    @Value("${simulated-annealing.cooling-rate}")
    private double tempCoolingRate;

    @Value("${simulated-annealing.epoch}")
    private int epoch;

    /**
     * Decides whether to accept a new state based on the Metropolis criterion.
     */
    static boolean decideToAccept(double totalDamage, double currentDamage, double temperature) {
        double deltaE = totalDamage - currentDamage;
        if (deltaE > 0) {
            return true;
        }

        double scaledDelta = deltaE / 100000.0;
        double probability = Math.exp(scaledDelta / temperature);
        return ThreadLocalRandom.current().nextDouble() < probability;
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
            List<String> disallowedToScrapRelicsCharacterIds) {
        log.info("Starting Simulated Annealing optimization. Target Epochs: {}, Initial Temp: {}",
                epoch, initialTemperature);

        ScannedData workingData = SerializationUtils.clone(data);
        MocRequest mocRequest = mocRequestFactory.createBaseRequest();

        // Capture in a final variable for the lambda; workingData itself
        // will be reassigned during rollback/retry below.
        final ScannedData initialData = workingData;
        mocRequest.setCharacters(characterIds.stream()
                .map(characterId -> asagiCharacterMapper.map(initialData, characterId))
                .toList());

        AnnealingState state = new AnnealingState(epoch, initialTemperature, tempCoolingRate);
        RetryPolicy retryPolicy = new RetryPolicy();
        ScannedData snapshot = SerializationUtils.clone(workingData);
        MocResponse bestResponse = null;

        while (state.hasRemainingEpochs()) {
            state.incrementSteps();

            updateRequestWithSubstats(mocRequest, workingData, characterIds);
            applyRelicsToRequest(mocRequest, workingData);

            long startTime = System.currentTimeMillis();

            try {
                validateRelicSets(mocRequest);

                MocResponse mocResponse = asagiClient.calculateDamage(mocRequest);
                double totalDamage = mocResponse.getT().stream()
                        .mapToDouble(TItem::getTotal)
                        .sum();

                boolean accepted = decideToAccept(totalDamage, state.getCurrentDamage(), state.getTemperature());

                if (accepted) {
                    handleAcceptedState(state, totalDamage);
                    snapshot = SerializationUtils.clone(workingData);
                    bestResponse = mocResponse;
                } else {
                    handleRejectedState(state);
                    workingData = SerializationUtils.clone(snapshot);
                }

                retryPolicy.recordSuccess();
                applyAdaptiveThrottling(startTime);

            } catch (Exception e) {
                retryPolicy.recordFailure();
                log.warn("Simulation step dropped due to error: {}. Consecutive failures: {}",
                        e.getMessage(), retryPolicy.getConsecutiveFailures());
                workingData = SerializationUtils.clone(snapshot);
                retryPolicy.applyBackoff();
            }

            performMutation(
                    workingData,
                    characterIds,
                    fixedCharacterIds,
                    allowedToScrapRelicsCharacterIds,
                    disallowedToScrapRelicsCharacterIds);
        }

        log.info("================================================================");
        log.info(String.format(
                "Optimization Completed! Final Damage: %.2f after %d total mutations.",
                state.getCurrentDamage(), state.getTotalSteps()));
        log.info("================================================================");

        return new SimulationResult(snapshot, bestResponse);
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

    private static void validateRelicSets(MocRequest mocRequest) {
        boolean missingSets = mocRequest.getCharacters().stream().anyMatch(item -> item.getRelicSet() == null
                || item.getRelicSet().getOrnament() == null
                || item.getRelicSet().getSet1() == null
                || item.getRelicSet().getSet2() == null);

        if (missingSets) {
            throw new IllegalStateException(
                    "Invalid relic configuration: One or more character items contain null relic sets.");
        }
    }

}
