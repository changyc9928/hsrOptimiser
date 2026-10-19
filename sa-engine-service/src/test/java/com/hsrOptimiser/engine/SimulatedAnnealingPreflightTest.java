package com.hsrOptimiser.engine;

import static org.assertj.core.api.Assertions.assertThat;

import com.hsrOptimiser.DTO.asagi.CharactersItem;
import com.hsrOptimiser.DTO.asagi.MocRequest;
import com.hsrOptimiser.DTO.hsrScanner.HSRCharacter;
import com.hsrOptimiser.DTO.hsrScanner.LightCone;
import com.hsrOptimiser.DTO.hsrScanner.ScannedData;
import java.util.List;
import org.junit.jupiter.api.Test;

class SimulatedAnnealingPreflightTest {

    @Test
    void namesRejectedConeAndCharacter() {
        HSRCharacter character = new HSRCharacter();
        character.setId("1308");
        character.setName("Acheron");
        LightCone cone = new LightCone();
        cone.setUid("lc1");
        cone.setId("20024");
        cone.setName("Lingering Tear");
        cone.setLocation("1308");
        ScannedData data = new ScannedData();
        data.setCharacters(List.of(character));
        data.setLightCones(List.of(cone));

        String message = "{\"detail\":[{\"type\":\"enum\","
            + "\"loc\":[\"body\",\"characters\",0,\"light_cones\"],"
            + "\"msg\":\"Input should be X\",\"input\":\"LingeringTear\"}]}";

        String result = SimulatedAnnealing.describeRejection(
            message, List.of("1308"), data);

        assertThat(result).contains("1308");
        assertThat(result).contains("LingeringTear");
        assertThat(result).contains("Lingering Tear (20024)");
    }

    @Test
    void fallsBackToShortMessage() {
        String result = SimulatedAnnealing.describeRejection(
            "boom", List.of("1308"), new ScannedData());

        assertThat(result).contains("boom");
    }

    @Test
    void findsCharactersWithoutCones() {
        CharactersItem withCone = new CharactersItem();
        withCone.setLightCones("InTheNight");
        CharactersItem withoutCone = new CharactersItem();
        MocRequest request = new MocRequest();
        request.setCharacters(List.of(withCone, withoutCone));

        assertThat(SimulatedAnnealing.findCharactersWithoutCones(
            request, List.of("1308", "1310"))).containsExactly("1310");
    }
}
