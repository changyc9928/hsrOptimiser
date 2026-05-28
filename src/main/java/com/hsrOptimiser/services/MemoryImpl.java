package com.hsrOptimiser.services;

import com.hsrOptimiser.DTO.hsrScanner.ScannedData;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class MemoryImpl implements Memory{

    private final Map<String, ScannedData> memory = new HashMap<>();

    @Override
    public ScannedData getMemory(String userId) {
        return memory.get(userId);
    }

    @Override
    public void insertMemory(String userId, ScannedData data) {
        memory.put(userId, data);
    }
}
