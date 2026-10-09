package com.hsrOptimiser.services;

import com.hsrOptimiser.DTO.hsrScanner.HSRCharacter;
import com.hsrOptimiser.DTO.hsrScanner.ScannedData;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TeamServiceImpl implements TeamService {

    private final Memory memory;

    @Override
    public List<HSRCharacter> getTeam(String userId) {
        ScannedData data = requireData(userId);
        List<String> ids = memory.getTeam(userId);
        if (ids == null) {
            return List.of();
        }
        return resolve(data, ids);
    }

    @Override
    public List<HSRCharacter> saveTeam(String userId, List<String> characterIds) {
        ScannedData data = requireData(userId);
        if (characterIds == null || characterIds.isEmpty()) {
            throw new IllegalArgumentException("Team must contain at least 1 character");
        }
        if (characterIds.size() > 4) {
            throw new IllegalArgumentException("Team can contain at most 4 characters");
        }
        Set<String> unique = new LinkedHashSet<>(characterIds);
        if (unique.size() != characterIds.size()) {
            throw new IllegalArgumentException("Team contains duplicate characters");
        }
        List<HSRCharacter> resolved = resolve(data, characterIds);
        memory.saveTeam(userId, new ArrayList<>(characterIds));
        return resolved;
    }

    private ScannedData requireData(String userId) {
        ScannedData data = memory.getMemory(userId);
        if (data == null) {
            throw new IllegalArgumentException("No scanned data for user: " + userId);
        }
        return data;
    }

    private List<HSRCharacter> resolve(ScannedData data, List<String> characterIds) {
        List<HSRCharacter> resolved = new ArrayList<>();
        for (String id : characterIds) {
            HSRCharacter character = data.getCharacters().stream()
                .filter(c -> id.equals(c.getId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                    "Character not found: " + id));
            resolved.add(character);
        }
        return resolved;
    }
}
