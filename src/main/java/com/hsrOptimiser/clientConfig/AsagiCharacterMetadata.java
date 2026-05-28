package com.hsrOptimiser.clientConfig;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AsagiCharacterMetadata {
    AHHA("Ahha", "0", 0, "unknown", 0, "unknown", 0, 0),
    SPARXIE("Sparxie", "1501", 5, "elation", 160, "Fire", 0, 0),
    YAO_GUANG("YaoGuang", "1502", 5, "elation", 180, "Physical", 150, 9),
    YAO_GUANG2("YaoGuang2", "0", 0, "unknown", 0, "unknown", 0, 0),
    SILVER_WOLF_LV999("SilverWolfLv999", "1506", 5, "elation", 60, "Quantum", 0, 0),
    TRAILBLAZER_ELATION_CAELUS("TrailblazerElation_Caelus", "8009", 5, "elation", 160, "Lightning", 100, 0),
    TRAILBLAZER_ELATION_STELLE("TrailblazerElation_Stelle", "8010", 5, "elation", 160, "Lightning", 100, 0),
    EVANESCIA("Evanescia", "1505", 5, "elation", 480, "Physical", 200, 5),
    JINGLIU("Jingliu", "1212", 5, "destruction", 140, "Ice", 0, 0),
    BLADE("Blade", "1205", 5, "destruction", 130, "Wind", 0, 0),
    JINGLIU_OLD("JingliuOld", "1212", 5, "destruction", 140, "Ice", 0, 0),
    IMBIBITOR_LUNAE("ImbibitorLunae", "1213", 5, "destruction", 140, "Imaginary", 0, 0),
    BLADE_OLD("BladeOld", "1205", 5, "destruction", 130, "Wind", 0, 0),
    CLARA("Clara", "1107", 5, "destruction", 110, "Physical", 0, 0),
    HOOK("Hook", "1109", 4, "destruction", 120, "Fire", 0, 0),
    ARLAN("Arlan", "1008", 4, "destruction", 110, "Lightning", 0, 0),
    TRAILBLAZER_PHYSICAL_CAELUS("TrailblazerPhysical_Caelus", "8001", 5, "destruction", 120,
        "Physical", 0, 0),
    TRAILBLAZER_PHYSICAL_STELLE("TrailblazerPhysical_Stelle", "8002", 5, "destruction", 120,
        "Physical", 0, 0),
    XUEYI("Xueyi", "1214", 4, "destruction", 120, "Quantum", 0, 0),
    MISHA("Misha", "1312", 4, "destruction", 100, "Ice", 0, 0),
    FIREFLY("Firefly", "1310", 5, "destruction", 240, "Fire", 150, 5),
    YUNLI("Yunli", "1221", 5, "destruction", 240, "Physical", 0, 0),
    MYDEI("Mydei", "1404", 5, "destruction", 160, "Imaginary", 0, 0),
    SABER("Saber", "1014", 5, "destruction", 360, "Wind", 0, 0),
    FIREFLY_OLD("FireflyOld", "1310", 5, "destruction", 240, "Fire", 150, 5),
    SEELE("Seele", "1102", 5, "the hunt", 120, "Quantum", 0, 0),
    TOPAZ("Topaz", "1112", 5, "the hunt", 130, "Fire", 0, 0),
    YANQING("Yanqing", "1209", 5, "the hunt", 140, "Ice", 0, 0),
    SUSHANG("Sushang", "1206", 4, "the hunt", 120, "Physical", 0, 0),
    DAN_HENG("DanHeng", "1002", 4, "the hunt", 100, "Wind", 0, 0),
    DR_RATIO("DrRatio", "1305", 5, "the hunt", 140, "Imaginary", 0, 0),
    BOOTHILL("Boothill", "1315", 5, "the hunt", 115, "Physical", 0, 0),
    MARCH_7TH_IMAGINARY("March7thImaginary", "1224", 4, "the hunt", 110, "Imaginary", 0, 0),
    FEIXIAO("Feixiao", "1220", 5, "the hunt", 12, "Wind", 0, 0),
    MOZE("Moze", "1223", 4, "the hunt", 120, "Lightning", 0, 0),
    ARCHER("Archer", "1015", 5, "the hunt", 220, "Quantum", 0, 0),
    ASHVEIL("Ashveil", "1504", 5, "the hunt", 150, "Lightning", 0, 0),
    JING_YUAN("JingYuan", "1204", 5, "erudition", 130, "Lightning", 0, 0),
    HIMEKO("Himeko", "1003", 5, "erudition", 120, "Fire", 0, 0),
    SERVAL("Serval", "1103", 4, "erudition", 100, "Lightning", 0, 0),
    HERTA("Herta", "1013", 4, "erudition", 110, "Ice", 0, 0),
    QINGQUE("Qingque", "1201", 4, "erudition", 140, "Quantum", 0, 0),
    ARGENTI("Argenti", "1302", 5, "erudition", 180, "Physical", 0, 0),
    JADE("Jade", "1314", 5, "erudition", 140, "Quantum", 0, 0),
    RAPPA("Rappa", "1317", 5, "erudition", 140, "Imaginary", 0, 0),
    THE_HERTA("TheHerta", "1401", 5, "erudition", 220, "Ice", 0, 0),
    ANAXA("Anaxa", "1405", 5, "erudition", 140, "Wind", 0, 0),
    KAFKA("Kafka", "1005", 5, "nihility", 120, "Lightning", 0, 0),
    PELA("Pela", "1106", 4, "nihility", 110, "Ice", 0, 0),
    WELT("Welt", "1004", 5, "nihility", 120, "Imaginary", 0, 0),
    SAMPO("Sampo", "1108", 4, "nihility", 120, "Wind", 0, 0),
    LUKA("Luka", "1111", 4, "nihility", 130, "Physical", 0, 0),
    SILVER_WOLF("SilverWolf", "1006", 5, "nihility", 110, "Quantum", 0, 0),
    GUINAIFEN("Guinaifen", "1210", 4, "nihility", 120, "Fire", 0, 0),
    BLACK_SWAN("BlackSwan", "1307", 5, "nihility", 120, "Wind", 0, 0),
    ACHERON("Acheron", "1308", 5, "nihility", 9, "Lightning", 0, 0),
    JIAOQIU("Jiaoqiu", "1218", 5, "nihility", 100, "Fire", 0, 0),
    FUGUE("Fugue", "1225", 5, "nihility", 130, "Fire", 100, 14),
    CIPHER("Cipher", "1406", 5, "nihility", 130, "Quantum", 0, 0),
    SILVER_WOLF_OLD("SilverWolfOld", "1006", 5, "nihility", 110, "Quantum", 0, 0),
    KAFKA_OLD("KafkaOld", "1005", 5, "nihility", 120, "Lightning", 0, 0),
    HYSILENS("Hysilens", "1410", 5, "nihility", 110, "Physical", 0, 0),
    DAHLIA("Dahlia", "1321", 5, "nihility", 130, "Fire", 0, 0),
    BLACK_SWAN_OLD("BlackSwanOld", "1307", 5, "nihility", 120, "Wind", 0, 0),
    WELT_OLD("WeltOld", "1004", 5, "nihility", 120, "Imaginary", 0, 0),
    ROBIN("Robin", "1309", 5, "harmony", 160, "Physical", 0, 0),
    RUAN_MEI("RuanMei", "1303", 5, "harmony", 130, "Ice", 150, 5),
    TINGYUN("Tingyun", "1202", 4, "harmony", 130, "Lightning", 0, 0),
    ASTA("Asta", "1009", 4, "harmony", 120, "Fire", 0, 0),
    BRONYA("Bronya", "1101", 5, "harmony", 120, "Wind", 0, 0),
    YUKONG("Yukong", "1207", 4, "harmony", 130, "Imaginary", 0, 0),
    HANYA("Hanya", "1215", 4, "harmony", 140, "Physical", 0, 0),
    SPARKLE("Sparkle", "1306", 5, "harmony", 110, "Quantum", 0, 0),
    TRAILBLAZER_IMAGINARY_CAELUS("TrailblazerImaginary_Caelus", "8005", 5, "harmony", 140,
        "Imaginary", 0, 0),
    TRAILBLAZER_IMAGINARY_STELLE("TrailblazerImaginary_Stelle", "8006", 5, "harmony", 140,
        "Imaginary", 0, 0),
    SUNDAY("Sunday", "1313", 5, "harmony", 130, "Imaginary", 0, 0),
    TRIBBIE("Tribbie", "1403", 5, "harmony", 120, "Quantum", 0, 0),
    CERYDRA("Cerydra", "1412", 5, "harmony", 130, "Wind", 0, 0),
    SPARKLE_OLD("SparkleOld", "1306", 5, "harmony", 110, "Quantum", 0, 0),
    DEFAULT_HEALER("DefaultHealer", "0", 0, "unknown", 0, "unknown", 0, 0),
    LUOCHA("Luocha", "1203", 5, "abundance", 100, "Imaginary", 0, 0),
    BAILU("Bailu", "1211", 5, "abundance", 100, "Lightning", 0, 0),
    NATASHA("Natasha", "1105", 4, "abundance", 90, "Physical", 0, 0),
    LYNX("Lynx", "1110", 4, "abundance", 100, "Quantum", 0, 0),
    HUOHUO("Huohuo", "1217", 5, "abundance", 140, "Wind", 200, 5),
    GALLAGHER("Gallagher", "1301", 4, "abundance", 110, "Fire", 150, 0),
    LINGSHA("Lingsha", "1222", 5, "abundance", 110, "Fire", 0, 0),
    GEPARD("Gepard", "1104", 5, "preservation", 100, "Ice", 0, 0),
    FU_XUAN("FuXuan", "1208", 5, "preservation", 135, "Quantum", 0, 0),
    MARCH_7TH("March7th", "1001", 4, "preservation", 120, "Ice", 0, 0),
    TRAILBLAZER_FIRE_CAELUS("TrailblazerFire_Caelus", "8003", 5, "preservation", 120, "Fire", 0, 0),
    TRAILBLAZER_FIRE_STELLE("TrailblazerFire_Stelle", "8004", 5, "preservation", 120, "Fire", 0, 0),
    AVENTURINE("Aventurine", "1304", 5, "preservation", 110, "Imaginary", 0, 0),
    PERMANSOR_TERRAE("PermansorTerrae", "1414", 5, "preservation", 135, "Physical", 0, 0),
    TRAILBLAZER_ICE_CAELUS("TrailblazerIce_Caelus", "8007", 5, "remembrance", 160, "Ice", 0, 0),
    TRAILBLAZER_ICE_STELLE("TrailblazerIce_Stelle", "8008", 5, "remembrance", 160, "Ice", 0, 0),
    AGLAEA("Aglaea", "1402", 5, "remembrance", 350, "Lightning", 0, 0),
    CASTORICE("Castorice", "1407", 5, "remembrance", 0, "Quantum", 0, 0),
    HYACINE("Hyacine", "1409", 5, "remembrance", 140, "Wind", 0, 0),
    EVERNIGHT("Evernight", "1413", 5, "remembrance", 240, "Ice", 0, 0),
    CYRENE("Cyrene", "1415", 5, "remembrance", 24, "Ice", 0, 0),
    MEM("Mem", "0", 0, "unknown", 0, "unknown", 0, 0),
    GARMENTMAKER("Garmentmaker", "0", 0, "unknown", 0, "unknown", 0, 0),
    DRAGON("Dragon", "0", 0, "unknown", 0, "unknown", 0, 0),
    LITTLE_ICA("LittleIca", "0", 0, "unknown", 0, "unknown", 0, 0),
    EVEY("Evey", "0", 0, "unknown", 0, "unknown", 0, 0),
    CYRENE_SPRIT("CyreneSprit", "0", 0, "unknown", 0, "unknown", 0, 0),
    STAGE_BUFF("StageBuff", "0", 0, "unknown", 0, "unknown", 0, 0);

    private final String displayName;
    private final String id;
    private final int rarity;
    private final String path;
    private final int maxSp;
    private final String attackType;
    private final int skillPriority;
    private final int addSpeed;

    // Pre-computed maps for O(1) lookups
    private static final Map<String, AsagiCharacterMetadata> ID_MAP = Arrays.stream(values())
        .filter(c -> !c.id.equals("0") && !c.name().endsWith("_OLD"))
        .collect(Collectors.toMap(AsagiCharacterMetadata::getId, c -> c));
    private static final Map<String, AsagiCharacterMetadata> NAME_MAP = Arrays.stream(values())
        .collect(Collectors.toMap(AsagiCharacterMetadata::getDisplayName, c -> c));
    private static final Map<AsagiCharacterMetadata, AsagiCharacterMetadata> OLD_VARIANTS = new HashMap<>();

    static {
        OLD_VARIANTS.put(SILVER_WOLF, SILVER_WOLF_OLD);
        OLD_VARIANTS.put(KAFKA, KAFKA_OLD);
        OLD_VARIANTS.put(FIREFLY, FIREFLY_OLD);
        OLD_VARIANTS.put(JINGLIU, JINGLIU_OLD);
        OLD_VARIANTS.put(BLADE, BLADE_OLD);
        OLD_VARIANTS.put(BLACK_SWAN, BLACK_SWAN_OLD);
        OLD_VARIANTS.put(WELT, WELT_OLD);
        OLD_VARIANTS.put(SPARKLE, SPARKLE_OLD);
    }

    public static AsagiCharacterMetadata getInfoById(String id, int abilityVersion) {
        AsagiCharacterMetadata baseChar = ID_MAP.get(id);
        if (baseChar == null) {
            throw new EnumConstantNotPresentException(AsagiCharacterMetadata.class, id);
        }

        if (abilityVersion == 0 && OLD_VARIANTS.containsKey(baseChar)) {
            return OLD_VARIANTS.get(baseChar);
        }

        return baseChar;
    }
}
