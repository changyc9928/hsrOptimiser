package com.hsrOptimiser.domain.hsrScanner;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

import java.io.Serializable;

@Data
public class CharacterTraces implements Serializable {
    @JsonAlias("ability_1")
    boolean ability1;
    @JsonAlias("ability_2")
    boolean ability2;
    @JsonAlias("ability_3")
    boolean ability3;
    @JsonAlias("stat_1")
    boolean stat1;
    @JsonAlias("stat_2")
    boolean stat2;
    @JsonAlias("stat_3")
    boolean stat3;
    @JsonAlias("stat_4")
    boolean stat4;
    @JsonAlias("stat_5")
    boolean stat5;
    @JsonAlias("stat_6")
    boolean stat6;
    @JsonAlias("stat_7")
    boolean stat7;
    @JsonAlias("stat_8")
    boolean stat8;
    @JsonAlias("stat_9")
    boolean stat9;
    @JsonAlias("stat_10")
    boolean stat10;

    public boolean getStat(String id) {
        switch (id) {
            case "201" -> {
                return stat1;
            }
            case "202" -> {
                return stat2;
            }
            case "203" -> {
                return stat3;
            }
            case "204" -> {
                return stat4;
            }
            case "205" -> {
                return stat5;
            }
            case "206" -> {
                return stat6;
            }
            case "207" -> {
                return stat7;
            }
            case "208" -> {
                return stat8;
            }
            case "209" -> {
                return stat9;
            }
            case "210" -> {
                return stat10;
            }
        }
        return false;
    }
}
