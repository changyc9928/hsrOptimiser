package com.hsrOptimiser.engine;

import com.hsrOptimiser.DTO.asagi.TotalSubStats;
import com.hsrOptimiser.DTO.hsrScanner.Relic;
import com.hsrOptimiser.DTO.hsrScanner.SubStats;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;

/**
 * Deduplicated substat accumulation logic.
 */
@Slf4j
public final class SubStatAggregator {

    public static final Map<String, BiConsumer<TotalSubStats, Double>> STAT_MAPPERS = Map.ofEntries(
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
            Map.entry("Break Effect_", TotalSubStats::addBreakRate));

    private SubStatAggregator() {
    }

    /**
     * Accumulates substats from a stream of {@link SubStats} into the given total.
     */
    public static void accumulateSubstats(TotalSubStats total, Stream<SubStats> stream) {
        stream.forEach(subStat -> {
            BiConsumer<TotalSubStats, Double> mapper = STAT_MAPPERS.get(subStat.getKey());
            if (mapper != null) {
                mapper.accept(total, subStat.getValue());
            } else {
                log.warn("Unexpected substat key encountered: {}", subStat.getKey());
            }
        });
    }

    /**
     * Accumulates substats from equipped relics for a given character.
     * Processes both regular substats and preview substats.
     */
    public static void accumulateFromRelics(
            TotalSubStats total,
            List<Relic> relics,
            String characterId) {
        accumulateSubstats(total,
                relics.stream()
                        .filter(r -> characterId.equals(r.getLocation()))
                        .flatMap(r -> r.getSubstats().stream()));

        accumulateSubstats(total,
                relics.stream()
                        .filter(r -> characterId.equals(r.getLocation()) && r.getPreviewSubstats() != null)
                        .flatMap(r -> r.getPreviewSubstats().stream()));
    }
}
