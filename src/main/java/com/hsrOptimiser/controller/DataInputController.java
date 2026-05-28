package com.hsrOptimiser.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hsrOptimiser.DTO.ApiResponse;
import com.hsrOptimiser.DTO.ScannedDataSize;
import com.hsrOptimiser.DTO.hsrScanner.ScannedData;
import com.hsrOptimiser.services.Memory;
import java.io.IOException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/data")
@RequiredArgsConstructor
public class DataInputController {

    private final Memory memory;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostMapping("/upload/{userId}")
    public ResponseEntity<ApiResponse<ScannedDataSize>> uploadV2(
        @PathVariable String userId,
        @RequestParam("file") MultipartFile file) throws IOException {
        ScannedData scannedData = objectMapper.readValue(file.getInputStream(), ScannedData.class);
        memory.insertMemory(userId, scannedData);
        ScannedDataSize scannedDataSize = new ScannedDataSize(scannedData.getCharacters().size(),
            scannedData.getLightCones().size(), scannedData.getRelics().size());
        ApiResponse<ScannedDataSize> apiResponse = new ApiResponse<>(true, scannedDataSize,
            "Successfully imported data", HttpStatus.OK.value());
        return ResponseEntity.ok(apiResponse);
    }
}
