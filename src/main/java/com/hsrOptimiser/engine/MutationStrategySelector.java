package com.hsrOptimiser.engine;

import java.util.List;
import java.util.Random;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * Selects a mutation strategy based on weighted probabilities.
 *
 * <p>
 * Default distribution (matching the original thresholds):
 * <ul>
 * <li>0.00 – 0.50 → Single relic mutation</li>
 * <li>0.50 – 0.75 → Existing pair mutation</li>
 * <li>0.75 – 0.875 → Planar pair mutation</li>
 * <li>0.875 – 1.00 → Cavern set mutation</li>
 * </ul>
 */
@Component
public class MutationStrategySelector {

    private final List<RelicMutationStrategy> strategies;
    private final double[] thresholds;

    public MutationStrategySelector(
            @Qualifier("singleRelicMutationStrategy") RelicMutationStrategy singleRelic,
            @Qualifier("existingPairMutationStrategy") RelicMutationStrategy existingPair,
            @Qualifier("planarPairMutationStrategy") RelicMutationStrategy planarPair,
            @Qualifier("cavernSetMutationStrategy") RelicMutationStrategy cavernSet) {
        this.strategies = List.of(singleRelic, existingPair, planarPair, cavernSet);
        this.thresholds = new double[] { 0.50, 0.75, 0.875, 1.0 };
    }

    /**
     * Selects a strategy based on a random roll in [0, 1).
     */
    public RelicMutationStrategy select(double roll) {
        for (int i = 0; i < thresholds.length; i++) {
            if (roll < thresholds[i]) {
                return strategies.get(i);
            }
        }
        // Fallback (should never happen with roll in [0,1))
        return strategies.get(strategies.size() - 1);
    }

    /**
     * Selects a strategy using the given random source.
     */
    public RelicMutationStrategy select(Random random) {
        return select(random.nextDouble());
    }
}
