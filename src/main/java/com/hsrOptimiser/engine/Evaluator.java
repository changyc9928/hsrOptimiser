package com.hsrOptimiser.engine;

import com.hsrOptimiser.domain.CharacterStats;
import com.hsrOptimiser.domain.EnemySetup;
import com.hsrOptimiser.domain.hsrScanner.Relic;
import com.hsrOptimiser.domain.hsrScanner.populatedData.PopulatedCharacter;
import com.hsrOptimiser.domain.hsrScanner.populatedData.PopulatedLightCone;
import com.hsrOptimiser.domain.hsrScanner.populatedData.PopulatedRelic;
import com.hsrOptimiser.properties.Properties;
import com.hsrOptimiser.properties.StatBonus;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.Setter;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Setter
public class Evaluator {

    PopulatedCharacter character;
    PopulatedLightCone lightCone;
    EnemySetup enemySetup;
    String targetFormula;
    String targetName;

    @Autowired
    Properties properties;

    private static Set<String> extractVariables(String expression) {
        Set<String> variables = new HashSet<>();
        // Regex pattern to find words that are likely variable names
        Pattern pattern = Pattern.compile("[a-zA-Z_][a-zA-Z0-9_.]*");
        Matcher matcher = pattern.matcher(expression);

        while (matcher.find()) {
            String variable = matcher.group();
            // Add to set (to avoid duplicates)
            variables.add(variable);
        }
        return variables;
    }

    private float evaluateFormula(HashMap<String, Float> otherBonuses, String formula) {
        Set<String> variables = extractVariables(formula);

        Expression expression = new ExpressionBuilder(formula).variables(variables).build();

        for (String variable : variables) {
            expression.setVariable(variable, otherBonuses.getOrDefault(variable, 0F));
        }

        expression.setVariable("Character_HP", character.getBaseHp());
        expression.setVariable("Character_ATK", character.getBaseAtk());
        expression.setVariable("Character_DEF", character.getBaseDef());
        expression.setVariable("Character_SPD", character.getBaseSpd());
        expression.setVariable("Character_Base_CRIT_Rate", character.getCriticalChance());
        expression.setVariable("Character_Base_CRIT_DMG", character.getCriticalDamage());

        if (lightCone != null) {
            expression.setVariable("LightCone_HP", lightCone.getBaseHp());
            expression.setVariable("LightCone_ATK", lightCone.getBaseAtk());
            expression.setVariable("LightCone_DEF", lightCone.getBaseDef());
        }

        expression.setVariable("Enemy_Resistance", enemySetup.getResistance());
        expression.setVariable("Enemy_Level", enemySetup.getLevel());

        return (float) expression.evaluate();
    }

    public CharacterStats getStatDetails(ArrayList<PopulatedRelic> relics,
        HashMap<String, Float> otherBonuses) {
        CharacterStats characterStats = new CharacterStats();
        characterStats.setCharacter(character);
        characterStats.setLightCone(lightCone);
        characterStats.setRelics(relics);

        HashMap<String, Float> stats = new HashMap<>();
        relics.forEach(populatedRelic -> {
            otherBonuses.merge(populatedRelic.getMainStat(), populatedRelic.getMainStatValue(),
                Float::sum);
            populatedRelic.getSubStats().forEach(subStats -> {
                otherBonuses.merge(subStats.getKey(), subStats.getValue(), Float::sum);
            });
        });
        character.getStatBonus().forEach((stat, bonus) -> {
            otherBonuses.merge(stat, bonus, Float::sum);
        });
        properties.getFormula().getBaseStat().forEach((stat, formula) -> {
            float result = calculateStatValue(relics, otherBonuses, formula);
            stats.put(stat, result);
        });
        characterStats.setStats(stats);
        return characterStats;
    }

    private float calculateStatValue(ArrayList<PopulatedRelic> relics,
        HashMap<String, Float> otherBonuses,
        String formula) {
        ArrayList<String> relicSets = relics.stream().map(Relic::getSetId).collect(
            Collectors.toCollection(ArrayList::new));
        Map<String, Long> relicSetCount = relicSets.stream()
            .collect(Collectors.groupingBy(item -> item, Collectors.counting()));
        relicSetCount.forEach((set, count) -> {
            if (properties.getSetBonus().containsKey(set)) {
                HashMap<Integer, ArrayList<StatBonus>> setBonus = properties.getSetBonus()
                    .get(set);
                if (setBonus.containsKey(count.intValue())) {
                    ArrayList<StatBonus> bonus = setBonus.get(count.intValue());
                    bonus.forEach(statBonus -> {
                        if (statBonus.getCondition() == null) {
                            otherBonuses.merge(statBonus.getStat(), statBonus.getValue(),
                                Float::sum);
                        } else {
                            float val = calculateStatValue(relics, otherBonuses,
                                properties.getFormula().getBaseStat()
                                    .get(statBonus.getCondition().getStat()));
                            if (val > statBonus.getCondition().getValue()) {
                                otherBonuses.merge(statBonus.getStat(), statBonus.getValue(),
                                    Float::sum);
                            }
                        }
                    });
                }
            }
        });
        return evaluateFormula(otherBonuses, formula);
    }
}
