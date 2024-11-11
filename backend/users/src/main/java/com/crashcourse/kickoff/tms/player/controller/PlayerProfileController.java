package com.crashcourse.kickoff.tms.player.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.crashcourse.kickoff.tms.player.model.PlayerProfile;
import com.crashcourse.kickoff.tms.exception.*;
import com.crashcourse.kickoff.tms.player.dto.*;
import com.crashcourse.kickoff.tms.player.service.PlayerProfileService;
import com.crashcourse.kickoff.tms.security.JwtUtil;
import com.crashcourse.kickoff.tms.security.JwtAuthService;
import com.crashcourse.kickoff.tms.user.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/playerProfiles")
public class PlayerProfileController {

    private final PlayerProfileService playerProfileService;
    private final UserService userService; // final for constructor injection
    private final JwtUtil jwtUtil; // final for constructor injection
    private final JwtAuthService jwtAuthService; // final for constructor injection

    @Autowired
    public PlayerProfileController(PlayerProfileService playerProfileService, JwtUtil jwtUtil,
            UserService userService, JwtAuthService jwtAuthService) {
        this.playerProfileService = playerProfileService;
        this.jwtUtil = jwtUtil;
        this.userService = userService;
        this.jwtAuthService = jwtAuthService;
    }

    @GetMapping
    public ResponseEntity<?> getAllPlayerProfiles() {
        try {
            // Retrieve the list of PlayerProfile entities
            List<PlayerProfile> playerProfiles = playerProfileService.getPlayerProfiles();

            // Convert each PlayerProfile to PlayerProfileResponseDTO
            List<PlayerProfileResponseDTO> playerProfileDTOs = playerProfiles.stream()
                    .map(playerProfile -> new PlayerProfileResponseDTO(
                            playerProfile.getId(),
                            playerProfile.getUser().getUsername(),
                            playerProfile.getProfileDescription(),
                            playerProfile.getPreferredPositions(),
                            playerProfile.getStatus(),
                            userService.getUserById(playerProfile.getId()).getProfilePictureUrl()))
                    .collect(Collectors.toList());

            // Return the list of PlayerProfileResponseDTO wrapped in a ResponseEntity
            return ResponseEntity.ok(playerProfileDTOs);

        } catch (Exception ex) {
            // Handle any other unexpected errors
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An unexpected error occurred while retrieving player profiles.");
        }
    }

    @GetMapping("/{playerId}")
    public ResponseEntity<?> getPlayerProfile(@PathVariable Long playerId,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String token) {
        // if (token == null || !token.startsWith("Bearer ")) {
        // return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authorization
        // token is missing or invalid" + token);
        // }
        // token = token.substring(7);
        // // Extract the userId from the token using JwtUtil
        // Long userIdFromToken = jwtUtil.extractUserId(token);
        // if (!userIdFromUsername.equals(userIdFromToken)) {
        // return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not
        // authorized to view this profile");
        // }

        PlayerProfile playerProfile = playerProfileService.getPlayerProfile(playerId);
        if (playerProfile == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("PlayerProfile not found");
        }
        // Convert PlayerProfile entity to PlayerProfileDTO
        PlayerProfileResponseDTO playerProfileDTO = new PlayerProfileResponseDTO(
                playerProfile.getId(),
                playerProfile.getUser().getUsername(),
                playerProfile.getProfileDescription(),
                playerProfile.getPreferredPositions(),
                playerProfile.getStatus(),
                userService.getUserById(playerProfile.getId()).getProfilePictureUrl());

        return ResponseEntity.ok(playerProfileDTO);
    }

    @PutMapping("/{playerId}/status")
    public ResponseEntity<?> updatePlayerStatus(
            @PathVariable Long playerId,
            @RequestBody @Valid PlayerStatusRequest statusRequest,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = true) String token) {

        // Validate the token and check if the user is an admin
        ResponseEntity<String> authResponse = jwtAuthService.validateAdminToken(token);
        if (authResponse != null) {
            return authResponse; // Return error response if token validation fails
        }

        try {
            playerProfileService.updatePlayerStatus(playerId, statusRequest.getPlayerStatus());
            return ResponseEntity.ok("Player status updated successfully");
        } catch (PlayerNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to update player status");
        }
    }

    @PutMapping("/{id}/update")
    @PreAuthorize("@playerProfileService.isOwner(#id, authentication.name)")
    public ResponseEntity<?> updatePlayerProfile(@PathVariable Long id,
            @RequestBody PlayerProfileUpdateDTO playerProfileUpdateDTO) {
        PlayerProfile playerProfile = playerProfileService.getPlayerProfile(id);
        if (playerProfile == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("PlayerProfile not found");
        }

        // The @PreAuthorize annotation ensures only the owner can proceed

        // Update the player profile
        PlayerProfile updatedProfile = playerProfileService.updatePlayerProfile(playerProfile, playerProfileUpdateDTO);

        return new ResponseEntity<>(updatedProfile, HttpStatus.OK);
    }

    @PostMapping("/{playerId}/acceptInvitation")
    public ResponseEntity<?> acceptInvitation(@PathVariable Long playerId,
            @RequestBody AcceptInvitationRequest request) {
        try {
            // Club club = clubService.acceptInvite(playerId, request.getClubId());
            // return new ResponseEntity<>(club, HttpStatus.OK);
            return new ResponseEntity<>("", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}
