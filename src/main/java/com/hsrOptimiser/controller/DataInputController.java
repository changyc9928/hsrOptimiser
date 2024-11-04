package com.hsrOptimiser.controller;

import com.hsrOptimiser.domain.ApiResponse;
import com.hsrOptimiser.domain.ScannedDataSize;
import com.hsrOptimiser.domain.hsrScanner.Character;
import com.hsrOptimiser.domain.hsrScanner.LightCone;
import com.hsrOptimiser.domain.hsrScanner.Relic;
import com.hsrOptimiser.domain.hsrScanner.ScannedData;
import com.hsrOptimiser.domain.hsrScanner.populatedData.PopulatedData;
import com.hsrOptimiser.services.DataPopulatingService;
import jakarta.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.stream.Collectors;
import org.apache.tomcat.util.digester.ArrayStack;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/data")
public class DataInputController {

    @Autowired
    DataPopulatingService dataPopulatingService;

    @PostMapping
    public ResponseEntity<ApiResponse<ScannedDataSize>> upload(HttpSession httpSession,
        @RequestBody ScannedData scannedData) {
        ScannedDataSize scannedDataSize = new ScannedDataSize(scannedData.getCharacters().size(),
            scannedData.getLightCones().size(), scannedData.getRelics().size());
        PopulatedData populatedData = dataPopulatingService.populateData(scannedData);
        httpSession.setAttribute("populated_data", populatedData);
        ApiResponse<ScannedDataSize> apiResponse = new ApiResponse<>(true, scannedDataSize,
            "Successfully imported data", HttpStatus.OK.value());
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/character")
    public ResponseEntity<ApiResponse<ArrayList<Character>>> getCharacterList(
        HttpSession httpSession) {
        PopulatedData populatedData = (PopulatedData) httpSession.getAttribute("populated_data");
        ApiResponse<ArrayList<Character>> apiResponse = new ApiResponse<>(true,
            populatedData.getCharacters().values().stream().map(populatedCharacter -> {
                Character character = new Character();
                character.setAscension(populatedCharacter.getAscension());
                character.setId(populatedCharacter.getId());
                character.setName(populatedCharacter.getName());
                character.setPath(populatedCharacter.getPath());
                character.setLevel(populatedCharacter.getLevel());
                character.setEidolon(populatedCharacter.getEidolon());
                character.setSkills(populatedCharacter.getSkills());
                character.setTraces(populatedCharacter.getTraces());
                return character;
            }).collect(Collectors.toCollection(ArrayStack::new)),
            "Successfully retrieved the list of characters",
            HttpStatus.OK.value());
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/light-cone")
    public ResponseEntity<ApiResponse<ArrayList<LightCone>>> getLightConeList(
        HttpSession httpSession) {
        PopulatedData populatedData = (PopulatedData) httpSession.getAttribute("populated_data");
        ApiResponse<ArrayList<LightCone>> apiResponse = new ApiResponse<>(true,
            populatedData.getLightCones().values().stream().map(populatedLightCone -> {
                LightCone lightCone = new LightCone();
                lightCone.setAscension(populatedLightCone.getAscension());
                lightCone.setName(populatedLightCone.getName());
                lightCone.setLock(populatedLightCone.isLock());
                lightCone.setLevel(populatedLightCone.getLevel());
                lightCone.setLocation(populatedLightCone.getLocation());
                lightCone.setUid(populatedLightCone.getUid());
                lightCone.setSuperimposition(populatedLightCone.getSuperimposition());
                lightCone.setId(populatedLightCone.getId());
                return lightCone;
            }).collect(Collectors.toCollection(ArrayList::new)),
            "Successfully retrieved the list of light cones",
            HttpStatus.OK.value());
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/relic")
    public ResponseEntity<ApiResponse<ArrayList<Relic>>> getRelicList(HttpSession httpSession) {
        PopulatedData populatedData = (PopulatedData) httpSession.getAttribute("populated_data");
        ApiResponse<ArrayList<Relic>> apiResponse = new ApiResponse<>(true,
            populatedData.getRelics().values().stream().map(populatedRelic -> {
                Relic relic = new Relic();
                relic.setName(populatedRelic.getName());
                relic.setLock(populatedRelic.isLock());
                relic.setMainStat(populatedRelic.getMainStat());
                relic.setUid(populatedRelic.getUid());
                relic.setRarity(populatedRelic.getRarity());
                relic.setSlot(populatedRelic.getSlot());
                relic.setSubStats(populatedRelic.getSubStats());
                relic.setDiscard(populatedRelic.isDiscard());
                relic.setLocation(populatedRelic.getLocation());
                relic.setSetId(populatedRelic.getSetId());
                relic.setLevel(populatedRelic.getLevel());
                return relic;
            }).collect(Collectors.toCollection(ArrayList::new)),
            "Successfully retrieved the list of relics", HttpStatus.OK.value());
        return ResponseEntity.ok(apiResponse);
    }
}
