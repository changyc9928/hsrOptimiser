package com.hsrOptimiser.clientConfig;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AsagiRelicSetMetadata {
    // --- Relic / Cavern Sets (Usually 100+ series) ---
    PASSERBY("101", "Passerby"),
    MUSKETEER("102", "Musketeer"),
    KNIGHT("103", "Knight"),
    HUNTER("104", "Hunter"),
    CHAMPION("105", "Champion"),
    EAGLE("106", "Eagle"),
    BAND("107", "Band"),
    FIRESMITH("108", "Firesmith"),
    GENIUS("109", "Genius"),
    THIEF("110", "Thief"),
    WASTELANDER("111", "Wastelander"),
    GUARD("112", "Guard"),
    DISCIPLE("113", "Disciple"),
    MESSENGER("114", "Messenger"),
    GRAND_DUKE("115", "GrandDuke"),
    PRISONER("116", "Prisoner"),
    PIONEERS("117", "Pioneers"),
    WATCHMAKERS("118", "Watchmakers"),
    IRON("119", "Iron"),
    WIND_SOARING("120", "WindSoaring"),
    SCHOLAR("121", "Scholar"),
    ORDEAL("122", "Ordeal"),
    HEROS("123", "Heros"),
    POETS("124", "Poets"),
    WARRIOR("125", "Warrior"),
    CAPTAIN("126", "Captain"),
    DELIVERER("127", "Deliverer"),
    RECLUSE("128", "Recluse"),
    MAGICAL_GIRLS("129", "MagicalGirls"),
    DIVINERS("130", "Diviners"),

    // --- Ornament / Planar Sets (Usually 300+ series) ---
    SPACE_STATION("301", "SpaceStation"),
    FLEET_OF_AGELESS("302", "FleetOfAgeless"),
    BELOBOG("303", "Belobog"),
    CELESTIAL("304", "Celestial"),
    SALSOTTO("305", "Salsotto"),
    PAN_GALACTIC("306", "PanGalactic"),
    TALIA_KINGDOM("307", "TaliaKingdom"),
    VONWACQ("308", "Vonwacq"),
    RUTILANT("309", "Rutilant"),
    BROKEN_KEEL("310", "BrokenKeel"),
    GLAMOTH("311", "Glamoth"),
    PENACONY("312", "Penacony"),
    IZUMO("313", "Izumo"),
    SIGONIA("314", "Sigonia"),
    DURAN("315", "Duran"),
    FORGE("316", "Forge"),
    BANANAMUSEMENT("317", "Bananamusement"),
    LUSHAKAS("318", "Lushakas"),
    DEMESNE("319", "Demesne"),
    GIANT_TREE("320", "GiantTree"),
    BEACON("321", "Beacon"),
    ARCADIA("322", "Arcadia"),
    AMPHOREUS("323", "Amphoreus"),
    TENGOKU("324", "Tengoku"),
    PUNKLORDE("325", "Punklorde"),
    CONVERGING_STARS("326", "ConvergingStars");

    // Cache maps for fast O(1) lookups
    private static final Map<String, AsagiRelicSetMetadata> BY_SET_ID = Arrays.stream(values())
        .collect(Collectors.toMap(AsagiRelicSetMetadata::getSetId, c -> c));
    private final String setId;
    private final String literalName;

    /**
     * Retrieve the enum using the numeric set_id key from the JSON.
     *
     * @param setId The numeric string id (e.g., "318")
     * @return The matching RelicSet, or null if not found.
     */
    public static AsagiRelicSetMetadata fromId(String setId) {
        return BY_SET_ID.get(setId);
    }
}