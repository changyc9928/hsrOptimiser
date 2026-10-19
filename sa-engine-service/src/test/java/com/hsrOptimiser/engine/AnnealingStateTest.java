package com.hsrOptimiser.engine;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AnnealingStateTest {

    @Test
    @DisplayName("Run length is driven by iterations, not by the acceptance rate")
    void runLengthDependsOnStepsNotAccepts() {
        AnnealingState state = new AnnealingState(3, 10, 0.05, 0.95);

        for (int i = 0; i < 30; i++) {
            assertThat(state.hasRemainingEpochs()).isTrue();
            state.incrementSteps();
        }

        assertThat(state.hasRemainingEpochs()).isFalse();
        assertThat(state.getCurrentEpoch()).isEqualTo(3);
        assertThat(state.getTotalSteps()).isEqualTo(30);
    }

    @Test
    @DisplayName("Rejecting every mutation still finishes the configured epochs")
    void rejectingMutationsStillAdvancesEpochs() {
        AnnealingState state = new AnnealingState(2, 5, 0.05, 0.95);

        for (int i = 0; i < 10; i++) {
            state.incrementSteps();
        }

        assertThat(state.getCurrentEpoch()).isEqualTo(2);
    }

    @Test
    @DisplayName("Temperature is cooled once per epoch")
    void temperatureCoolsPerEpoch() {
        AnnealingState state = new AnnealingState(4, 2, 0.05, 0.5);

        state.incrementSteps();
        assertThat(state.getTemperature()).isEqualTo(0.05);
        state.incrementSteps();
        assertThat(state.getTemperature()).isEqualTo(0.025);
        state.incrementSteps();
        assertThat(state.getTemperature()).isEqualTo(0.025);
        state.incrementSteps();
        assertThat(state.getTemperature()).isEqualTo(0.0125);
    }

    @Test
    @DisplayName("Temperature is calibrated from the first measured damage, exactly once")
    void calibrateScalesTemperatureToDamage() {
        AnnealingState state = new AnnealingState(4, 2, 0.05, 0.95);

        state.calibrate(0.05, 14_000_000);
        assertThat(state.getTemperature()).isEqualTo(700_000);

        // A later, very different sample must not rescale a calibrated run.
        state.calibrate(0.05, 1_000_000);
        assertThat(state.getTemperature()).isEqualTo(700_000);
    }

    @Test
    @DisplayName("Uncalibrated state ignores meaningless damage samples")
    void calibrateIgnoresNonPositiveDamage() {
        AnnealingState state = new AnnealingState(4, 2, 0.05, 0.95);

        state.calibrate(0.05, 0);
        assertThat(state.getTemperature()).isEqualTo(0.05);
        assertThat(state.isCalibrated()).isFalse();
    }

    @Test
    @DisplayName("Accepting updates damage without advancing epochs")
    void acceptOnlyUpdatesDamage() {
        AnnealingState state = new AnnealingState(4, 2, 0.05, 0.95);

        state.incrementSteps();
        state.accept(1234);

        assertThat(state.getCurrentDamage()).isEqualTo(1234);
        assertThat(state.getCurrentEpoch()).isZero();
    }

    @Test
    @DisplayName("Broken configurations cannot stall the loop")
    void degenerateConfigurationStillTerminates() {
        assertThat(new AnnealingState(0, 10, 0.05, 0.95).hasRemainingEpochs()).isFalse();

        AnnealingState zeroSteps = new AnnealingState(5, 0, 0.05, 0.95);
        assertThat(zeroSteps.hasRemainingEpochs()).isTrue();
        zeroSteps.incrementSteps();
        assertThat(zeroSteps.getCurrentEpoch()).isEqualTo(1);
    }
}