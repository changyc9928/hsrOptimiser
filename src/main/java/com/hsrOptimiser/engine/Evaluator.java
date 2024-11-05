package com.hsrOptimiser.engine;

import com.hsrOptimiser.domain.CharacterStats;
import com.hsrOptimiser.domain.EnemySetup;
import com.hsrOptimiser.domain.hsrScanner.Relic;
import com.hsrOptimiser.domain.hsrScanner.populatedData.PopulatedCharacter;
import com.hsrOptimiser.domain.hsrScanner.populatedData.PopulatedLightCone;
import com.hsrOptimiser.domain.hsrScanner.populatedData.PopulatedRelic;
import com.hsrOptimiser.properties.Properties;
import com.hsrOptimiser.properties.StatBonus;
import com.hsrOptimiser.properties.StatBonusCondition;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
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

    private PopulatedCharacter character;
    private PopulatedLightCone lightCone;
    private EnemySetup enemySetup;
    private String targetFormula;
    private String targetName;

    @Autowired
    private Properties properties;

    private static Set<String> extractVariables(final String expression) {
        final Set<String> variables = new HashSet<>();
        final Pattern pattern = Pattern.compile("[a-zA-Z_][a-zA-Z0-9_.]*");
        final Matcher matcher = pattern.matcher(expression);

        while (matcher.find()) {
            variables.add(matcher.group());
        }
        return variables;
    }

    public static Map<String, Float> deepCopy(final Map<String, Float> original) {
        return original.entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private float evaluateFormula(final Map<String, Float> otherBonuses, final String formula) {
        final Set<String> variables = extractVariables(formula);
        final Expression expression = new ExpressionBuilder(formula).variables(variables).build();

        setOtherBonuses(expression, variables, otherBonuses);
        setCharacterVariables(expression);
        setLightConeVariables(expression);
        setEnemyVariables(expression);

        return (float) expression.evaluate();
    }

    private void setCharacterVariables(final Expression expression) {
        expression.setVariable("Character_HP", character.getBaseHp());
        expression.setVariable("Character_ATK", character.getBaseAtk());
        expression.setVariable("Character_DEF", character.getBaseDef());
        expression.setVariable("Character_SPD", character.getBaseSpd());
        expression.setVariable("Character_Base_CRIT_Rate", character.getCriticalChance());
        expression.setVariable("Character_Base_CRIT_DMG", character.getCriticalDamage());
    }

    private void setLightConeVariables(final Expression expression) {
        if (lightCone != null) {
            expression.setVariable("LightCone_HP", lightCone.getBaseHp());
            expression.setVariable("LightCone_ATK", lightCone.getBaseAtk());
            expression.setVariable("LightCone_DEF", lightCone.getBaseDef());
        }
    }

    private void setEnemyVariables(final Expression expression) {
        expression.setVariable("Enemy_Resistance", enemySetup.getResistance());
        expression.setVariable("Enemy_Level", enemySetup.getLevel());
    }

    private void setOtherBonuses(final Expression expression, final Set<String> variables,
        final Map<String, Float> otherBonuses) {
        for (final String variable : variables) {
            // TODO: 05/11/2024 Only allow variables with SCREAMING_SNAKE_CASE to get values from here
            expression.setVariable(variable, otherBonuses.getOrDefault(variable, 0F));
        }
    }

    public CharacterStats getStatDetails(final List<PopulatedRelic> relics,
        final Map<String, Float> otherBonuses) {
        final CharacterStats characterStats = new CharacterStats();
        characterStats.setCharacter(character);
        characterStats.setLightCone(lightCone);
        characterStats.setRelics((ArrayList<PopulatedRelic>) relics);

        final Map<String, Float> updatedBonuses = updateBonusesFromRelicsAndCharacter(relics,
            otherBonuses);

        final HashMap<String, Float> stats = new HashMap<>();
        properties.getFormula().getBaseStat().forEach((stat, formula) -> {
            ArrayList<String> correspondingStat = new ArrayList<>();
            switch (stat) {
                case ("HP") -> {
                    correspondingStat.add("HP_FLAT");
                    correspondingStat.add("HP_PERCENT");
                }
                case ("ATK") -> {
                    correspondingStat.add("ATK_FLAT");
                    correspondingStat.add("ATK_PERCENT");
                }
                case ("DEF") -> {
                    correspondingStat.add("DEF_FLAT");
                    correspondingStat.add("DEF_PERCENT");
                }
                case ("SPD") -> {
                    correspondingStat.add("SPD_FLAT");
                    correspondingStat.add("SPD_PERCENT");
                }
                default -> correspondingStat.add(stat);
            }
            correspondingStat.forEach(s -> {
                float result = calculateStatValue(relics, updatedBonuses, formula, s);
                stats.put(stat, result);
            });
        });

        characterStats.setStats(stats);
        return characterStats;
    }

    private Map<String, Float> updateBonusesFromRelicsAndCharacter(
        final List<PopulatedRelic> relics, final Map<String, Float> otherBonuses) {
        final Map<String, Float> updatedBonuses = new HashMap<>(otherBonuses);

        relics.forEach(populatedRelic -> {
            updatedBonuses.merge(populatedRelic.getMainStat(), populatedRelic.getMainStatValue(),
                Float::sum);
            populatedRelic.getSubStats().forEach(
                subStats -> updatedBonuses.merge(subStats.getKey(), subStats.getValue(),
                    Float::sum));
        });

        character.getStatBonus()
            .forEach((stat, bonus) -> updatedBonuses.merge(stat, bonus, Float::sum));

        // TODO: 05/11/2024 Get bonus from light cone as well 

        return updatedBonuses;
    }

    private float calculateStatValue(final List<PopulatedRelic> relics,
        final Map<String, Float> otherBonuses,
        final String formula, final String stat) {
        final Map<String, Long> relicSetCount = getRelicSetCount(relics);

        applySetBonuses(relicSetCount, relics, otherBonuses, stat);

        return evaluateFormula(otherBonuses, formula);
    }

    private Map<String, Long> getRelicSetCount(final List<PopulatedRelic> relics) {
        return relics.stream()
            .map(Relic::getSetId)
            .collect(Collectors.groupingBy(item -> item, Collectors.counting()));
    }

    private void applySetBonuses(final Map<String, Long> relicSetCount,
        final List<PopulatedRelic> relics,
        final Map<String, Float> otherBonuses, final String stat) {
        relicSetCount.forEach((set, count) -> {
            if (properties.getSetBonus().containsKey(set)) {
                final HashMap<Integer, ArrayList<StatBonus>> setBonus = properties.getSetBonus()
                    .get(set);
                for (int i = 1; i <= count; i++) {
                    if (setBonus.containsKey(i)) {
                        setBonus.get(i).forEach(
                            statBonus -> applyBonusIfConditionsMet(relics, otherBonuses, statBonus,
                                stat));
                    }
                }
            }
        });
    }

    private void applyBonusIfConditionsMet(final List<PopulatedRelic> relics,
        final Map<String, Float> otherBonuses,
        final StatBonus statBonus, final String stat) {
        if (Objects.equals(statBonus.getStat(), stat) && areConditionsMet(relics, otherBonuses,
            statBonus)) {
            otherBonuses.merge(statBonus.getStat(), statBonus.getValue(), Float::sum);
        }
    }

    private boolean areConditionsMet(final List<PopulatedRelic> relics,
        final Map<String, Float> otherBonuses, final StatBonus statBonus) {
        final AtomicBoolean conditionMet = new AtomicBoolean(true);

        statBonus.getCondition().forEach(statBonusCondition -> {
            if (!isConditionMet(relics, otherBonuses, statBonusCondition)) {
                conditionMet.set(false);
            }
        });

        return conditionMet.get();
    }

    private boolean isConditionMet(final List<PopulatedRelic> relics,
        final Map<String, Float> otherBonuses,
        final StatBonusCondition statBonusCondition) {
        final Map<String, Float> thisBonus = new HashMap<>(otherBonuses);
        final String formula = properties.getFormula().getBaseStat()
            .get(statBonusCondition.getStat());
        final float value = calculateStatValue(relics, thisBonus, formula,
            statBonusCondition.getStat());
        return value >= statBonusCondition.getValue();
    }
}
