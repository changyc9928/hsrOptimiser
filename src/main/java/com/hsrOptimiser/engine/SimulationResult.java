package com.hsrOptimiser.engine;

import com.hsrOptimiser.DTO.asagi.MocResponse;
import com.hsrOptimiser.DTO.hsrScanner.ScannedData;

/**
 * The result of a simulated annealing optimization run.
 */
public record SimulationResult(ScannedData data, MocResponse mocResponse) {
}
