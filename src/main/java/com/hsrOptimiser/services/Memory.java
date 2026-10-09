package com.hsrOptimiser.services;

import com.hsrOptimiser.DTO.hsrScanner.ScannedData;
import java.util.List;

public interface Memory {

    public ScannedData getMemory(String userId);

    public void insertMemory(String userId, ScannedData data);

    public List<String> getTeam(String userId);

    public void saveTeam(String userId, List<String> characterIds);
}
