package com.hsrOptimiser.engine;

import static org.assertj.core.api.Assertions.assertThat;

import com.hsrOptimiser.DTO.hsrScanner.Relic;
import com.hsrOptimiser.DTO.hsrScanner.ScannedData;
import java.util.List;
import org.junit.jupiter.api.Test;

class SimulatedAnnealingSpeedTest {

    @Test
    void usesSearchRoundUntilRefineWindow() {
        assertThat(SimulatedAnnealing.roundForStep(1, 240, 0.25, 3, 6)).isEqualTo(3);
        assertThat(SimulatedAnnealing.roundForStep(180, 240, 0.25, 3, 6)).isEqualTo(3);
        assertThat(SimulatedAnnealing.roundForStep(181, 240, 0.25, 3, 6)).isEqualTo(6);
        assertThat(SimulatedAnnealing.roundForStep(240, 240, 0.25, 3, 6)).isEqualTo(6);
    }

    @Test
    void supportsDisablingOrExtendingRefineWindow() {
        assertThat(SimulatedAnnealing.roundForStep(1, 240, 0, 3, 6)).isEqualTo(3);
        assertThat(SimulatedAnnealing.roundForStep(1, 240, 1, 3, 6)).isEqualTo(6);
    }

    @Test
    void signatureOnlyCoversEquippedTeamRelicsAndIgnoresOrder() {
        Relic first = relic("r1", "1505");
        Relic second = relic("r2", "1502");
        Relic stray = relic("r3", "1503");
        List<String> team = List.of("1505", "1502");

        ScannedData data = new ScannedData();
        data.setRelics(List.of(first, second, stray));
        String signature = SimulatedAnnealing.buildRelicSignature(data, team);

        assertThat(signature).contains("1505:r1", "1502:r2");
        assertThat(signature).doesNotContain("r3");

        data.setRelics(List.of(second, stray, first));
        assertThat(SimulatedAnnealing.buildRelicSignature(data, team)).isEqualTo(signature);
    }

    @Test
    void signatureChangesWhenRelicMoves() {
        Relic relic = relic("r1", "1505");
        ScannedData data = new ScannedData();
        data.setRelics(List.of(relic));
        List<String> team = List.of("1505", "1502");

        String before = SimulatedAnnealing.buildRelicSignature(data, team);
        relic.setLocation("1502");

        assertThat(SimulatedAnnealing.buildRelicSignature(data, team)).isNotEqualTo(before);
    }

    private static Relic relic(String uid, String location) {
        Relic relic = new Relic();
        relic.setUid(uid);
        relic.setLocation(location);
        return relic;
    }
}
