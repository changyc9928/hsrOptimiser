package com.hsrOptimiser.engine;

import com.hsrOptimiser.DTO.hsrScanner.Relic;
import java.util.Random;
import java.util.Set;
import java.util.function.Predicate;
import org.springframework.util.StringUtils;

/**
 * Utility methods for relic availability checks and reservoir sampling.
 */
public final class RelicAvailabilityHelper {

    private RelicAvailabilityHelper() {
    }

    /**
     * A relic is available if it is unequipped, or equipped to a character
     * that is in the allowed set and not in the disallowed set.
     */
    public static boolean isAvailable(Relic relic, Set<String> allowed, Set<String> disallowed) {
        String location = relic.getLocation();
        return !StringUtils.hasText(location)
                || (allowed.contains(location) && !disallowed.contains(location));
    }

    /**
     * Reservoir sampling: selects one matching element from an iterable
     * with uniform probability, using O(1) extra space.
     *
     * @return a randomly selected matching item, or {@code null} if none match
     */
    public static <T> T reservoirSample(Iterable<T> source, Predicate<T> predicate, Random random) {
        T selected = null;
        int count = 0;

        for (T item : source) {
            if (!predicate.test(item)) {
                continue;
            }
            count++;
            if (random.nextInt(count) == 0) {
                selected = item;
            }
        }

        return selected;
    }
}
