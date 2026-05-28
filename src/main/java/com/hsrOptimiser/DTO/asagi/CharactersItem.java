package com.hsrOptimiser.DTO.asagi;


import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.Map;
import java.util.Set;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CharactersItem {

    private static final Map<String, Integer> SKILL_PRIORITY_MAP = Map.of(
        "1505", 200,
        "1502", 150,
        "1301", 150,
        "1310", 150,
        "1303", 150,
        "1225", 100,
        "8010", 100,
        "1217", 200
    );

    private static final Map<String, Integer> ADD_SPEED_MAP = Map.of(
        "1301", 0,
        "1505", 5,
        "1303", 5,
        "1310", 5,
        "1225", 14,
        "1502", 9,
        "8010", 0,
        "1217", 5
    );

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

    private String note;
    private int lightConesEidolon;
    private RelicSub relicSub;
    private int eidolon;
    private RelicSet relicSet;
    private boolean enableEidolon;
    private int type;
    private int speed;
    private LightConesObj lightConesObj;
    private int addSpeed;
    private String lightCones;
    private int skillPriority;
    private String name;
    private int reality;
    private RelicMain relicMain;
    private String job;
    private boolean useTechnique;
    private String key;
    private String id;
}
