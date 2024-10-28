package com.hsrOptimiser.domain.hsrScanner;

import com.fasterxml.jackson.annotation.JsonAlias;

public enum Stats {
    @JsonAlias("ATK")
    ATK_FLAT,
    @JsonAlias("ATK_")
    ATK_PERCENT,
    @JsonAlias("DEF")
    DEF_FLAT,
    @JsonAlias("DEF_")
    DEF_PERCENT,
    @JsonAlias("HP")
    HP_FLAT,
    @JsonAlias("HP_")
    HP_PERCENT,
    @JsonAlias({"CRIT Rate_", "CRIT Rate"})
    CRIT_RATE,
    @JsonAlias({"CRIT DMG_", "CRIT DMG"})
    CRIT_DMG,
    @JsonAlias("SPD")
    SPD_FLAT,
    @JsonAlias("SPD_")
    SPD_PERCENT,
    @JsonAlias("Energy Regeneration Rate")
    ENERGY_REG_RATE,
    @JsonAlias({"Effect Hit Rate_", "Effect Hit Rate"})
    EFF_HIT_RATE,
    @JsonAlias("Effect RES_")
    EFF_RES,
    @JsonAlias({"Break Effect_", "Break Effect"})
    BRK_EFF,
    @JsonAlias("Outgoing Healing Boost")
    OTG_HEAL_BOOST,
    @JsonAlias("Fire DMG Boost")
    FIRE_DMG_BOOST,
    @JsonAlias("Ice DMG Boost")
    ICE_DMG_BOOST,
    @JsonAlias("Wind DMG Boost")
    WIND_DMG_BOOST,
    @JsonAlias("Lightning DMG Boost")
    LIGHTNING_DMG_BOOST,
    @JsonAlias("Quantum DMG Boost")
    QUANTUM_DMG_BOOST,
    @JsonAlias("Imaginary DMG Boost")
    IMAGINARY_DMG_BOOST,
    @JsonAlias("Physical DMG Boost")
    PHYSICAL_DMG_BOOST,
    @JsonAlias("DMG Boost")
    CMN_DMG_BOOST,
    @JsonAlias("Basic ATK DMG Boost")
    BSC_ATK_DMG_BOOST,
    @JsonAlias("Skill DMG Boost")
    SKL_DMG_BOOST,
    @JsonAlias("Ultimate DMG Boost")
    ULT_DMG_BOOST,
    @JsonAlias("Follow-up ATK DMG Boost")
    FUP_ATK_DMG_BOOST,
    @JsonAlias("Shield DMG Absorption")
    SHIELD_DMG_ABS,
    @JsonAlias("DMG Reduction")
    DMG_RED,
    @JsonAlias("DEF Reduction")
    DEF_RED,
    @JsonAlias("DEF Ignore")
    DEF_IGN,
    @JsonAlias("Break DMG DEF Ignore")
    BRK_DMG_DEF_IGN,
    @JsonAlias("Super Break DMG DEF Ignore")
    SPR_BRK_DMG_DEF_IGN,
    REGULAR_DMG,
}
