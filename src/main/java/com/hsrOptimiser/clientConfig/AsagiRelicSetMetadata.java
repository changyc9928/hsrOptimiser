package com.hsrOptimiser.clientConfig;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AsagiRelicSetMetadata {
    PASSERBY("101", "Passerby"),
    MUSKETEER("102", "Musketeer"),
    KNIGHT("103", "Knight"),
    HUNTER("104", "Hunter"),
    CHAMPION("105", "Champion"),
    GUARD("106", "Guard"),
    FIRESMITH("107", "Firesmith"),
    GENIUS("108", "Genius"),
    BAND("109", "Band"),
    EAGLE("110", "Eagle"),
    THIEF("111", "Thief"),
    WASTELANDER("112", "Wastelander"),
    DISCIPLE("113", "Disciple"),
    MESSENGER("114", "Messenger"),
    GRAND_DUKE("115", "GrandDuke"),
    PRISONER("116", "Prisoner"),
    PIONEERS("117", "Pioneers"),
    WATCHMAKERS("118", "Watchmakers"),
    IRON("119", "Iron"),
    WIND_SOARING("120", "WindSoaring"),
    ORDEAL("121", "Ordeal"),
    SCHOLAR("122", "Scholar"),
    HEROS("123", "Heros"),
    POETS("124", "Poets"),
    WARRIOR("125", "Warrior"),
    CAPTAIN("126", "Captain"),
    DELIVERER("127", "Deliverer"),
    RECLUSE("128", "Recluse"),
    MAGICAL_GIRLS("129", "MagicalGirls"),
    DIVINERS("130", "Diviners"),
    NAVIGATOR("131", "Navigator"),
    MASTER_SMITH("132", "MasterSmith"),

    SPACE_STATION("301", "SpaceStation"),
    FLEET_OF_AGELESS("302", "FleetOfAgeless"),
    PAN_GALACTIC("303", "PanGalactic"),
    BELOBOG("304", "Belobog"),
    CELESTIAL("305", "Celestial"),
    SALSOTTO("306", "Salsotto"),
    TALIA_KINGDOM("307", "TaliaKingdom"),
    VONWACQ("308", "Vonwacq"),
    RUTILANT("309", "Rutilant"),
    BROKEN_KEEL("310", "BrokenKeel"),
    GLAMOTH("311", "Glamoth"),
    PENACONY("312", "Penacony"),
    SIGONIA("313", "Sigonia"),
    IZUMO("314", "Izumo"),
    DURAN("315", "Duran"),
    FORGE("316", "Forge"),
    LUSHAKAS("317", "Lushakas"),
    BANANAMUSEMENT("318", "Bananamusement"),
    DEMESNE("319", "Demesne"),
    GIANT_TREE("320", "GiantTree"),
    ARCADIA("321", "Arcadia"),
    BEACON("322", "Beacon"),
    AMPHOREUS("323", "Amphoreus"),
    TENGOKU("324", "Tengoku"),
    PUNKLORDE("325", "Punklorde"),
    CONVERGING_STARS("326", "ConvergingStars"),
    ANCHORAGE("327", "Anchorage"),
    SCIENCES_INSTITUTE("328", "SciencesInstitute");

    // Cache maps for fast O(1) lookups
    private static final Map<String, AsagiRelicSetMetadata> BY_SET_ID = Arrays.stream(values())
        .collect(Collectors.toMap(AsagiRelicSetMetadata::getSetId, c -> c));
    private final String setId;
    private final String literalName;

    public static AsagiRelicSetMetadata fromId(String setId) {
        return BY_SET_ID.get(setId);
    }
}
