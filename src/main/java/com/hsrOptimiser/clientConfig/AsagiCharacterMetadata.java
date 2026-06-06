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
    MARCH_7TH("March7th", "1001", 4, "preservation", 101, "Ice", 110, 0),
    DAN_HENG("DanHeng", "1002", 4, "the hunt", 110, "Wind", 100, 0),
    HIMEKO("Himeko", "1003", 5, "erudition", 96, "Fire", 100, 0),
    WELT_OLD("WeltOld", "1004", 5, "nihility", 102, "Imaginary", 100, 0),
    WELT("Welt", "1004", 5, "nihility", 102, "Imaginary", 100, 0),
    KAFKA_OLD("KafkaOld", "1005", 5, "nihility", 100, "Lightning", 100, 0),
    KAFKA("Kafka", "1005", 5, "nihility", 100, "Lightning", 100, 0),
    SILVER_WOLF_OLD("SilverWolfOld", "1006", 5, "nihility", 107, "Quantum", 120, 0),
    SILVER_WOLF("SilverWolf", "1006", 5, "nihility", 107, "Quantum", 120, 0),
    ARLAN("Arlan", "1008", 4, "destruction", 102, "Lightning", 100, 0),
    ASTA("Asta", "1009", 4, "harmony", 106, "Fire", 100, 0),
    HERTA("Herta", "1013", 4, "erudition", 100, "Ice", 100, 0),
    SABER("Saber", "1014", 5, "destruction", 101, "Wind", 200, 0),
    ARCHER("Archer", "1015", 5, "the hunt", 105, "Quantum", 100, 0),

    BRONYA("Bronya", "1101", 5, "harmony", 99, "Wind", 150, 0),
    SEELE("Seele", "1102", 5, "the hunt", 115, "Quantum", 100, 0),
    SERVAL("Serval", "1103", 4, "erudition", 104, "Lightning", 100, 0),
    GEPARD("Gepard", "1104", 5, "preservation", 92, "Ice", 100, 0),
    NATASHA("Natasha", "1105", 4, "abundance", 98, "Physical", 200, 0),
    PELA("Pela", "1106", 4, "nihility", 105, "Ice", 110, 0),
    CLARA("Clara", "1107", 5, "destruction", 90, "Physical", 100, 0),
    SAMPO("Sampo", "1108", 4, "nihility", 102, "Wind", 100, 0),
    HOOK("Hook", "1109", 4, "destruction", 94, "Fire", 100, 0),
    LYNX("Lynx", "1110", 4, "abundance", 100, "Quantum", 200, 0),
    LUKA("Luka", "1111", 4, "nihility", 103, "Physical", 100, 0),
    TOPAZ("Topaz", "1112", 5, "the hunt", 110, "Fire", 100, 0),

    QINGQUE("Qingque", "1201", 4, "erudition", 98, "Quantum", 100, 0),
    TINGYUN("Tingyun", "1202", 4, "harmony", 112, "Lightning", 150, 0),
    LUOCHA("Luocha", "1203", 5, "abundance", 101, "Imaginary", 200, 0),
    JING_YUAN("JingYuan", "1204", 5, "erudition", 99, "Lightning", 100, 0),
    BLADE_OLD("BladeOld", "1205", 5, "destruction", 97, "Wind", 170, 0),
    BLADE("Blade", "1205", 5, "destruction", 97, "Wind", 170, 0),
    SUSHANG("Sushang", "1206", 4, "the hunt", 107, "Physical", 100, 0),
    YUKONG("Yukong", "1207", 4, "harmony", 107, "Imaginary", 125, 0),
    FU_XUAN("FuXuan", "1208", 5, "preservation", 100, "Quantum", 200, 0),
    YANQING("Yanqing", "1209", 5, "the hunt", 109, "Ice", 100, 0),
    GUINAIFEN("Guinaifen", "1210", 4, "nihility", 106, "Fire", 100, 0),
    BAILU("Bailu", "1211", 5, "abundance", 98, "Lightning", 200, 0),
    JINGLIU_OLD("JingliuOld", "1212", 5, "destruction", 96, "Ice", 170, 9),
    JINGLIU("Jingliu", "1212", 5, "destruction", 96, "Ice", 170, 9),
    IMBIBITOR_LUNAE("ImbibitorLunae", "1213", 5, "destruction", 102, "Imaginary", 100, 0),
    XUEYI("Xueyi", "1214", 4, "destruction", 103, "Quantum", 100, 0),
    HANYA("Hanya", "1215", 4, "harmony", 110, "Physical", 150, 9),
    HUOHUO("Huohuo", "1217", 5, "abundance", 98, "Wind", 200, 5),
    JIAOQIU("Jiaoqiu", "1218", 5, "nihility", 98, "Fire", 100, 5),
    FEIXIAO("Feixiao", "1220", 5, "the hunt", 112, "Wind", 100, 0),
    YUNLI("Yunli", "1221", 5, "destruction", 94, "Physical", 150, 0),
    LINGSHA("Lingsha", "1222", 5, "abundance", 98, "Fire", 200, 0),
    MOZE("Moze", "1223", 4, "the hunt", 111, "Lightning", 100, 0),
    MARCH_7TH_IMAGINARY("March7thImaginary", "1224", 4, "the hunt", 102, "Imaginary", 100, 0),
    FUGUE("Fugue", "1225", 5, "nihility", 102, "Fire", 100, 14),

    GALLAGHER("Gallagher", "1301", 4, "abundance", 98, "Fire", 200, 0),
    ARGENTI("Argenti", "1302", 5, "erudition", 103, "Physical", 100, 0),
    RUAN_MEI("RuanMei", "1303", 5, "harmony", 104, "Ice", 150, 5),
    AVENTURINE("Aventurine", "1304", 5, "preservation", 106, "Imaginary", 200, 0),
    DR_RATIO("DrRatio", "1305", 5, "the hunt", 103, "Imaginary", 100, 0),
    SPARKLE_OLD("SparkleOld", "1306", 5, "harmony", 107, "Quantum", 150, 0),
    SPARKLE("Sparkle", "1306", 5, "harmony", 101, "Quantum", 150, 0),
    BLACK_SWAN_OLD("BlackSwanOld", "1307", 5, "nihility", 120, "Wind", 0, 0),
    BLACK_SWAN("BlackSwan", "1307", 5, "nihility", 102, "Wind", 100, 0),
    ACHERON("Acheron", "1308", 5, "nihility", 101, "Lightning", 100, 0),
    ROBIN("Robin", "1309", 5, "harmony", 102, "Physical", 150, 5),
    FIREFLY_OLD("FireflyOld", "1310", 5, "destruction", 104, "Fire", 150, 5),
    FIREFLY("Firefly", "1310", 5, "destruction", 104, "Fire", 150, 5),
    MISHA("Misha", "1312", 4, "destruction", 96, "Ice", 100, 0),
    SUNDAY("Sunday", "1313", 5, "harmony", 96, "Imaginary", 201, 0),
    JADE("Jade", "1314", 5, "erudition", 103, "Quantum", 100, 0),
    BOOTHILL("Boothill", "1315", 5, "the hunt", 107, "Physical", 100, 0),
    RAPPA("Rappa", "1317", 5, "erudition", 96, "Imaginary", 100, 9),
    DAHLIA("Dahlia", "1321", 5, "nihility", 96, "Fire", 100, 5),

    THE_HERTA("TheHerta", "1401", 5, "erudition", 99, "Ice", 100, 5),
    AGLAEA("Aglaea", "1402", 5, "remembrance", 102, "Lightning", 200, 0),
    TRIBBIE("Tribbie", "1403", 5, "harmony", 96, "Quantum", 201, 0),
    MYDEI("Mydei", "1404", 5, "destruction", 95, "Imaginary", 200, 5),
    ANAXA("Anaxa", "1405", 5, "erudition", 97, "Wind", 100, 0),
    CIPHER("Cipher", "1406", 5, "nihility", 106, "Quantum", 100, 14),
    CASTORICE("Castorice", "1407", 5, "remembrance", 95, "Quantum", 200, 0),
    // Phainon 1408
    HYACINE("Hyacine", "1409", 5, "remembrance", 110, "Wind", 120, 14),
    HYSILENS("Hysilens", "1410", 5, "nihility", 102, "Physical", 100, 14),
    CERYDRA("Cerydra", "1412", 5, "harmony", 99, "Wind", 100, 0),
    EVERNIGHT("Evernight", "1413", 5, "remembrance", 99, "Ice", 120, 0),
    PERMANSOR_TERRAE("PermansorTerrae", "1414", 5, "preservation", 97, "Physical", 100, 0),
    CYRENE("Cyrene", "1415", 5, "remembrance", 101, "Ice", 120, 9),

    SPARXIE("Sparxie", "1501", 5, "elation", 107, "Fire", 200, 0),
    YAO_GUANG("YaoGuang", "1502", 5, "elation", 101, "Physical", 150, 9),
    ASHVEIL("Ashveil", "1504", 5, "the hunt", 106, "Lightning", 100, 0),
    EVANESCIA("Evanescia", "1505", 5, "elation", 104, "Physical", 200, 5),
    SILVER_WOLF_LV999("SilverWolfLv999", "1506", 5, "elation", 110, "Quantum", 200, 9),
    // Mortenax Blade 1507

    TRAILBLAZER_PHYSICAL_CAELUS("TrailblazerPhysical_Caelus", "8001", 5, "destruction", 100,
        "Physical", 100, 0),
    TRAILBLAZER_PHYSICAL_STELLE("TrailblazerPhysical_Stelle", "8002", 5, "destruction", 100,
        "Physical", 100, 0),
    TRAILBLAZER_FIRE_CAELUS("TrailblazerFire_Caelus", "8003", 5, "preservation", 95, "Fire", 110, 0),
    TRAILBLAZER_FIRE_STELLE("TrailblazerFire_Stelle", "8004", 5, "preservation", 95, "Fire", 110, 0),
    TRAILBLAZER_IMAGINARY_CAELUS("TrailblazerImaginary_Caelus", "8005", 5, "harmony", 105,
        "Imaginary", 100, 0),
    TRAILBLAZER_IMAGINARY_STELLE("TrailblazerImaginary_Stelle", "8006", 5, "harmony", 105,
        "Imaginary", 100, 0),
    TRAILBLAZER_ICE_CAELUS("TrailblazerIce_Caelus", "8007", 5, "remembrance", 103, "Ice", 200, 0),
    TRAILBLAZER_ICE_STELLE("TrailblazerIce_Stelle", "8008", 5, "remembrance", 103, "Ice", 200, 0),
    TRAILBLAZER_ELATION_CAELUS("TrailblazerElation_Caelus", "8009", 5, "elation", 106, "Lightning", 100, 0),
    TRAILBLAZER_ELATION_STELLE("TrailblazerElation_Stelle", "8010", 5, "elation", 106, "Lightning", 100, 0),

    AHHA("Ahha", "0", 0, "unknown", 0, "unknown", 0, 0),
    YAO_GUANG2("YaoGuang2", "0", 0, "unknown", 0, "unknown", 0, 0),
    DEFAULT_HEALER("DefaultHealer", "0", 0, "unknown", 0, "unknown", 0, 0),
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
        .collect(Collectors.toMap(AsagiCharacterMetadata::getRawDisplayName, c -> c));
    private static final Map<AsagiCharacterMetadata, AsagiCharacterMetadata> OLD_VARIANTS = new HashMap<>();

    static {
        OLD_VARIANTS.put(SILVER_WOLF, SILVER_WOLF_OLD);
        OLD_VARIANTS.put(KAFKA, KAFKA_OLD);
        OLD_VARIANTS.put(FIREFLY, FIREFLY_OLD);
        OLD_VARIANTS.put(JINGLIU, JINGLIU_OLD);
        OLD_VARIANTS.put(BLADE, BLADE_OLD);
//        OLD_VARIANTS.put(BLACK_SWAN, BLACK_SWAN_OLD);
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

    private String getRawDisplayName() {
        return displayName;
    }

    public String getDisplayName() {
        return this.displayName.split("_")[0];
    }
}
