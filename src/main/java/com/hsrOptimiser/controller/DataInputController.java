package com.hsrOptimiser.controller;

import com.hsrOptimiser.domain.ApiResponse;
import com.hsrOptimiser.domain.ScannedDataSize;
import com.hsrOptimiser.domain.hsrScanner.ScannedData;
import com.hsrOptimiser.domain.hsrScanner.populatedData.PopulatedData;
import com.hsrOptimiser.services.DataPopulatingService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/data")
public class DataInputController {

    @Autowired
    DataPopulatingService dataPopulatingService;

    @PostMapping
    public ResponseEntity<ApiResponse<ScannedDataSize>> upload(HttpSession httpSession, @RequestBody ScannedData scannedData) {
        httpSession.setAttribute("scanned_data", scannedData);
        ScannedDataSize scannedDataSize = new ScannedDataSize(scannedData.getCharacters().size(), scannedData.getLightCones().size(), scannedData.getRelics().size());
        ApiResponse<ScannedDataSize> apiResponse = new ApiResponse<>(true, scannedDataSize, "Successfully imported data", HttpStatus.OK.value());
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PopulatedData>> download(HttpSession httpSession) {
        ScannedData scannedData = (ScannedData) httpSession.getAttribute("scanned_data");
        PopulatedData populatedData = dataPopulatingService.populateData(scannedData);
        ApiResponse<PopulatedData> apiResponse = new ApiResponse<>(true, populatedData, "Successfully retrieved populated data", HttpStatus.OK.value());
        return ResponseEntity.ok(apiResponse);
    }
}
