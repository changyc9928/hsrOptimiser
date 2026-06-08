package com.hsrOptimiser.engine;

import com.hsrOptimiser.DTO.hsrScanner.ScannedData;

/**
 * A strategy for mutating the relic assignments on a {@link ScannedData}
 * snapshot.
 */
public interface RelicMutationStrategy {

    /**
     * Mutates relic assignments in-place on the given data.
     *
     * @param data    the scanned data whose relics will be modified
     * @param context the mutation context carrying character, version, and
     *                availability info
     */
    void mutate(ScannedData data, MutationContext context);
}
