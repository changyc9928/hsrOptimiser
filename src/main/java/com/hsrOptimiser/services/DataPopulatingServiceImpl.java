package com.hsrOptimiser.services;

import com.hsrOptimiser.client.ProjectYattaClient;
import com.hsrOptimiser.domain.hsrScanner.Character;
import com.hsrOptimiser.domain.hsrScanner.LightCone;
import com.hsrOptimiser.domain.hsrScanner.Relic;
import com.hsrOptimiser.domain.hsrScanner.ScannedData;
import com.hsrOptimiser.domain.hsrScanner.Slot;
import com.hsrOptimiser.domain.hsrScanner.populatedData.PopulatedCharacter;
import com.hsrOptimiser.domain.hsrScanner.populatedData.PopulatedData;
import com.hsrOptimiser.domain.hsrScanner.populatedData.PopulatedLightCone;
import com.hsrOptimiser.domain.hsrScanner.populatedData.PopulatedRelic;
import com.hsrOptimiser.domain.projectYatta.ProjectYattaData;
import com.hsrOptimiser.domain.projectYatta.ProjectYattaResponse;
import com.hsrOptimiser.domain.projectYatta.SubSkills;
import com.hsrOptimiser.domain.projectYatta.Upgrade;
import com.hsrOptimiser.properties.Properties;
import com.hsrOptimiser.properties.RarityStat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class DataPopulatingServiceImpl implements DataPopulatingService {

    @Autowired
    ProjectYattaClient projectYattaClient;
    @Autowired
    Properties properties;

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
        populatedRelic.setMainStatValue(
            calculateStat(relic.getRarity(), relic.getMainStat(), relic.getLevel()));
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
        if (!relic.getSlot().equals(Slot.Head) && relic.getMainStat().equals("HP_FLAT")) {
            relic.setMainStat("HP_PERCENT");
        } else if (!relic.getSlot().equals(Slot.Hands) && relic.getMainStat().equals("ATK_FLAT")) {
            relic.setMainStat("ATK_PERCENT");
        } else if (relic.getMainStat().equals("DEF_FLAT")) {
            relic.setMainStat("DEF_PERCENT");
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

    private PopulatedCharacter mapToPopulatedCharacter(Character character,
        ProjectYattaResponse characterResponse) {
        ProjectYattaData cd = characterResponse.getData();
        Upgrade characterUpgrade = cd.getUpgrade().get(character.getAscension());

        PopulatedCharacter populatedCharacter = new PopulatedCharacter();
        populatedCharacter.setBaseHp(calculateBaseValue(characterUpgrade.getSkillBase().getHPBase(),
            characterUpgrade.getSkillAdd().getHPAdd(), character.getLevel()));
        populatedCharacter.setBaseAtk(
            calculateBaseValue(characterUpgrade.getSkillBase().getAttackBase(),
                characterUpgrade.getSkillAdd().getAttackAdd(), character.getLevel()));
        populatedCharacter.setBaseDef(
            calculateBaseValue(characterUpgrade.getSkillBase().getDefenceBase(),
                characterUpgrade.getSkillAdd().getDefenceAdd(), character.getLevel()));
        populatedCharacter.setBaseSpd(characterUpgrade.getSkillBase().getSpeedBase());
        populatedCharacter.setBaseAggro(characterUpgrade.getSkillBase().getBaseAggro());
        populatedCharacter.setCriticalChance(
            characterUpgrade.getSkillBase().getCriticalChance() * 100);
        populatedCharacter.setCriticalDamage(
            characterUpgrade.getSkillBase().getCriticalDamage() * 100);
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

    private PopulatedLightCone mapToPopulatedLightCone(LightCone lightCone,
        ProjectYattaResponse lightConeResponse) {
        ProjectYattaData cd = lightConeResponse.getData();
        Upgrade lightConeUpgrade = cd.getUpgrade().get(lightCone.getAscension());

        PopulatedLightCone populatedLightCone = new PopulatedLightCone();
        populatedLightCone.setBaseHp(calculateBaseValue(lightConeUpgrade.getSkillBase().getHPBase(),
            lightConeUpgrade.getSkillAdd().getHPAdd(), lightCone.getLevel()));
        populatedLightCone.setBaseAtk(
            calculateBaseValue(lightConeUpgrade.getSkillBase().getAttackBase(),
                lightConeUpgrade.getSkillAdd().getAttackAdd(), lightCone.getLevel()));
        populatedLightCone.setBaseDef(
            calculateBaseValue(lightConeUpgrade.getSkillBase().getDefenceBase(),
                lightConeUpgrade.getSkillAdd().getDefenceAdd(), lightCone.getLevel()));
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

    private HashMap<String, Float> calculateStatBonuses(ProjectYattaData cd, Character character) {
        HashMap<String, Float> bonus = new HashMap<>();
        for (int i = 0; i < 10; i++) {
            String id = String.format("%d", 200 + i);
            if (character.getTraces().getStat(id)) {
                SubSkills subSkills = cd.getTraces().getSubSkills().get(character.getId() + id);
                String stats = properties.getTraceTitleMapper().get(subSkills.getName());
                Float bonusValue = bonus.computeIfAbsent(stats, k -> 0F);
                float addValue = subSkills.getStatusList().get(0).getValue();
                if (!stats.endsWith("FLAT")) {
                    addValue *= 100;
                }
                bonus.put(stats, bonusValue + addValue);
            }
        }
        return bonus;
    }

    public float calculateStat(int rarity, String stat, int level) {
        HashMap<String, RarityStat> rarityStat = properties.getRarityStats().get(rarity);
        RarityStat statValue = rarityStat.get(stat);
        return statValue.getBase() + statValue.getMultiplier() * level;
    }
}
