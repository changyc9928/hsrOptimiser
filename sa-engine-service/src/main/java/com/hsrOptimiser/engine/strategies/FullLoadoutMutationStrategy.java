package com.hsrOptimiser.engine.strategies;

import com.hsrOptimiser.DTO.hsrScanner.ScannedData;
import com.hsrOptimiser.engine.MutationContext;
import com.hsrOptimiser.engine.RelicMutationStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Fills an entire loadout by equipping a valid cavern pair set and a valid planar pair set. This is
 * used to repair characters that start with missing equipment, where the ordinary one-slot
 * mutations cannot make progress because every intermediate request is invalid.
 */
@Component
@RequiredArgsConstructor
public class FullLoadoutMutationStrategy implements RelicMutationStrategy {

    private final CavernSetMutationStrategy cavernSetMutationStrategy;
    private final PlanarPairMutationStrategy planarPairMutationStrategy;

    @Override
    public void mutate(ScannedData data, MutationContext context) {
        cavernSetMutationStrategy.mutate(data, context);
        planarPairMutationStrategy.mutate(data, context);
    }
}
