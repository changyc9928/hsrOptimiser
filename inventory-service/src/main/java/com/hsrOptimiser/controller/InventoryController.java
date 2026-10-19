package com.hsrOptimiser.controller;

import com.hsrOptimiser.DTO.ApiResponse;
import com.hsrOptimiser.DTO.CreateCharacterRequest;
import com.hsrOptimiser.DTO.CreateLightConeRequest;
import com.hsrOptimiser.DTO.CreateRelicRequest;
import com.hsrOptimiser.DTO.UpdateCharacterRequest;
import com.hsrOptimiser.DTO.UpdateLightConeRequest;
import com.hsrOptimiser.DTO.UpdateRelicRequest;
import com.hsrOptimiser.DTO.hsrScanner.HSRCharacter;
import com.hsrOptimiser.DTO.hsrScanner.LightCone;
import com.hsrOptimiser.DTO.hsrScanner.Relic;
import com.hsrOptimiser.services.LoadoutService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final LoadoutService loadoutService;

    @PostMapping("/lightcones")
    public ResponseEntity<ApiResponse<LightCone>> addLightCone(
        @RequestAttribute("userId") String userId,
        @RequestBody CreateLightConeRequest request) {
        return ResponseEntity.ok(new ApiResponse<>(true,
            loadoutService.addLightCone(userId, request),
            "Light cone added", HttpStatus.OK.value()));
    }

    @PostMapping("/characters")
    public ResponseEntity<ApiResponse<HSRCharacter>> addCharacter(
        @RequestAttribute("userId") String userId,
        @RequestBody CreateCharacterRequest request) {
        return ResponseEntity.ok(new ApiResponse<>(true,
            loadoutService.addCharacter(userId, request),
            "Character added", HttpStatus.OK.value()));
    }

    @PostMapping("/relics")
    public ResponseEntity<ApiResponse<Relic>> addRelic(
        @RequestAttribute("userId") String userId,
        @RequestBody CreateRelicRequest request) {
        return ResponseEntity.ok(new ApiResponse<>(true,
            loadoutService.addRelic(userId, request),
            "Relic added", HttpStatus.OK.value()));
    }

    @PutMapping("/characters/{id}")
    public ResponseEntity<ApiResponse<HSRCharacter>> updateCharacter(
        @RequestAttribute("userId") String userId,
        @PathVariable String id,
        @RequestBody UpdateCharacterRequest request) {
        return ResponseEntity.ok(new ApiResponse<>(true,
            loadoutService.updateCharacter(userId, id, request),
            "Character updated", HttpStatus.OK.value()));
    }

    @PutMapping("/lightcones/{uid}")
    public ResponseEntity<ApiResponse<LightCone>> updateLightCone(
        @RequestAttribute("userId") String userId,
        @PathVariable String uid,
        @RequestBody UpdateLightConeRequest request) {
        return ResponseEntity.ok(new ApiResponse<>(true,
            loadoutService.updateLightCone(userId, uid, request),
            "Light cone updated", HttpStatus.OK.value()));
    }

    @PutMapping("/relics/{uid}")
    public ResponseEntity<ApiResponse<Relic>> updateRelic(
        @RequestAttribute("userId") String userId,
        @PathVariable String uid,
        @RequestBody UpdateRelicRequest request) {
        return ResponseEntity.ok(new ApiResponse<>(true,
            loadoutService.updateRelic(userId, uid, request),
            "Relic updated", HttpStatus.OK.value()));
    }
}
