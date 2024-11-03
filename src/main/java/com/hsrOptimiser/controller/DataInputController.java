package com.hsrOptimiser.controller;

import com.hsrOptimiser.domain.ApiResponse;
import com.hsrOptimiser.domain.ScannedDataSize;
import com.hsrOptimiser.domain.hsrScanner.Character;
import com.hsrOptimiser.domain.hsrScanner.LightCone;
import com.hsrOptimiser.domain.hsrScanner.Relic;
import com.hsrOptimiser.domain.hsrScanner.ScannedData;
import com.hsrOptimiser.services.DataPopulatingService;
import jakarta.servlet.http.HttpSession;
import java.util.ArrayList;
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
        httpSession.setAttribute("scanned_data", scannedData);
        ScannedDataSize scannedDataSize = new ScannedDataSize(scannedData.getCharacters().size(),
            scannedData.getLightCones().size(), scannedData.getRelics().size());
        ApiResponse<ScannedDataSize> apiResponse = new ApiResponse<>(true, scannedDataSize,
            "Successfully imported data", HttpStatus.OK.value());
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/character")
    public ResponseEntity<ApiResponse<ArrayList<Character>>> getCharacterList(
        HttpSession httpSession) {
        ScannedData scannedData = (ScannedData) httpSession.getAttribute("scanned_data");
        ApiResponse<ArrayList<Character>> apiResponse = new ApiResponse<>(true,
            scannedData.getCharacters(), "Successfully retrieved the list of characters",
            HttpStatus.OK.value());
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/light-cone")
    public ResponseEntity<ApiResponse<ArrayList<LightCone>>> getLightConeList(
        HttpSession httpSession) {
        ScannedData scannedData = (ScannedData) httpSession.getAttribute("scanned_data");
        ApiResponse<ArrayList<LightCone>> apiResponse = new ApiResponse<>(true,
            scannedData.getLightCones(), "Successfully retrieved the list of light cones",
            HttpStatus.OK.value());
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/relic")
    public ResponseEntity<ApiResponse<ArrayList<Relic>>> getRelicList(HttpSession httpSession) {
        ScannedData scannedData = (ScannedData) httpSession.getAttribute("scanned_data");
        ApiResponse<ArrayList<Relic>> apiResponse = new ApiResponse<>(true, scannedData.getRelics(),
            "Successfully retrieved the list of relics", HttpStatus.OK.value());
        return ResponseEntity.ok(apiResponse);
    }
}
