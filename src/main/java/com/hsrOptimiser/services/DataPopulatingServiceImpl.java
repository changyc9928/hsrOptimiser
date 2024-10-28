package com.hsrOptimiser.services;

import com.hsrOptimiser.client.ProjectYattaClient;
import com.hsrOptimiser.domain.hsrScanner.Character;
import com.hsrOptimiser.domain.hsrScanner.*;
import com.hsrOptimiser.domain.hsrScanner.populatedData.PopulatedCharacter;
import com.hsrOptimiser.domain.hsrScanner.populatedData.PopulatedData;
import com.hsrOptimiser.domain.hsrScanner.populatedData.PopulatedLightCone;
import com.hsrOptimiser.domain.hsrScanner.populatedData.PopulatedRelic;
import com.hsrOptimiser.domain.projectYatta.ProjectYattaData;
import com.hsrOptimiser.domain.projectYatta.ProjectYattaResponse;
import com.hsrOptimiser.domain.projectYatta.SubSkills;
import com.hsrOptimiser.domain.projectYatta.Upgrade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Collectors;

@Service
public class DataPopulatingServiceImpl implements DataPopulatingService {

    @Autowired
    ProjectYattaClient projectYattaClient;

    @Override
    public PopulatedData populateData(ScannedData scannedData) {
        PopulatedData populatedData = new PopulatedData();
        populatedData.setCharacters(fetchPopulatedCharacters(scannedData));
        populatedData.setLightCones(fetchPopulatedLightCones(scannedData));
        populatedData.setRelics(fetchPopulatedRelics(scannedData));
        return populatedData;
    }

    private ArrayList<PopulatedRelic> fetchPopulatedRelics(ScannedData scannedData) {
        return scannedData.getRelics().stream()
                .map(this::transformRelic)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private PopulatedRelic transformRelic(Relic relic) {
        updateMainStat(relic);
        PopulatedRelic populatedRelic = new PopulatedRelic();
        populatedRelic.setMainStatValue(calculateStat(relic.getRarity(), relic.getMainStat(), relic.getLevel()));
        populatedRelic.setUid(relic.getUid());
        populatedRelic.setLevel(relic.getLevel());
        populatedRelic.setLock(relic.isLock());
        populatedRelic.setName(relic.getName());
        populatedRelic.setDiscard(relic.isDiscard());
        populatedRelic.setRarity(relic.getRarity());
        populatedRelic.setSlot(relic.getSlot());
        populatedRelic.setLocation(relic.getLocation());
        populatedRelic.setSetId(relic.getSetId());
        populatedRelic.setSubStats(relic.getSubStats());
        populatedRelic.setMainStat(relic.getMainStat());
        return populatedRelic;
    }

    private void updateMainStat(Relic relic) {
        if (!relic.getSlot().equals(Slot.Head) && relic.getMainStat().equals(Stats.HP_FLAT)) {
            relic.setMainStat(Stats.HP_PERCENT);
        } else if (!relic.getSlot().equals(Slot.Hands) && relic.getMainStat().equals(Stats.ATK_FLAT)) {
            relic.setMainStat(Stats.ATK_PERCENT);
        } else if (relic.getMainStat().equals(Stats.DEF_FLAT)) {
            relic.setMainStat(Stats.DEF_PERCENT);
        }
    }

    private ArrayList<PopulatedCharacter> fetchPopulatedCharacters(ScannedData scannedData) {
        return (ArrayList<PopulatedCharacter>) Flux.fromIterable(scannedData.getCharacters())
                .flatMap(character -> projectYattaClient.getCharacterDataAsync(character.getId())
                        .map(characterResponse -> mapToPopulatedCharacter(character, characterResponse)))
                .collectList()
                .block();
    }

    private ArrayList<PopulatedLightCone> fetchPopulatedLightCones(ScannedData scannedData) {
        return (ArrayList<PopulatedLightCone>) Flux.fromIterable(scannedData.getLightCones())
                .flatMap(lightCone -> projectYattaClient.getLightConeDataAsync(lightCone.getId())
                        .map(lightConeResponse -> mapToPopulatedLightCone(lightCone, lightConeResponse)))
                .collectList()
                .block();
    }

    private PopulatedCharacter mapToPopulatedCharacter(Character character, ProjectYattaResponse characterResponse) {
        ProjectYattaData cd = characterResponse.getData();
        Upgrade characterUpgrade = cd.getUpgrade().get(character.getAscension());

        PopulatedCharacter populatedCharacter = new PopulatedCharacter();
        populatedCharacter.setBaseHp(calculateBaseValue(characterUpgrade.getSkillBase().getHPBase(), characterUpgrade.getSkillAdd().getHPAdd(), character.getLevel()));
        populatedCharacter.setBaseAtk(calculateBaseValue(characterUpgrade.getSkillBase().getAttackBase(), characterUpgrade.getSkillAdd().getAttackAdd(), character.getLevel()));
        populatedCharacter.setBaseDef(calculateBaseValue(characterUpgrade.getSkillBase().getDefenceBase(), characterUpgrade.getSkillAdd().getDefenceAdd(), character.getLevel()));
        populatedCharacter.setBaseSpd(characterUpgrade.getSkillBase().getSpeedBase());
        populatedCharacter.setBaseAggro(characterUpgrade.getSkillBase().getBaseAggro());
        populatedCharacter.setCriticalChance(characterUpgrade.getSkillBase().getCriticalChance() * 100);
        populatedCharacter.setCriticalDamage(characterUpgrade.getSkillBase().getCriticalDamage() * 100);
        populatedCharacter.setStatBonus(calculateStatBonuses(cd, character));
        populatedCharacter.setName(character.getName());
        populatedCharacter.setAscension(character.getAscension());
        populatedCharacter.setEidolon(character.getEidolon());
        populatedCharacter.setId(character.getId());
        populatedCharacter.setLevel(character.getLevel());
        populatedCharacter.setPath(character.getPath());
        populatedCharacter.setSkills(character.getSkills());
        populatedCharacter.setTraces(character.getTraces());
        return populatedCharacter;
    }

    private PopulatedLightCone mapToPopulatedLightCone(LightCone lightCone, ProjectYattaResponse lightConeResponse) {
        ProjectYattaData cd = lightConeResponse.getData();
        Upgrade lightConeUpgrade = cd.getUpgrade().get(lightCone.getAscension());

        PopulatedLightCone populatedLightCone = new PopulatedLightCone();
        populatedLightCone.setBaseHp(calculateBaseValue(lightConeUpgrade.getSkillBase().getHPBase(), lightConeUpgrade.getSkillAdd().getHPAdd(), lightCone.getLevel()));
        populatedLightCone.setBaseAtk(calculateBaseValue(lightConeUpgrade.getSkillBase().getAttackBase(), lightConeUpgrade.getSkillAdd().getAttackAdd(), lightCone.getLevel()));
        populatedLightCone.setBaseDef(calculateBaseValue(lightConeUpgrade.getSkillBase().getDefenceBase(), lightConeUpgrade.getSkillAdd().getDefenceAdd(), lightCone.getLevel()));
        populatedLightCone.setName(lightCone.getName());
        populatedLightCone.setAscension(lightCone.getAscension());
        populatedLightCone.setId(lightCone.getId());
        populatedLightCone.setLevel(lightCone.getLevel());
        populatedLightCone.setSuperimposition(lightCone.getSuperimposition());
        populatedLightCone.setUid(lightCone.getUid());
        populatedLightCone.setLock(lightCone.isLock());
        populatedLightCone.setLocation(lightCone.getLocation());
        return populatedLightCone;
    }

    private float calculateBaseValue(float base, float add, int level) {
        return base + add * (level - 1);
    }

    private HashMap<Stats, Float> calculateStatBonuses(ProjectYattaData cd, Character character) {
        HashMap<Stats, Float> bonus = new HashMap<>();
        for (int i = 0; i < 10; i++) {
            String id = String.format("%d", 200 + i);
            if (character.getTraces().getStat(id)) {
                SubSkills subSkills = cd.getTraces().getSubSkills().get(character.getId() + id);
                Stats stats = titleMapper(subSkills.getName());
                Float bonusValue = bonus.computeIfAbsent(stats, k -> 0F);
                float addValue = subSkills.getStatusList().get(0).getValue();
                if (stats != Stats.SPD_FLAT) {
                    addValue *= 100;
                }
                bonus.put(stats, bonusValue + addValue);
            }
        }
        return bonus;
    }

    public Stats titleMapper(String title) {
        return switch (title) {
            case "HP Boost" -> Stats.HP_PERCENT;
            case "ATK Boost" -> Stats.ATK_PERCENT;
            case "DEF Boost" -> Stats.DEF_PERCENT;
            case "SPD Boost" -> Stats.SPD_FLAT;
            case "CRIT Rate Boost" -> Stats.CRIT_RATE;
            case "CRIT DMG Boost" -> Stats.CRIT_DMG;
            case "Effect RES Boost" -> Stats.EFF_RES;
            case "Break Boost", "Break Enhance" -> Stats.BRK_EFF;
            case "Energy Regeneration Boost" -> Stats.ENERGY_REG_RATE;
            case "Effect Hit Rate Boost" -> Stats.EFF_HIT_RATE;
            case "DMG Boost" -> Stats.CMN_DMG_BOOST;
            case "DMG Boost: Ice" -> Stats.ICE_DMG_BOOST;
            case "DMG Boost: Fire", "DMG Boost Fire" -> Stats.FIRE_DMG_BOOST;
            case "DMG Boost: Wind" -> Stats.WIND_DMG_BOOST;
            case "DMG Boost: Lightning" -> Stats.LIGHTNING_DMG_BOOST;
            case "DMG Boost: Imaginary" -> Stats.IMAGINARY_DMG_BOOST;
            case "DMG Boost: Quantum" -> Stats.QUANTUM_DMG_BOOST;
            case "DMG Boost: Physical" -> Stats.PHYSICAL_DMG_BOOST;
            default -> null;
        };
    }

    public float calculateStat(int rarity, Stats stat, int level) {
        return switch (rarity) {
            case 5 -> calculateStatForRarity5(stat, level);
            case 4 -> calculateStatForRarity4(stat, level);
            case 3 -> calculateStatForRarity3(stat, level);
            case 2 -> calculateStatForRarity2(stat, level);
            default -> 0.0f;
        };
    }

    private float calculateStatForRarity5(Stats stat, int level) {
        return (float) switch (stat) {
            case SPD_FLAT -> 4.032 + 1.4 * level;
            case HP_FLAT -> 112.896 + 39.5136 * level;
            case ATK_FLAT -> 56.448 + 19.7568 * level;
            case HP_PERCENT, ATK_PERCENT, EFF_HIT_RATE -> 6.9120 + 2.4192 * level;
            case DEF_PERCENT -> 8.64 + 3.024 * level;
            case BRK_EFF -> 10.3680 + 3.6277 * level;
            case ENERGY_REG_RATE -> 3.1104 + 1.0886 * level;
            case OTG_HEAL_BOOST -> 5.5296 + 1.9354 * level;
            case PHYSICAL_DMG_BOOST, FIRE_DMG_BOOST, ICE_DMG_BOOST, WIND_DMG_BOOST, LIGHTNING_DMG_BOOST, QUANTUM_DMG_BOOST, IMAGINARY_DMG_BOOST ->
                    6.2208 + 2.1773 * level;
            case CRIT_RATE -> 5.184 + 1.8144 * level;
            case CRIT_DMG -> 10.368 + 3.6288 * level;
            default -> 0.0f;
        };
    }

    private float calculateStatForRarity4(Stats stat, int level) {
        return (float) switch (stat) {
            case SPD_FLAT -> 3.2256 + 1.1 * level;
            case HP_FLAT -> 90.3168 + 31.61088 * level;
            case ATK_FLAT -> 45.1584 + 15.80544 * level;
            case HP_PERCENT, ATK_PERCENT, EFF_HIT_RATE -> 5.5296 + 1.9354 * level;
            case DEF_PERCENT -> 6.912 + 2.4192 * level;
            case BRK_EFF, CRIT_DMG -> 8.2944 + 2.9030 * level;
            case ENERGY_REG_RATE -> 2.4883 + 0.8709 * level;
            case OTG_HEAL_BOOST -> 4.4237 + 1.5483 * level;
            case PHYSICAL_DMG_BOOST, FIRE_DMG_BOOST, ICE_DMG_BOOST, WIND_DMG_BOOST, LIGHTNING_DMG_BOOST, QUANTUM_DMG_BOOST, IMAGINARY_DMG_BOOST ->
                    4.9766 + 1.7418 * level;
            case CRIT_RATE -> 4.1472 + 1.4515 * level;
            default -> 0.0f;
        };
    }

    private float calculateStatForRarity3(Stats stat, int level) {
        return (float) switch (stat) {
            case SPD_FLAT -> 2.4192 + level;
            case HP_FLAT -> 67.7376 + 23.70816 * level;
            case ATK_FLAT -> 33.8688 + 11.85408 * level;
            case HP_PERCENT, ATK_PERCENT, EFF_HIT_RATE -> 4.1472 + 1.4515 * level;
            case DEF_PERCENT -> 5.184 + 1.8144 * level;
            case BRK_EFF, CRIT_DMG -> 6.2208 + 2.1773 * level;
            case ENERGY_REG_RATE -> 1.8662 + 0.6532 * level;
            case OTG_HEAL_BOOST -> 3.3178 + 1.1612 * level;
            case PHYSICAL_DMG_BOOST, FIRE_DMG_BOOST, ICE_DMG_BOOST, WIND_DMG_BOOST, LIGHTNING_DMG_BOOST, QUANTUM_DMG_BOOST, IMAGINARY_DMG_BOOST ->
                    3.7325 + 1.3064 * level;
            case CRIT_RATE -> 3.1104 + 1.0886 * level;
            default -> 0.0f;
        };
    }

    private float calculateStatForRarity2(Stats stat, int level) {
        return (float) switch (stat) {
            case SPD_FLAT -> 1.6128 + level;
            case HP_FLAT -> 45.1584 + 15.80544 * level;
            case ATK_FLAT -> 22.5792 + 7.90272 * level;
            case HP_PERCENT, ATK_PERCENT, EFF_HIT_RATE -> 2.7648 + 0.9677 * level;
            case DEF_PERCENT -> 3.456 + 1.2096 * level;
            case BRK_EFF, CRIT_DMG -> 4.1472 + 1.4515 * level;
            case ENERGY_REG_RATE -> 1.2442 + 0.4355 * level;
            case OTG_HEAL_BOOST -> 2.2118 + 0.7741 * level;
            case PHYSICAL_DMG_BOOST, FIRE_DMG_BOOST, ICE_DMG_BOOST, WIND_DMG_BOOST, LIGHTNING_DMG_BOOST, QUANTUM_DMG_BOOST, IMAGINARY_DMG_BOOST ->
                    2.4883 + 0.8709 * level;
            case CRIT_RATE -> 2.0736 + 0.7258 * level;
            default -> 0.0f;
        };
    }
}
