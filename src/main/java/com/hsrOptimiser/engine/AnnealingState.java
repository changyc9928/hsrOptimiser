package com.hsrOptimiser.engine;

/**
 * Encapsulates the mutable state of the simulated annealing algorithm.
 */
public class AnnealingState {

    private final int totalEpochs;
    private final double coolingRate;

    private int currentEpoch;
    private int totalSteps;
    private double currentDamage;
    private double temperature;
    private SimulationResult finalResult;

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

    public int getTotalEpochs() {
        return totalEpochs;
    }

    public int getCurrentEpoch() {
        return currentEpoch;
    }

    public int getTotalSteps() {
        return totalSteps;
    }

    public double getCurrentDamage() {
        return currentDamage;
    }

    public double getTemperature() {
        return temperature;
    }

    public SimulationResult getFinalResult() {
        return finalResult;
    }

    public void setFinalResult(SimulationResult finalResult) {
        this.finalResult = finalResult;
    }
}
