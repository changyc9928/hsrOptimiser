package com.hsrOptimiser.services;

import com.hsrOptimiser.DTO.hsrScanner.HSRCharacter;
import com.hsrOptimiser.DTO.hsrScanner.ScannedData;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class TeamServiceImpl implements TeamService {

    private final Memory memory;
    private RedisTemplate<String, Object> redisTemplate;

    public TeamServiceImpl(Memory memory) {
        this.memory = memory;
        this.redisTemplate = null;
    }

    @org.springframework.beans.factory.annotation.Autowired
    public TeamServiceImpl(Memory memory, RedisTemplate<String, Object> redisTemplate) {
        this.memory = memory;
        this.redisTemplate = redisTemplate;
    }

    private static final String TEAM_KEY = "inventory:team:%s";

    @Override
    public List<HSRCharacter> getTeam(String userId) {
        ScannedData data = memory.getMemory(userId);
        if (data == null) {
            return List.of();
        }
        List<String> ids = getTeamIds(userId);
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
        if (redisTemplate != null) {
            redisTemplate.opsForValue().set(String.format(TEAM_KEY, userId), new ArrayList<>(characterIds));
        }
        return resolved;
    }

    @Override
    public List<HSRCharacter> removeMember(String userId, String characterId) {
        List<String> ids = getTeamIds(userId);
        if (ids == null || !ids.contains(characterId)) {
            return getTeam(userId);
        }
        List<String> next = ids.stream().filter(id -> !id.equals(characterId)).toList();
        memory.saveTeam(userId, new ArrayList<>(next));
        if (redisTemplate != null) {
            redisTemplate.opsForValue().set(String.format(TEAM_KEY, userId),
                new ArrayList<>(next));
        }
        ScannedData data = memory.getMemory(userId);
        if (data == null || next.isEmpty()) {
            return List.of();
        }
        return resolve(data, next);
    }

    private List<String> getTeamIds(String userId) {
        if (redisTemplate != null) {
            Object value = redisTemplate.opsForValue().get(String.format(TEAM_KEY, userId));
            if (value instanceof List<?> list) {
                List<String> ids = new ArrayList<>();
                for (Object o : list) {
                    ids.add(String.valueOf(o));
                }
                return ids;
            }
        }
        return memory.getTeam(userId);
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
