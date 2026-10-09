package com.hsrOptimiser.controller;

import com.hsrOptimiser.DTO.ApiResponse;
import com.hsrOptimiser.DTO.EquipLightConeRequest;
import com.hsrOptimiser.DTO.EquipRelicsRequest;
import com.hsrOptimiser.DTO.LoadoutDTO;
import com.hsrOptimiser.services.LoadoutService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/loadout")
@RequiredArgsConstructor
public class LoadoutController {

    private final LoadoutService loadoutService;

    @GetMapping("/{characterId}")
    public ResponseEntity<ApiResponse<LoadoutDTO>> getLoadout(
        @RequestAttribute("userId") String userId,
        @PathVariable String characterId) {
        return ResponseEntity.ok(new ApiResponse<>(true,
            loadoutService.getLoadout(userId, characterId),
            "OK", HttpStatus.OK.value()));
    }

    @PostMapping("/{characterId}/lightcone")
    public ResponseEntity<ApiResponse<LoadoutDTO>> equipLightCone(
        @RequestAttribute("userId") String userId,
        @PathVariable String characterId,
        @RequestBody EquipLightConeRequest request) {
        return ResponseEntity.ok(new ApiResponse<>(true,
            loadoutService.equipLightCone(userId, characterId, request.getLightConeUid()),
            "Light cone equipped", HttpStatus.OK.value()));
    }

    @DeleteMapping("/{characterId}/lightcone")
    public ResponseEntity<ApiResponse<LoadoutDTO>> unequipLightCone(
        @RequestAttribute("userId") String userId,
        @PathVariable String characterId) {
        return ResponseEntity.ok(new ApiResponse<>(true,
            loadoutService.unequipLightCone(userId, characterId),
            "Light cone unequipped", HttpStatus.OK.value()));
    }

    @PutMapping("/{characterId}/relics")
    public ResponseEntity<ApiResponse<LoadoutDTO>> equipRelics(
        @RequestAttribute("userId") String userId,
        @PathVariable String characterId,
        @RequestBody EquipRelicsRequest request) {
        return ResponseEntity.ok(new ApiResponse<>(true,
            loadoutService.equipRelics(userId, characterId, request.getRelicUids()),
            "Relics equipped", HttpStatus.OK.value()));
    }

    @DeleteMapping("/{characterId}/relics")
    public ResponseEntity<ApiResponse<LoadoutDTO>> unequipRelics(
        @RequestAttribute("userId") String userId,
        @PathVariable String characterId) {
        return ResponseEntity.ok(new ApiResponse<>(true,
            loadoutService.unequipRelics(userId, characterId),
            "Relics unequipped", HttpStatus.OK.value()));
    }
}
