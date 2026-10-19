package com.hsrOptimiser.controller;

import com.hsrOptimiser.DTO.ApiResponse;
import com.hsrOptimiser.DTO.SaveTeamRequest;
import com.hsrOptimiser.DTO.hsrScanner.HSRCharacter;
import com.hsrOptimiser.services.TeamService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/team")
@RequiredArgsConstructor
public class TeamController {

    private final TeamService teamService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<HSRCharacter>>> getTeam(
        @RequestAttribute("userId") String userId) {
        return ResponseEntity.ok(new ApiResponse<>(true,
            teamService.getTeam(userId), "OK", HttpStatus.OK.value()));
    }

    @PutMapping
    public ResponseEntity<ApiResponse<List<HSRCharacter>>> saveTeam(
        @RequestAttribute("userId") String userId,
        @RequestBody SaveTeamRequest request) {
        return ResponseEntity.ok(new ApiResponse<>(true,
            teamService.saveTeam(userId, request.getCharacterIds()),
            "Team saved", HttpStatus.OK.value()));
    }
}
