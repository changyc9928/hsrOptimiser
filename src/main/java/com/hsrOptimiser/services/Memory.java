package com.hsrOptimiser.services;

import com.hsrOptimiser.DTO.hsrScanner.ScannedData;

public interface Memory {
    public ScannedData getMemory(String userId);

    public void insertMemory(String userId, ScannedData data);
}
