package com.hsrOptimiser.engine.strategies;

import com.hsrOptimiser.DTO.hsrScanner.Relic;
import com.hsrOptimiser.DTO.hsrScanner.ScannedData;
import com.hsrOptimiser.DTO.hsrScanner.Slot;
import com.hsrOptimiser.clientConfig.AsagiCharacterMetadata;
import com.hsrOptimiser.engine.MutationContext;
import com.hsrOptimiser.engine.RelicAvailabilityHelper;
import com.hsrOptimiser.engine.RelicMutationStrategy;
import java.util.Objects;
import java.util.Set;
import org.springframework.stereotype.Component;

/**
 * Mutates a single relic: picks a random equipped relic, then replaces it
 * with a random matching unequipped relic of the same set and slot.
 */
@Component
public class SingleRelicMutationStrategy implements RelicMutationStrategy {

    @Override
    public void mutate(ScannedData data, MutationContext context) {
        var relics = data.getRelics();
        var random = context.random();
        var allowed = context.allowedCharacters();
        var disallowed = context.disallowedCharacters();
        var characterId = context.characterId();
        var abilityVersion = context.abilityVersion();

        // Pick a random equipped relic (reservoir sampling)
        Relic sourceRelic = RelicAvailabilityHelper.reservoirSample(
                relics,
                r -> characterId.equals(r.getLocation()),
                random);

        if (sourceRelic == null) {
            return;
        }

        String setId = sourceRelic.getSetId();
        Slot slot = sourceRelic.getSlot();

        String mainStat = AsagiCharacterMetadata.getInfoById(characterId, abilityVersion)
                .getAttackType() + " DMG Boost";

        Set<String> allowedSphereStats = Set.of(mainStat, "ATK", "DEF", "HP");

        // Pick a matching replacement (reservoir sampling)
        Relic candidate = null;
        int matches = 0;

        for (Relic r : relics) {
            boolean scrapable = r == sourceRelic
                    || RelicAvailabilityHelper.isAvailable(r, allowed, disallowed);

            if (!scrapable) {
                continue;
            }

            if (r.getSlot() != slot) {
                continue;
            }

            if (slot == Slot.PlanarSphere && !allowedSphereStats.contains(r.getMainstat())) {
                continue;
            }

            if (r.getRarity() != 5) {
                continue;
            }

            if (!Objects.equals(r.getSetId(), setId)) {
                continue;
            }

            matches++;
            if (random.nextInt(matches) == 0) {
                candidate = r;
            }
        }

        if (candidate == null || candidate == sourceRelic) {
            return;
        }

        sourceRelic.setLocation("");
        candidate.setLocation(characterId);
    }
}
