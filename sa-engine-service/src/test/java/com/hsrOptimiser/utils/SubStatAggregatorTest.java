package com.hsrOptimiser.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import com.hsrOptimiser.DTO.asagi.TotalSubStats;
import com.hsrOptimiser.DTO.hsrScanner.Relic;
import com.hsrOptimiser.DTO.hsrScanner.Slot;
import com.hsrOptimiser.DTO.hsrScanner.SubStats;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Preview substats describe rolls a relic has not received yet. Counting them credited every
 * character with stats the equipped build does not have, which is why unenhanced relics used to
 * score as if they were fully leveled.
 */
class SubStatAggregatorTest {

    private static final String CHARACTER_ID = "1502";

    private static SubStats sub(String key, double value) {
        SubStats subStats = new SubStats();
        subStats.setKey(key);
        subStats.setValue(value);
        subStats.setCount(1);
        subStats.setStep(2);
        return subStats;
    }

    private static Relic relic(String location, List<SubStats> rolled, List<SubStats> preview) {
        Relic relic = new Relic();
        relic.setUid(location + "-" + (rolled == null ? 0 : rolled.size()));
        relic.setSlot(Slot.Head);
        relic.setSetId("A");
        relic.setLevel(0);
        relic.setRarity(5);
        relic.setMainstat("ATK");
        relic.setLocation(location);
        relic.setSubstats(rolled);
        relic.setPreviewSubstats(preview);
        return relic;
    }

    @Test
    @DisplayName("only rolled substats are accumulated")
    void accumulateFromRelics_countsRolledSubstatsOnly() {
        TotalSubStats total = new TotalSubStats();
        List<Relic> relics = List.of(relic(CHARACTER_ID,
            List.of(sub("CRIT Rate_", 3.24)),
            List.of(sub("CRIT Rate_", 6.48), sub("CRIT DMG_", 12.96))));

        SubStatAggregator.accumulateFromRelics(total, relics, CHARACTER_ID);

        assertThat(total.getCritRate()).isCloseTo(3.24, within(1e-9));
        assertThat(total.getCritDmg()).isZero();
    }

    @Test
    @DisplayName("relics equipped by other characters are ignored")
    void accumulateFromRelics_ignoresOtherCharacters() {
        TotalSubStats total = new TotalSubStats();
        List<Relic> relics = List.of(
            relic(CHARACTER_ID, List.of(sub("CRIT Rate_", 3.24)), null),
            relic("8010", List.of(sub("CRIT Rate_", 6.48)), null));

        SubStatAggregator.accumulateFromRelics(total, relics, CHARACTER_ID);

        assertThat(total.getCritRate()).isCloseTo(3.24, within(1e-9));
    }

    @Test
    @DisplayName("missing substats do not fail accumulation")
    void accumulateFromRelics_handlesNullSubstats() {
        TotalSubStats total = new TotalSubStats();

        SubStatAggregator.accumulateFromRelics(total,
            List.of(relic(CHARACTER_ID, null, List.of(sub("CRIT Rate_", 6.48)))), CHARACTER_ID);

        assertThat(total.getCritRate()).isZero();
    }
}
