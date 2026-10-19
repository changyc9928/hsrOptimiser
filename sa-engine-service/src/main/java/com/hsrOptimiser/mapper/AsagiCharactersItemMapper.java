package com.hsrOptimiser.mapper;

import com.hsrOptimiser.DTO.asagi.CharactersItem;
import com.hsrOptimiser.DTO.asagi.LightConesObj;
import com.hsrOptimiser.DTO.asagi.RelicSub;
import com.hsrOptimiser.DTO.hsrScanner.HSRCharacter;
import com.hsrOptimiser.DTO.hsrScanner.Relic;
import com.hsrOptimiser.DTO.hsrScanner.ScannedData;
import com.hsrOptimiser.clientConfig.AsagiCharacterMetadata;
import com.hsrOptimiser.services.AsagiRelicBuildService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class AsagiCharactersItemMapper {

    private final AsagiRelicBuildService relicBuildService;

    public CharactersItem map(ScannedData scannedData, String characterId) {

        HSRCharacter character = scannedData.getCharacters().stream()
            .filter(c -> c.getId().equals(characterId))
            .findFirst()
            .orElseThrow();

        AsagiCharacterMetadata info = AsagiCharacterMetadata.getInfoById(
            character.getId(),
            character.getAbilityVersion()
        );

        CharactersItem dto = new CharactersItem();

        applyBaseInfo(dto, character, info);
        applyLightCone(dto, scannedData, characterId);
        applyRelics(dto, scannedData, characterId);

        return dto;
    }

    private void applyBaseInfo(
        CharactersItem dto,
        HSRCharacter character,
        AsagiCharacterMetadata info
    ) {
        dto.setId(character.getId());
        dto.setEidolon(character.getEidolon());

        dto.setEnableEidolon(true);
        dto.setUseTechnique(true);

        dto.setType(1);

        dto.setSpeed(info.getMaxSp());
        dto.setAddSpeed(info.getAddSpeed());
        dto.setSkillPriority(info.getSkillPriority());

        dto.setReality(info.getRarity());
        dto.setJob(info.getPath());

        dto.setKey(info.getDisplayName());
        dto.setName(info.getDisplayName());
    }

    private void applyLightCone(
        CharactersItem dto,
        ScannedData scannedData,
        String characterId
    ) {

        scannedData.getLightCones().stream()
            .filter(lc -> characterId.equals(lc.getLocation()))
            .findFirst()
            .ifPresent(cone -> {

                LightConesObj obj =
                    AsagiLightConeMapper.mapToObjectById(cone.getId());

                dto.setLightConesObj(obj);
                dto.setLightCones(obj.getKey());
            });
    }

    public void applyRelics(
        CharactersItem dto,
        ScannedData scannedData,
        String characterId
    ) {

        List<Relic> relics = scannedData.getRelics().stream()
            .filter(r -> characterId.equals(r.getLocation()))
            .toList();

        dto.setRelicSub(new RelicSub());

        dto.setRelicSet(
            relicBuildService.buildRelicSet(relics)
        );

        dto.setRelicMain(
            relicBuildService.buildRelicMain(relics)
        );
    }
}
