package com.hsrOptimiser.services;

import com.hsrOptimiser.DTO.asagi.RelicMain;
import com.hsrOptimiser.DTO.asagi.RelicSet;
import com.hsrOptimiser.clientConfig.AsagiRelicSetMetadata;
import com.hsrOptimiser.DTO.hsrScanner.Relic;
import com.hsrOptimiser.DTO.hsrScanner.Slot;
import com.hsrOptimiser.utils.StatMapper;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class RelicBuildService {

    private static boolean isOrnamentSet(String setId) {
        return Integer.parseInt(setId) >= 300;
    }

    private static String getRelicSetName(String setId) {
        return AsagiRelicSetMetadata.fromId(setId).getLiteralName();
    }

    public RelicSet buildRelicSet(List<Relic> relics) {
        RelicSet relicSet = new RelicSet();

        Map<String, Long> setCounts = categorizeSetIds(relics);

        setCounts.entrySet().stream()
            .filter(e -> isOrnamentSet(e.getKey()) && e.getValue() >= 2)
            .findFirst()
            .ifPresent(e ->
                relicSet.setOrnament(getRelicSetName(e.getKey()))
            );

        List<Map.Entry<String, Long>> cavernSets = setCounts.entrySet().stream()
            .filter(e -> !isOrnamentSet(e.getKey()) && e.getValue() >= 2)
            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
            .toList();

        if (cavernSets.isEmpty()) {
            return relicSet;
        }

        Map.Entry<String, Long> primary = cavernSets.get(0);

        String primaryName = getRelicSetName(primary.getKey());

        if (primary.getValue() >= 4) {

            relicSet.setSet1(primaryName);
            relicSet.setSet2(primaryName);

            return relicSet;
        }

        relicSet.setSet1(primaryName);

        if (cavernSets.size() > 1) {
            relicSet.setSet2(
                getRelicSetName(cavernSets.get(1).getKey())
            );
        }
        return relicSet;
    }

    public RelicMain buildRelicMain(List<Relic> relics) {
        Map<Slot, Relic> relicBySlot = relics.stream()
            .collect(Collectors.toMap(
                Relic::getSlot,
                Function.identity(),
                (a, b) -> a
            ));

        RelicMain relicMain = new RelicMain();

        relicMain.setBody(getMainStat(relicBySlot, Slot.Body));
        relicMain.setFeet(getMainStat(relicBySlot, Slot.Feet));
        relicMain.setRope(getMainStat(relicBySlot, Slot.LinkRope));
        relicMain.setSphere(getMainStat(relicBySlot, Slot.PlanarSphere));
        return relicMain;
    }

    private Map<String, Long> categorizeSetIds(List<Relic> relics) {
        if (relics == null) {
            return Map.of();
        }

        return relics.stream()
            .filter(r -> r.getSetId() != null)
            .collect(Collectors.groupingBy(
                Relic::getSetId,
                Collectors.counting()
            ));
    }

    private String getMainStat(Map<Slot, Relic> relicBySlot, Slot slot) {
        return Optional.ofNullable(relicBySlot.get(slot))
            .map(Relic::getMainstat)
            .map(StatMapper::mapStatKey)
            .orElse(null);
    }
}