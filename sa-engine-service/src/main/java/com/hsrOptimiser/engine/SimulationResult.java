package com.hsrOptimiser.engine;

import com.hsrOptimiser.DTO.asagi.MocResponse;
import com.hsrOptimiser.DTO.hsrScanner.ScannedData;

/**
 * The result of a simulated annealing optimization run.
 *
 * @param data relics of the best build found, not of the last accepted state
 * @param mocResponse damage breakdown that produced that build
 * @param bestDamage total damage of the returned build
 */
public record SimulationResult(ScannedData data, MocResponse mocResponse, double bestDamage) {

}
