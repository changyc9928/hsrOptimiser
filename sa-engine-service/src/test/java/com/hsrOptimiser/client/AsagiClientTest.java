package com.hsrOptimiser.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hsrOptimiser.DTO.asagi.MocRequest;
import com.hsrOptimiser.DTO.asagi.MocResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class AsagiClientTest {

    @Autowired
    private AsagiClient asagiClient;

    @Test
    public void asagiClientTest() throws JsonProcessingException {

        String json = """
            {
              "bronya_strategy": 1,
              "topaz_strategy": 0,
              "castorice_strategy": false,
              "boothill_strategy": 2,
              "wave_set": null,
              "characters": [
                {
                  "key": "Evanescia",
                  "name": "Evanescia",
                  "eidolon": 0,
                  "light_cones": "UntilFlowersBloomAgain",
                  "light_cones_eidolon": 1,
                  "job": "elation",
                  "relic_set": {
                    "ornament": "Punklorde",
                    "set1": "MagicalGirls",
                    "set2": "MagicalGirls"
                  },
                  "relic_main": {
                    "body": "crit_dmg",
                    "feet": "atk_rate",
                    "sphere": "deal_dmg",
                    "rope": "ep_rate"
                  },
                  "relic_sub": {
                    "atk_flat": 0,
                    "atk_rate": 0,
                    "hp_flat": 0,
                    "hp_rate": 0,
                    "def_flat": 0,
                    "def_rate": 0,
                    "spd_flat": 0,
                    "crit_dmg": 12,
                    "crit_rate": 12,
                    "effect_hit": 0,
                    "effect_res": 0,
                    "break_rate": 0
                  },
                  "use_technique": true,
                  "enable_eidolon": true,
                  "reality": 5,
                  "speed": 104,
                  "type": 1,
                  "light_cones_obj": {
                    "key": "UntilFlowersBloomAgain",
                    "reality": 5,
                    "type": 1
                  },
                  "note": null,
                  "add_speed": 5,
                  "skill_priority": 200
                },
                {
                  "key": "YaoGuang",
                  "name": "YaoGuang",
                  "eidolon": 0,
                  "light_cones": "WhenSheDecidedToSee",
                  "light_cones_eidolon": 1,
                  "job": "elation",
                  "relic_set": {
                    "ornament": "ConvergingStars",
                    "set1": "Diviners",
                    "set2": "Diviners"
                  },
                  "relic_main": {
                    "body": "crit_rate",
                    "feet": "spd_flat",
                    "sphere": "def_rate",
                    "rope": "ep_rate"
                  },
                  "relic_sub": {
                    "atk_flat": 0,
                    "atk_rate": 0,
                    "hp_flat": 0,
                    "hp_rate": 0,
                    "def_flat": 0,
                    "def_rate": 0,
                    "spd_flat": 12,
                    "crit_dmg": 2,
                    "crit_rate": 12,
                    "effect_hit": 0,
                    "effect_res": 0,
                    "break_rate": 0
                  },
                  "use_technique": true,
                  "enable_eidolon": true,
                  "reality": 5,
                  "speed": 101,
                  "type": 1,
                  "light_cones_obj": {
                    "key": "WhenSheDecidedToSee",
                    "reality": 0,
                    "type": 0
                  },
                  "note": null,
                  "add_speed": 9,
                  "skill_priority": 150
                },
                {
                  "key": "TrailblazerElation",
                  "name": "TrailblazerElation",
                  "eidolon": 6,
                  "light_cones": "MushyAhroomysAdventures",
                  "light_cones_eidolon": 5,
                  "job": "elation",
                  "relic_set": {
                    "ornament": "BrokenKeel",
                    "set1": "Diviners",
                    "set2": "Diviners"
                  },
                  "relic_main": {
                    "body": "crit_rate",
                    "feet": "spd_flat",
                    "sphere": "atk_rate",
                    "rope": "ep_rate"
                  },
                  "relic_sub": {
                    "atk_flat": 0,
                    "atk_rate": 0,
                    "hp_flat": 0,
                    "hp_rate": 0,
                    "def_flat": 0,
                    "def_rate": 0,
                    "spd_flat": 11,
                    "crit_dmg": 7,
                    "crit_rate": 4,
                    "effect_hit": 0,
                    "effect_res": 4,
                    "break_rate": 0
                  },
                  "use_technique": true,
                  "enable_eidolon": true,
                  "reality": 4,
                  "speed": 106,
                  "type": 1,
                  "light_cones_obj": {
                    "key": "MushyAhroomysAdventures",
                    "reality": 4,
                    "type": 2
                  },
                  "note": null,
                  "add_speed": 0,
                  "skill_priority": 100
                },
                {
                  "key": "Huohuo",
                  "name": "Huohuo",
                  "eidolon": 0,
                  "light_cones": "NightOfFright",
                  "light_cones_eidolon": 1,
                  "job": "abundance",
                  "relic_set": {
                    "ornament": "Lushakas",
                    "set1": "Warrior",
                    "set2": "Warrior"
                  },
                  "relic_main": {
                    "body": "heal_rate",
                    "feet": "spd_flat",
                    "sphere": "hp_rate",
                    "rope": "ep_rate"
                  },
                  "relic_sub": {
                    "atk_flat": 0,
                    "atk_rate": 0,
                    "hp_flat": 0,
                    "hp_rate": 10,
                    "def_flat": 0,
                    "def_rate": 0,
                    "spd_flat": 12,
                    "crit_dmg": 0,
                    "crit_rate": 0,
                    "effect_hit": 0,
                    "effect_res": 4,
                    "break_rate": 0
                  },
                  "use_technique": true,
                  "enable_eidolon": true,
                  "reality": 5,
                  "speed": 98,
                  "type": 1,
                  "light_cones_obj": {
                    "key": "NightOfFright",
                    "reality": 5,
                    "type": 1
                  },
                  "note": "huohuo_note",
                  "add_speed": 5,
                  "skill_priority": 200
                }
              ],
              "start_sp": 3,
              "distribute_ep": true,
              "create_link": false,
              "battle_mode": "multi",
              "boss_weaks": [
                "Physical",
                "Fire",
                "Ice",
                "Wind",
                "Lightning",
                "Imaginary",
                "Quantum"
              ],
              "boss2_weaks": [
                "Physical",
                "Fire",
                "Ice",
                "Wind",
                "Lightning",
                "Imaginary",
                "Quantum"
              ],
              "follower_weaks": [
                "Physical",
                "Fire",
                "Ice",
                "Wind",
                "Lightning",
                "Imaginary",
                "Quantum"
              ],
              "healer_speed": 134,
              "enable_break": true,
              "enable_defeated": false,
              "round": 6,
              "enemy_level": "base90",
              "fix_break": true,
              "exo_toughness": true,
              "enable_wave_reset": false,
              "input_actual_number": true,
              "total_sub_status": [
                {
                  "key": "Evanescia",
                  "hp_flat": 0,
                  "hp_rate": 0,
                  "atk_flat": 0,
                  "atk_rate": 0,
                  "def_flat": 0,
                  "def_rate": 0,
                  "spd_flat": 0,
                  "crit_rate": 0,
                  "crit_dmg": 0,
                  "effect_hit": 0,
                  "effect_res": 0,
                  "break_rate": 0
                },
                {
                  "key": "YaoGuang",
                  "hp_flat": 0,
                  "hp_rate": 0,
                  "atk_flat": 0,
                  "atk_rate": 0,
                  "def_flat": 0,
                  "def_rate": 0,
                  "spd_flat": 0,
                  "crit_rate": 0,
                  "crit_dmg": 0,
                  "effect_hit": 0,
                  "effect_res": 0,
                  "break_rate": 0
                },
                {
                  "key": "TrailblazerElation",
                  "hp_flat": 0,
                  "hp_rate": 0,
                  "atk_flat": 0,
                  "atk_rate": 0,
                  "def_flat": 0,
                  "def_rate": 0,
                  "spd_flat": 0,
                  "crit_rate": 0,
                  "crit_dmg": 0,
                  "effect_hit": 0,
                  "effect_res": 0,
                  "break_rate": 0
                },
                {
                  "key": "Huohuo",
                  "hp_flat": 0,
                  "hp_rate": 0,
                  "atk_flat": 0,
                  "atk_rate": 0,
                  "def_flat": 0,
                  "def_rate": 0,
                  "spd_flat": 0,
                  "crit_rate": 0,
                  "crit_dmg": 0,
                  "effect_hit": 0,
                  "effect_res": 0,
                  "break_rate": 0
                }
              ]
            }
            """;

        ObjectMapper mapper = new ObjectMapper();

        MocRequest req = mapper.readValue(json, MocRequest.class);

        System.out.println(req);

        MocResponse mocResponse = asagiClient.calculateDamage(req);
        System.out.println(mocResponse);
    }
}
