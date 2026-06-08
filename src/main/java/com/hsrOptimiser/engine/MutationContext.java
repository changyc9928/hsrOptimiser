package com.hsrOptimiser.engine;

import java.util.Random;
import java.util.Set;

/**
 * Bundles the common parameters passed to every mutation strategy.
 */
public record MutationContext(
        String characterId,
        int abilityVersion,
        Set<String> allowedCharacters,
        Set<String> disallowedCharacters,
        Random random) {
}
