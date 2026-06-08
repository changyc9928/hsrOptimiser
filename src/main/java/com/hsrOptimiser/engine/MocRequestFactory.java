package com.hsrOptimiser.engine;

import com.hsrOptimiser.DTO.asagi.MocRequest;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Factory for creating base {@link MocRequest} instances with standard
 * configuration.
 */
@Component
public class MocRequestFactory {

    public MocRequest createBaseRequest() {
        MocRequest request = new MocRequest();
        request.setBattleMode("multi");
        request.setRound(6);

        List<String> universalWeaks = List.of(
                "Physical", "Fire", "Ice", "Wind", "Lightning", "Imaginary", "Quantum");
        request.setBossWeaks(universalWeaks);
        request.setBoss2Weaks(universalWeaks);
        request.setFollowerWeaks(universalWeaks);

        request.setStartSp(3);
        request.setDistributeEp(true);
        request.setHealerSpeed(134);
        request.setEnemyLevel("base90");
        request.setInputActualNumber(true);
        request.setBronyaStrategy(1);
        request.setTopazStrategy(0);
        request.setBoothillStrategy(2);
        request.setEnableBreak(true);
        request.setFixBreak(true);

        return request;
    }
}
