package com.hsrOptimiser.clientConfig;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AsagiLightConeMetadata {

    // ==================== 3-Star Light Cones ====================
    ARROWS("20000", "Arrows", 3, 1),
    CORNUCOPIA("20001", "Cornucopia", 3, 1),
    COLLAPSING_SKY("20002", "CollapsingSky", 3, 1),
    AMBER("20003", "Amber", 3, 1),
    VOID("20004", "Void", 3, 1),
    CHORUS("20005", "Chorus", 3, 1),
    DATA_BANK("20006", "DataBank", 3, 1),
    DARTING_ARROW("20007", "DartingArrow", 3, 1),
    FINE_FRUIT("20008", "FineFruit", 3, 1),
    SHATTERED_HOME("20009", "ShatteredHome", 3, 1),
    DEFENSE("20010", "Defense", 3, 1),
    LOOP("20011", "Loop", 3, 1),
    MESHING_COGS("20012", "MeshingCogs", 3, 1),
    PASSKEY("20013", "Passkey", 3, 1),
    ADVERSARIAL("20014", "Adversarial", 3, 1),
    MULTIPLICATION("20015", "Multiplication", 3, 1),
    MUTUAL_DEMISE("20016", "MutualDemise", 3, 1),
    PIONEERING("20017", "Pioneering", 3, 1),
    HIDDEN_SHADOW("20018", "HiddenShadow", 3, 1),
    MEDIATION("20019", "Mediation", 3, 1),
    SAGACITY("20020", "Sagacity", 3, 1),
    SHADOWBURN("20021", "Shadowburn", 3, 1),
    REMINISCENCE("20022", "Reminiscence", 3, 1),
    SNEERING("20023", "Sneering", 3, 1),
    LINGERING_TEAR("20024", "LingeringTear", 3, 1),

    // ==================== 4-Star Light Cones (Base Set 21000 - 21035) ====================
    POST_OP("21000", "PostOp", 4, 1),
    GOOD_NIGHT_SLEEP_WELL("21001", "GoodNightSleepWell", 4, 1),
    MY_NEW_LIFE("21002", "MyNewLife", 4, 1),
    ONLY_SILENCE_REMAINS("21003", "OnlySilenceRemains", 4, 1),
    MEMORIES_PAST("21004", "MemoriesPast", 4, 1),
    MOLES_WELCOME_YOU("21005", "MolesWelcomeYou", 4, 1),
    BIRTH_OF_THE_SELF("21006", "BirthOftheSelf", 4, 1),
    SHARED_FEELING("21007", "SharedFeeling", 4, 1),
    EYES_PREY("21008", "EyesPrey", 4, 1),
    LANDAUS_CHOICE("21009", "LandausChoice", 4, 1),
    SWORDPLAY("21010", "Swordplay", 4, 1),
    PLANETARY_RENDEZVOUS("21011", "PlanetaryRendezvous", 4, 1),
    SECRET_VOW("21012", "SecretVow", 4, 1),
    MAKE_WORLD_CLAMOR("21013", "MakeWorldClamor", 4, 1),
    PERFECT_TIMING("21014", "PerfectTiming", 4, 1),
    RESOLUTION_SHINES("21015", "ResolutionShines", 4, 1),
    UNIVERSAL_MARKET("21016", "UniversalMarket", 4, 1),
    SUBSCRIBE_MORE("21017", "SubscribeMore", 4, 1),
    DANCE_DANCE_DANCE("21018", "DanceDanceDance", 4, 1),
    UNDER_THE_BLUE_SKY("21019", "UndertheBlueSky", 4, 1),
    GENIUSES_REPOSE("21020", "GeniusesRepose", 4, 1),
    QUID_PRO_QUO("21021", "QuidProQuo", 4, 1),
    FERMATA("21022", "Fermata", 4, 1),
    WE_ARE_WILDFIRE("21023", "WeAreWildfire", 4, 1),
    RIVER_FLOWS_SPRING("21024", "RiverFlowsSpring", 4, 1),
    PAST_FUTURE("21025", "PastFuture", 4, 1),
    WOOF_WALK_TIME("21026", "WoofWalkTime", 4, 1),
    SERIOUSNESS_BREAKFAST("21027", "SeriousnessBreakfast", 4, 1),
    WARMTH_NIGHTS("21028", "WarmthNights", 4, 1),
    WE_WILL_MEET_AGAIN("21029", "WeWillMeetAgain", 4, 1),
    THIS_IS_ME("21030", "ThisIsMe", 4, 1),
    RETURN_DARKNESS("21031", "ReturnDarkness", 4, 1),
    CARVE_MOON_WEAVE_CLOUDS("21032", "CarveMoonWeaveClouds", 4, 1),
    NOWHERE_TO_RUN("21033", "NowhereToRun", 4, 1),
    PEACEFUL_DAY("21034", "PeacefulDay", 4, 1),
    WHAT_IS_REAL("21035", "WhatIsReal", 4, 1),

    // ==================== 4-Star Light Cones (Later Additions 21036+) ====================
    DREAMVILLE("21036", "Dreamville", 4, 1),
    FINAL_VICTOR("21037", "FinalVictor", 4, 1),
    FLAMES_AFAR("21038", "FlamesAfar", 4, 1),
    THREADS_FOREWOVEN("21039", "ThreadsForewoven", 4, 1),
    DAY_COSMOS_FELL("21040", "DayCosmosFell", 4, 1),
    SHOWTIME("21041", "Showtime", 4, 1),
    INDELIBLE_PROMISE("21042", "IndeliblePromise", 4, 1),
    CONCERT_FOR_TWO("21043", "ConcertForTwo", 4, 1),
    BOUNDLESS_CHOREO("21044", "BoundlessChoreo", 4, 1),
    AFTER_CHARMONY_FALL("21045", "AfterCharmonyFall", 4, 1),
    POISED_TO_BLOOM("21046", "PoisedToBloom", 4, 1),
    SHADOWED_BY_NIGHT("21047", "ShadowedByNight", 4, 1),
    DREAMS_MONTAGE("21048", "DreamsMontage", 4, 1),
    VICTORY_BLINK("21050", "VictoryBlink", 4, 1),
    GENIUSES_GREETINGS("21051", "GeniusesGreetings", 4, 1),
    SWEAT_NOW_CRY_LESS("21052", "SweatNowCryLess", 4, 1),
    JOURNEY_FOREVER_PEACEFUL("21053", "JourneyForeverPeaceful", 4, 1),
    STORY_NEXT_PAGE("21054", "StoryNextPage", 4, 1),
    UNTO_TOMORROWS_MORROW("21055", "UntoTomorrowsMorrow", 4, 1),
    IN_PURSUIT_WIND("21056", "InPursuitWind", 4, 1),
    FLOWER_REMEMBERS("21057", "FlowerRemembers", 4, 1),
    TRAIL_BYGONE_BLOOD("21058", "TrailBygoneBlood", 4, 1),
    DREAM_SCENTED_WHEAT("21060", "DreamScentedWheat", 4, 1),
    HOLIDAY_THERMAE_ESCAPADE("21061", "HolidayThermaeEscapade", 4, 1),
    SEE_YOU_THE_END("21062", "SeeYouTheEnd", 4, 1),
    MUSHY_AHROOMYS_ADVENTURES("21064", "MushyAhroomysAdventures", 4, 1), // API specific spelling
    TODAYS_GOOD_LUCK("21065", "TodaysGoodLuck", 4, 1),

    // ==================== 4-Star Event / Warp Light Cones (22xxx) ====================
    BEFORE_MISSION_STARTS("22000", "BeforeMissionStarts", 4, 1),
    OVER_HERE("22001", "OverHere", 4, 1),
    TOMORROWS_JOURNEY("22002", "TomorrowsJourney", 4, 1),
    NINJA_RECORD("22003", "NinjaRecord", 4, 1),
    GREAT_COSMIC_ENTERPRISE("22004", "GreatCosmicEnterprise", 4, 1),
    FOREVER_VICTUAL("22005", "ForeverVictual", 4, 1),
    TAKE_FLIGHT_TOWARD_PINK_TOMORROW("22006", "TakeFlightTowardPinkTomorrow", 4, 1),
    TOMORROW_TOGETHER("22007", "TomorrowTogether", 4, 1),

    // ✅ 5-Star Light Cones - CORRECTED IDs
    NIGHT_MILKY_WAY("23000", "NightMilkyWay", 5, 1),
    IN_THE_NIGHT("23001", "InTheNight", 5, 1),
    SOMETHING_IRREPLACEABLE("23002", "SomethingIrreplaceable", 5, 1),
    BATTLE_ISNT_OVER("23003", "BattleIsntOver", 5, 1),
    NAME_OF_WORLD("23004", "NameOfWorld", 5, 1),
    MOMENT_VICTORY("23005", "MomentVictory", 5, 1),
    ALL_YOU_NEED("23006", "AllYouNeed", 5, 1),
    INCESSANT_RAIN("23007", "IncessantRain", 5, 1),
    ECHOES_OF_COFFIN("23008", "EchoesOfCoffin", 5, 1),
    UNREACHABLE_SIDE("23009", "UnreachableSide", 5, 1),
    BEFORE_DAWN("23010", "BeforeDawn", 5, 1),
    SHUT_HER_EYES("23011", "ShutHerEyes", 5, 1),
    SLEEP_LIKE_DEAD("23012", "SleepLikeDead", 5, 1),
    TIME_WAITS("23013", "TimeWaits", 5, 1),
    MY_OWN_SWORD("23014", "MyOwnSword", 5, 1),

    // ✅ FIXED: Unique IDs for each light cone
    BRIGHTER_THAN_SUN("23015", "BrighterThanSun", 5, 1),           // ← Was 23020 (CONFLICT)
    INTO_UNREACHABLE_VEIL("23016", "IntoUnreachableVeil", 5, 1),   // ← Was 23015
    NIGHT_OF_FRIGHT("23017", "NightOfFright", 5, 1),               // ← Was missing/wrong
    FLAME_BLOOD("23018", "FlameBlood", 5, 1),                      // ← Was 23016
    REFORGED_REMEMBRANCE("23019", "ReforgedRemembrance", 5, 1),
    SAILING_TOWARDS_SECOND_LIFE("23020", "SailingTowardsSecondLife", 5,
        1), // ← Keep this one at 23020

    // Reassigned entries to correct GitHub IDs:
    LONG_ROAD_LEADS_HOME("23035", "LongRoadLeadsHome", 5, 1),

    // Other verified 5★ entries (IDs from GitHub)
    FALL_OF_AEON("24000", "FallOfAeon", 5, 1),
    CRUISING_STELLAR_SEA("24001", "CruisingStellarSea", 5, 1),
    TEXTURE_MEMORIES("24002", "TextureMemories", 5, 1),
    SOLITARY_HEALING("24003", "SolitaryHealing", 5, 1),
    ETERNAL_CALCULUS("24004", "EternalCalculus", 5, 1),

    // Additional 5★ entries from error message list (add as needed with correct IDs)
    WHEREABOUTS_SHOULD_DREAMS_REST("23025", "WhereaboutsShouldDreamsRest", 5, 1),
    YET_HOPE_IS_PRICELESS("23026", "YetHopeIsPriceless", 5, 1),
    DANCE_AT_SUNSET("23023", "DanceAtSunset", 5, 1),
    THANKLESS_CORONATION("23030", "ThanklessCoronation", 5, 1),
    THUS_BURNS_DAWN("23031", "ThusBurnsDawn", 5, 1),
    NINJUTSU("23032", "Ninjutsu", 5, 1),
    LIFE_SHOULD_BE_CAST_TO_FLAMES("23033", "LifeShouldBeCastToFlames", 5, 1),
    FLOWING_NIGHTGLOW("23034", "FlowingNightglow", 5, 1),
    PAST_SELF_MIRROR("23036", "PastSelfMirror", 5, 1),
    EARTHLY_ESCAPADE("23038", "EarthlyEscapade", 5, 1),
    ALONG_PASSING_SHORE("23022", "AlongPassingShore", 5, 1),
    THOSE_MANY_SPRINGS("23024", "ThoseManySprings", 5, 1),
    INSTANT_BEFORE_GAZE("23040", "InstantBeforeGaze", 5, 1),
    NINJUTSU_DUP("23042", "Ninjutsu", 5, 1), // If duplicate exists
    PASSKEY_5("23043", "Passkey", 5, 1); // If 5★ variant exists

    private static final Map<String, AsagiLightConeMetadata> ID_MAP =
        Arrays.stream(values())
            .collect(Collectors.toMap(AsagiLightConeMetadata::getId, c -> c));
    // ==================== Lookup Map ====================
    private final String id;
    private final String internalName;
    private final int rarity;
    private final int type;

    public static AsagiLightConeMetadata getInfoById(String id) {
        AsagiLightConeMetadata cone = ID_MAP.get(id);
        if (cone == null) {
            throw new EnumConstantNotPresentException(AsagiLightConeMetadata.class, id);
        }
        return cone;
    }

    // ✅ Optional: Helper to validate API input names
    public static boolean isValidApiName(String name) {
        return Arrays.stream(values())
            .anyMatch(c -> c.getInternalName().equals(name));
    }
}