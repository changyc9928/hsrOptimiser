package com.hsrOptimiser.utils;

import java.util.Map;
import java.util.Set;

public final class StatMapper {

    private static final Map<String, String> STAT_MAPPING = Map.ofEntries(
        Map.entry("ATK", "atk_rate"),
        Map.entry("DEF", "def_rate"),
        Map.entry("HP", "hp_rate"),
        Map.entry("SPD", "spd_flat"),
        Map.entry("CRIT Rate", "crit_rate"),
        Map.entry("CRIT DMG", "crit_dmg"),
        Map.entry("Energy Regeneration Rate", "ep_rate"),
        Map.entry("Effect Hit Rate", "effect_hit"),
        Map.entry("Break Effect", "break_rate"),
        Map.entry("Outgoing Healing Boost", "heal_rate")
    );

    private static final Set<String> DAMAGE_STATS = Set.of(
        "Wind DMG Boost",
        "Ice DMG Boost",
        "Fire DMG Boost",
        "Lightning DMG Boost",
        "Physical DMG Boost",
        "Quantum DMG Boost",
        "Imaginary DMG Boost"
    );

    public static String mapStatKey(String stat) {

        if (DAMAGE_STATS.contains(stat)) {
            return "deal_dmg";
        }

        return STAT_MAPPING.getOrDefault(stat, stat);
    }
}