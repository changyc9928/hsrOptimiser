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
    MESHING_COGS("20012", "MeshingCogs", 3, 2),
    PASSKEY("20013", "Passkey", 3, 0),
    ADVERSARIAL("20014", "Adversarial", 3, 1),
    MULTIPLICATION("20015", "Multiplication", 3, 2),
    MUTUAL_DEMISE("20016", "MutualDemise", 3, 1),
    PIONEERING("20017", "Pioneering", 3, 1),
    HIDDEN_SHADOW("20018", "HiddenShadow", 3, 1),
    MEDIATION("20019", "Mediation", 3, 1),
    SAGACITY("20020", "Sagacity", 3, 1),
    SHADOWBURN("20021", "Shadowburn", 3, 2),
    REMINISCENCE("20022", "Reminiscence", 3, 2),
    SNEERING("20023", "Sneering", 3, 1),
    LINGERING_TEAR("20024", "LingeringTear", 3, 1),

    // ==================== 4-Star Light Cones ====================
    POST_OP("21000", "PostOp", 4, 0),
    GOOD_NIGHT_SLEEP_WELL("21001", "GoodNightSleepWell", 4, 0),
    MY_NEW_LIFE("21002", "MyNewLife", 4, 0),
    ONLY_SILENCE_REMAINS("21003", "OnlySilenceRemains", 4, 0),
    MEMORIES_PAST("21004", "MemoriesPast", 4, 0),
    MOLES_WELCOME_YOU("21005", "MolesWelcomeYou", 4, 0),
    BIRTH_OF_THE_SELF("21006", "BirthOftheSelf", 4, 0),
    SHARED_FEELING("21007", "SharedFeeling", 4, 0),
    EYES_PREY("21008", "EyesPrey", 4, 0),
    LANDAUS_CHOICE("21009", "LandausChoice", 4, 0),
    SWORDPLAY("21010", "Swordplay", 4, 0),
    PLANETARY_RENDEZVOUS("21011", "PlanetaryRendezvous", 4, 0),
    SECRET_VOW("21012", "SecretVow", 4, 0),
    MAKE_WORLD_CLAMOR("21013", "MakeWorldClamor", 4, 0),
    PERFECT_TIMING("21014", "PerfectTiming", 4, 0),
    RESOLUTION_SHINES("21015", "ResolutionShines", 4, 0),
    UNIVERSAL_MARKET("21016", "UniversalMarket", 4, 0),
    SUBSCRIBE_MORE("21017", "SubscribeMore", 4, 0),
    DANCE_DANCE_DANCE("21018", "DanceDanceDance", 4, 0),
    UNDER_THE_BLUE_SKY("21019", "UndertheBlueSky", 4, 0),
    GENIUSES_REPOSE("21020", "GeniusesRepose", 4, 0),
    QUID_PRO_QUO("21021", "QuidProQuo", 4, 2),
    FERMATA("21022", "Fermata", 4, 2),
    WE_ARE_WILDFIRE("21023", "WeAreWildfire", 4, 2),
    RIVER_FLOWS_SPRING("21024", "RiverFlowsSpring", 4, 2),
    PAST_FUTURE("21025", "PastFuture", 4, 2),
    WOOF_WALK_TIME("21026", "WoofWalkTime", 4, 2),
    SERIOUSNESS_BREAKFAST("21027", "SeriousnessBreakfast", 4, 2),
    WARMTH_NIGHTS("21028", "WarmthNights", 4, 2),
    WE_WILL_MEET_AGAIN("21029", "WeWillMeetAgain", 4, 2),
    THIS_IS_ME("21030", "ThisIsMe", 4, 2),
    RETURN_DARKNESS("21031", "ReturnDarkness", 4, 2),
    CARVE_MOON_WEAVE_CLOUDS("21032", "CarveMoonWeaveClouds", 4, 2),
    NOWHERE_TO_RUN("21033", "NowhereToRun", 4, 2),
    PEACEFUL_DAY("21034", "PeacefulDay", 4, 2),
    WHAT_IS_REAL("21035", "WhatIsReal", 4, 2),
    DREAMVILLE("21036", "Dreamville", 4, 2),
    FINAL_VICTOR("21037", "FinalVictor", 4, 2),
    FLAMES_AFAR("21038", "FlamesAfar", 4, 2),
    THREADS_FOREWOVEN("21039", "ThreadsForewoven", 4, 2),
    DAY_COSMOS_FELL("21040", "DayCosmosFell", 4, 2),
    SHOWTIME("21041", "Showtime", 4, 2),
    INDELIBLE_PROMISE("21042", "IndeliblePromise", 4, 2),
    CONCERT_FOR_TWO("21043", "ConcertForTwo", 4, 2),
    BOUNDLESS_CHOREO("21044", "BoundlessChoreo", 4, 2),
    AFTER_CHARMONY_FALL("21045", "AfterCharmonyFall", 4, 0),
    POISED_TO_BLOOM("21046", "PoisedToBloom", 4, 2),
    SHADOWED_BY_NIGHT("21047", "ShadowedByNight", 4, 0),
    DREAMS_MONTAGE("21048", "DreamsMontage", 4, 0),
    VICTORY_BLINK("21050", "VictoryBlink", 4, 2),
    GENIUSES_GREETINGS("21051", "GeniusesGreetings", 4, 1),
    SWEAT_NOW_CRY_LESS("21052", "SweatNowCryLess", 4, 1),
    JOURNEY_FOREVER_PEACEFUL("21053", "JourneyForeverPeaceful", 0, 0),
    STORY_NEXT_PAGE("21054", "StoryNextPage", 0, 0),
    UNTO_TOMORROWS_MORROW("21055", "UntoTomorrowsMorrow", 0, 0),
    IN_PURSUIT_WIND("21056", "InPursuitWind", 0, 0),
    FLOWER_REMEMBERS("21057", "FlowerRemembers", 0, 0),
    TRAIL_BYGONE_BLOOD("21058", "TrailBygoneBlood", 4, 0),
    DREAM_SCENTED_WHEAT("21060", "DreamScentedWheat", 0, 0),
    HOLIDAY_THERMAE_ESCAPADE("21061", "HolidayThermaeEscapade", 4, 2),
    SEE_YOU_THE_END("21062", "SeeYouTheEnd", 0, 0),
    MUSHY_AHROOMYS_ADVENTURES("21064", "MushyAhroomysAdventures", 4, 2),
    TODAYS_GOOD_LUCK("21065", "TodaysGoodLuck", 4, 1),
    BEFORE_MISSION_STARTS("22000", "BeforeMissionStarts", 4, 2),
    OVER_HERE("22001", "OverHere", 4, 2),
    TOMORROWS_JOURNEY("22002", "TomorrowsJourney", 4, 2),
    NINJA_RECORD("22003", "NinjaRecord", 4, 2),
    GREAT_COSMIC_ENTERPRISE("22004", "GreatCosmicEnterprise", 4, 0),
    FOREVER_VICTUAL("22005", "ForeverVictual", 4, 2),
    TAKE_FLIGHT_TOWARD_PINK_TOMORROW("22006", "TakeFlightTowardPinkTomorrow", 4, 2),
    TOMORROW_TOGETHER("22007", "TomorrowTogether", 4, 1),

    // ==================== 5-Star Light Cones ====================
    NIGHT_MILKY_WAY("23000", "NightMilkyWay", 5, 0),
    IN_THE_NIGHT("23001", "InTheNight", 5, 1),
    SOMETHING_IRREPLACEABLE("23002", "SomethingIrreplaceable", 5, 0),
    BATTLE_ISNT_OVER("23003", "BattleIsntOver", 5, 0),
    NAME_OF_WORLD("23004", "NameOfWorld", 5, 0),
    MOMENT_VICTORY("23005", "MomentVictory", 5, 0),
    ALL_YOU_NEED("23006", "AllYouNeed", 5, 1),
    INCESSANT_RAIN("23007", "IncessantRain", 5, 1),
    ECHOES_OF_COFFIN("23008", "EchoesOfCoffin", 5, 1),
    UNREACHABLE_SIDE("23009", "UnreachableSide", 5, 1),
    BEFORE_DAWN("23010", "BeforeDawn", 5, 1),
    SHUT_HER_EYES("23011", "ShutHerEyes", 5, 1),
    SLEEP_LIKE_DEAD("23012", "SleepLikeDead", 5, 0),
    TIME_WAITS("23013", "TimeWaits", 5, 0),
    MY_OWN_SWORD("23014", "MyOwnSword", 5, 1),
    BRIGHTER_THAN_SUN("23015", "BrighterThanSun", 5, 1),
    WORRISOME_BLISSFUL("23016", "WorrisomeBlissful", 5, 1),
    NIGHT_OF_FRIGHT("23017", "NightOfFright", 5, 1),
    INSTANT_BEFORE_GAZE("23018", "InstantBeforeGaze", 5, 1),
    PAST_SELF_MIRROR("23019", "PastSelfMirror", 5, 1),
    PURE_THOUGHT("23020", "PureThought", 5, 1),
    EARTHLY_ESCAPADE("23021", "EarthlyEscapade", 5, 1),
    REFORGED_REMEMBRANCE("23022", "ReforgedRemembrance", 5, 1),
    INHERENTLY_UNJUST_DESTINY("23023", "InherentlyUnjustDestiny", 5, 1),
    ALONG_PASSING_SHORE("23024", "AlongPassingShore", 5, 1),
    WHEREABOUTS_SHOULD_DREAMS_REST("23025", "WhereaboutsShouldDreamsRest", 5, 1),
    FLOWING_NIGHTGLOW("23026", "FlowingNightglow", 5, 1),
    SAILING_TOWARDS_SECOND_LIFE("23027", "SailingTowardsSecondLife", 5, 1),
    YET_HOPE_IS_PRICELESS("23028", "YetHopeIsPriceless", 5, 1),
    THOSE_MANY_SPRINGS("23029", "ThoseManySprings", 5, 1),
    DANCE_AT_SUNSET("23030", "DanceAtSunset", 5, 1),
    VENTURE_FORTH_TO_HUNT("23031", "VentureForthToHunt", 5, 1),
    SCENT_ALONE_STAYS_TRUE("23032", "ScentAloneStaysTrue", 5, 1),
    NINJUTSU("23033", "Ninjutsu", 5, 1),
    GROUNDED_ASCENT("23034", "GroundedAscent", 5, 1),
    LONG_ROAD_LEADS_HOME("23035", "LongRoadLeadsHome", 5, 1),
    TIME_WOVEN_INTO_GOLD("23036", "TimeWovenIntoGold", 5, 1),
    INTO_UNREACHABLE_VEIL("23037", "IntoUnreachableVeil", 5, 1),
    IF_TIME_WERE_FLOWER("23038", "IfTimeWereFlower", 5, 1),
    FLAME_BLOOD("23039", "FlameBlood", 5, 1),
    MAKE_FAREWELLS_MORE_BEAUTIFUL("23040", "MakeFarewellsMoreBeautiful", 5, 1),
    LIFE_SHOULD_BE_CAST_TO_FLAMES("23041", "LifeShouldBeCastToFlames", 5, 1),
    LONG_MAY_RAINBOWS("23042", "LongMayRainbows", 5, 1),
    LIES_DANCE_BREEZE("23043", "LiesDanceBreeze", 5, 1),
    THUS_BURNS_DAWN("23044", "ThusBurnsDawn", 5, 1),
    THANKLESS_CORONATION("23045", "ThanklessCoronation", 5, 1),
    HELL_WHERE_IDEALS_BURN("23046", "HellWhereIdealsBurn", 5, 1),
    WHY_DOES_OCEAN_SING("23047", "WhyDoesOceanSing", 5, 1),
    EPOCH_ETCHED_IN_THE_GOLDEN_BLOOD("23048", "EpochEtchedInGoldenBlood", 5, 1),
    TO_EVERNIGHTS_STARS("23049", "ToEvernightsStars", 5, 1),
    NEVER_FORGET_HER_FLAME("23050", "NeverForgetHerFlame", 5, 1),
    THOUGH_WORLDS_APART("23051", "ThoughWorldsApart", 5, 1),
    THIS_LOVE_FOREVER("23052", "ThisLoveForever", 5, 1),
    DAZZLED_BY_FLOWERY_WORLD("23053", "DazzledByFloweryWorld", 5, 1),
    WHEN_SHE_DECIDED_TO_SEE("23054", "WhenSheDecidedToSee", 0, 0),
    FINALE_OF_LIE("23056", "FinaleOfLie", 5, 1),
    WELCOME_COSMIC_CITY("23057", "WelcomeCosmicCity", 5, 1),
    UNTIL_FLOWERS_BLOOM_AGAIN("23058", "UntilFlowersBloomAgain", 5, 1),
    REFORGED_IN_HELLFIRE("23059", "ReforgedInHellfire", 5, 1),
    FALL_OF_AEON("24000", "FallOfAeon", 5, 2),
    CRUISING_STELLAR_SEA("24001", "CruisingStellarSea", 5, 2),
    TEXTURE_MEMORIES("24002", "TextureMemories", 5, 2),
    SOLITARY_HEALING("24003", "SolitaryHealing", 5, 2),
    ETERNAL_CALCULUS("24004", "EternalCalculus", 5, 2),
    MEMORYS_CURTAIN_NEVER_FALLS("24005", "MemorysCurtainNeverFalls", 5, 2),
    ELATION_BRIMMING_WITH_BLESSINGS("24006", "ElationBrimmingWithBlessings", 5, 2);


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