package com.hsrOptimiser.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.hsrOptimiser.DTO.hsrScanner.HSRCharacter;
import com.hsrOptimiser.DTO.hsrScanner.ScannedData;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TeamServiceTest {

    private static final String USER = "u1";

    private Memory memory;
    private TeamService teamService;

    @BeforeEach
    void setUp() {
        memory = new MemoryImpl();
        teamService = new TeamServiceImpl(memory);
        ScannedData data = new ScannedData();
        List<HSRCharacter> characters = new ArrayList<>();
        for (String id : List.of("1308", "1310", "1407", "1505", "1217")) {
            HSRCharacter c = new HSRCharacter();
            c.setId(id);
            characters.add(c);
        }
        data.setCharacters(characters);
        memory.insertMemory(USER, data);
    }

    @Test
    void saveAndGetTeam() {
        teamService.saveTeam(USER, List.of("1308", "1310"));
        assertThat(teamService.getTeam(USER)).extracting(HSRCharacter::getId)
            .containsExactly("1308", "1310");
    }

    @Test
    void emptyTeamWhenNeverSaved() {
        assertThat(teamService.getTeam(USER)).isEmpty();
    }

    @Test
    void duplicateCharactersRejected() {
        assertThatThrownBy(() -> teamService.saveTeam(USER, List.of("1308", "1308")))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void moreThanFourRejected() {
        assertThatThrownBy(
            () -> teamService.saveTeam(USER, List.of("1308", "1310", "1407", "1505", "1217")))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void unknownCharacterRejected() {
        assertThatThrownBy(() -> teamService.saveTeam(USER, List.of("9999")))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
