package com.hsrOptimiser.engine;

import lombok.Data;

/**
 * Encapsulates the mutable state of the simulated annealing algorithm.
 *
 * <p>
 * An epoch is a fixed batch of {@code stepsPerEpoch} mutations, and the temperature is cooled once
 * per epoch. Counting accepted moves as epochs (the previous behaviour) made the run length depend
 * entirely on the acceptance rate, so the same configuration produced anywhere between a hundred
 * and a few thousand damage evaluations.
 */
@Data
public class AnnealingState {

    private final int totalEpochs;
    private final int stepsPerEpoch;
    private final double coolingRate;

    private int currentEpoch;
    private int totalSteps;
    private double currentDamage;
    private double temperature;
    private boolean calibrated;

    public AnnealingState(int totalEpochs, int stepsPerEpoch, double initialTemperatureFactor,
        double coolingRate) {
        this.totalEpochs = totalEpochs;
        this.stepsPerEpoch = Math.max(1, stepsPerEpoch);
        this.coolingRate = coolingRate;
        this.currentEpoch = 0;
        this.totalSteps = 0;
        this.currentDamage = 0;
        this.temperature = initialTemperatureFactor;
    }

    public boolean hasRemainingEpochs() {
        return totalSteps < (long) totalEpochs * stepsPerEpoch;
    }

    /**
     * Counts one mutation and advances the epoch (and temperature) on the epoch boundary.
     */
    public void incrementSteps() {
        totalSteps++;
        if (totalSteps % stepsPerEpoch == 0) {
            currentEpoch++;
            temperature *= coolingRate;
        }
    }

    public void accept(double newDamage) {
        currentDamage = newDamage;
    }

    /**
     * Scales the starting temperature to the damage scale of this run.
     *
     * <p>
     * Absolute temperatures are meaningless across teams: a fixed value that allows real
     * exploration at 1M damage freezes the search completely at 14M, which is what turned this
     * optimizer into a greedy hill climber. Calibrating once against the first measured damage
     * keeps acceptance probabilities meaningful regardless of team wealth.
     *
     * @param factor starting temperature as a fraction of the initial team damage
     */
    public void calibrate(double factor, double initialDamage) {
        if (calibrated || initialDamage <= 0 || !Double.isFinite(initialDamage)) {
            return;
        }
        this.temperature = Math.max(1.0, initialDamage * factor);
        this.calibrated = true;
    }
}