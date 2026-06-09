package com.hsrOptimiser.engine;

import lombok.Data;

/**
 * Encapsulates the mutable state of the simulated annealing algorithm.
 */
@Data
public class AnnealingState {

    private final int totalEpochs;
    private final double coolingRate;

    private int currentEpoch;
    private int totalSteps;
    private double currentDamage;
    private double temperature;

    public AnnealingState(int totalEpochs, double initialTemperature, double coolingRate) {
        this.totalEpochs = totalEpochs;
        this.coolingRate = coolingRate;
        this.currentEpoch = 0;
        this.totalSteps = 0;
        this.currentDamage = 0;
        this.temperature = initialTemperature;
    }

    public boolean hasRemainingEpochs() {
        return currentEpoch < totalEpochs;
    }

    public void incrementSteps() {
        totalSteps++;
    }

    public void accept(double newDamage) {
        currentDamage = newDamage;
        currentEpoch++;
        temperature *= coolingRate;
    }
}
