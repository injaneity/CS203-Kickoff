package com.crashcourse.kickoff.tms.club.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.crashcourse.kickoff.tms.club.dto.ApplicationUpdateDTO;
import com.crashcourse.kickoff.tms.club.dto.CaptainTransferRequest;
import com.crashcourse.kickoff.tms.club.dto.ClubCreationRequest;
import com.crashcourse.kickoff.tms.club.dto.ClubRatingUpdateDTO;
import com.crashcourse.kickoff.tms.club.dto.PlayerApplicationDTO;
import com.crashcourse.kickoff.tms.club.dto.PlayerInviteRequest;
import com.crashcourse.kickoff.tms.club.dto.ClubPenaltyStatusRequest;
import com.crashcourse.kickoff.tms.club.dto.PlayerLeaveRequest;
import com.crashcourse.kickoff.tms.club.exception.ClubNotFoundException;
import com.crashcourse.kickoff.tms.club.exception.PenaltyNotFoundException;
import com.crashcourse.kickoff.tms.club.model.Club;
import com.crashcourse.kickoff.tms.club.model.ClubPenaltyStatus;
import com.crashcourse.kickoff.tms.club.model.ClubProfile;
import com.crashcourse.kickoff.tms.club.service.ClubServiceImpl;
import com.crashcourse.kickoff.tms.security.JwtAuthService;
import com.crashcourse.kickoff.tms.security.JwtUtil;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/clubs")
@RequiredArgsConstructor
public class ClubController {

    private final ClubServiceImpl clubService;
    private final JwtUtil jwtUtil; // final for constructor injection
    private final JwtAuthService jwtAuthService;

    /**
     * Create a new Club.
     *
     * @param clubRequest DTO containing club creation data.
     * @param token       Authorization token from the request header.
     * @return ResponseEntity with the created Club data and HTTP status.
     */
    @PostMapping("/createClub")
    public ResponseEntity<?> createClub(@Valid @RequestBody ClubCreationRequest clubRequest,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token) {
        try {
            token = token.substring(7);
            Long userIdFromToken = jwtUtil.extractUserId(token);
            Club createdClub = clubService.createClub(clubRequest.getClub(), userIdFromToken);
            return new ResponseEntity<>(createdClub, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Retrieve all Clubs.
     *
     * @return List of all Club entities.
     */
    @GetMapping
    public List<Club> getAllClubs() {
        return clubService.getAllClubs();
    }

    // @GetMapping("/{clubId}")
    // public ResponseEntity<?> getClubById(@PathVariable Long clubId) {
    // Optional<Club> club = clubService.getClubById(clubId);
    // if (club.isPresent()) {
    // return new ResponseEntity<>(club.get(), HttpStatus.OK);
    // }
    // return new ResponseEntity<String>("Club not found", HttpStatus.NOT_FOUND);
    // }

    /**
     * Update an existing Club.
     *
     * @param clubId      ID of the club to update.
     * @param clubDetails Club entity containing updated data.
     * @return ResponseEntity with the updated Club data and HTTP status.
     */
    @PutMapping("/{clubId}")
    public ResponseEntity<?> updateClub(@PathVariable Long clubId, @RequestBody Club clubDetails) {
        try {
            Club updatedClub = clubService.updateClub(clubId, clubDetails);
            return new ResponseEntity<>(updatedClub, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Delete a Club by its ID.
     *
     * @param clubId ID of the club to delete.
     * @return ResponseEntity with a success message and HTTP status.
     */
    @DeleteMapping("/{clubId}")
    public ResponseEntity<?> deleteClub(@PathVariable Long clubId) {
        clubService.deleteClub(clubId);
        return new ResponseEntity<>("Club deleted successfully", HttpStatus.OK);
    }

    /**
     * Transfer captaincy of a Club to another player.
     *
     * @param clubId  ID of the club.
     * @param request DTO containing current and new captain IDs.
     * @return ResponseEntity with the updated Club data and HTTP status.
     */
    @PatchMapping("/{clubId}/transferCaptain")
    public ResponseEntity<?> transferCaptaincy(@PathVariable Long clubId, @RequestBody CaptainTransferRequest request) {
        try {
            Club updatedClub = clubService.transferCaptaincy(clubId, request.getCurrentCaptainId(),
                    request.getNewCaptainId());
            return new ResponseEntity<>(updatedClub, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Add a player to a Club.
     *
     * @param clubId   ID of the club.
     * @param playerId ID of the player to add.
     * @return ResponseEntity with the updated Club data and HTTP status.
     */
    @PatchMapping("/{clubId}/addPlayer")
    public ResponseEntity<?> addPlayerToClub(@PathVariable Long clubId, @RequestBody Long playerId) {
        try {
            Club updatedClub = clubService.addPlayerToClub(clubId, playerId);
            return new ResponseEntity<>(updatedClub, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Remove a player from a Club.
     *
     * @param clubId   ID of the club.
     * @param playerId ID of the player to remove.
     * @return ResponseEntity with the updated Club data and HTTP status.
     */
    @PatchMapping("/{clubId}/removePlayer")
    public ResponseEntity<?> removePlayerFromClub(@PathVariable Long clubId, @RequestBody Long playerId) {
        try {
            Club updatedClub = clubService.removePlayerFromClub(clubId, playerId);
            return new ResponseEntity<>(updatedClub, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Apply to join a Club.
     *
     * @param clubId         ID of the club to apply to.
     * @param applicationDTO DTO containing player application data.
     * @return ResponseEntity with a success message and HTTP status.
     */
    @PostMapping("/{clubId}/apply")
    public ResponseEntity<?> applyToClub(@PathVariable Long clubId, @RequestBody PlayerApplicationDTO applicationDTO) {
        try {
            applicationDTO.setClubId(clubId);
            clubService.applyToClub(applicationDTO);
            return new ResponseEntity<>("Application submitted successfully", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Invite a player to join a Club.
     *
     * @param clubId       ID of the club.
     * @param inviteRequest DTO containing player invite data.
     * @return ResponseEntity with a success message and HTTP status.
     */
    @PostMapping("/{clubId}/invite")
    public ResponseEntity<?> invitePlayerToClub(@PathVariable Long clubId,
            @RequestBody PlayerInviteRequest inviteRequest) {
        try {
            clubService.invitePlayerToClub(clubId, inviteRequest.getPlayerId(), inviteRequest.getCaptainId());
            return new ResponseEntity<>("Invitation sent successfully", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Retrieve the penalty status of a Club.
     *
     * @param clubId ID of the club.
     * @return ResponseEntity with the penalty status and HTTP status.
     */
    @GetMapping("/{clubId}/penaltystatus")
    public ResponseEntity<?> getPenaltyStatus(@PathVariable Long clubId) {
        try {
            ClubPenaltyStatus penaltyStatus = clubService.getPenaltyStatusByClubId(clubId);
    
            if (penaltyStatus == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Penalty status not found for Club ID: " + clubId);
            }
    
            // Check if penalty is active and return a boolean response
            boolean hasPenalty = penaltyStatus.hasActivePenalty();
            return ResponseEntity.ok(hasPenalty);
        } catch (ClubNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while retrieving the penalty status.");
        }
    }

    /**
     * Update the penalty status of a Club.
     *
     * @param clubId               ID of the club.
     * @param penaltyStatusRequest DTO containing penalty status data.
     * @param token                Authorization token from the request header.
     * @return ResponseEntity with the updated ClubProfile data and HTTP status.
     */
    @PutMapping("/{clubId}/status")
    public ResponseEntity<?> updateClubStatus(
            @PathVariable Long clubId,
            @RequestBody @Valid ClubPenaltyStatusRequest penaltyStatusRequest,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = true) String token) {

        // Validate the token and check if the user is an admin
        ResponseEntity<String> authResponse = jwtAuthService.validateAdminToken(token);
        if (authResponse != null) {
            return authResponse; // Return error response if token validation fails
        }

        ClubPenaltyStatus newStatus = new ClubPenaltyStatus(penaltyStatusRequest);
        try {
            ClubProfile updatedClubProfile = clubService.updateClubPenaltyStatus(clubId, newStatus);
            return ResponseEntity.ok(updatedClubProfile);
        } catch (ClubNotFoundException | PenaltyNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    /**
     * Retrieve all players in a Club.
     *
     * @param clubId ID of the club.
     * @return ResponseEntity with the list of player IDs and HTTP status.
     */
    @GetMapping("/{clubId}/players")
    public ResponseEntity<?> getPlayersFromClub(@PathVariable Long clubId) {
        try {
            List<Long> players = clubService.getPlayers(clubId);
            return new ResponseEntity<>(players, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Retrieve the profile of a Club.
     *
     * @param id ID of the club.
     * @return ResponseEntity with the ClubProfile data and HTTP status.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ClubProfile> getClubProfile(@PathVariable Long id) {
        Optional<Club> club = clubService.getClubById(id);
        if (club.isPresent()) {
            Club existingClub = club.get();
            ClubProfile profile = new ClubProfile(existingClub);
            return ResponseEntity.ok(profile);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    /**
     * Retrieve the Club associated with a Player.
     *
     * @param playerId ID of the player.
     * @return ResponseEntity with the Club data and HTTP status.
     */
    @GetMapping("/player/{playerId}")
    public ResponseEntity<Club> getClubByPlayerId(@PathVariable Long playerId) {
        return clubService.getClubByPlayerId(playerId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Retrieve player applications for a Club.
     *
     * @param clubId ID of the club.
     * @return ResponseEntity with the list of applicant IDs and HTTP status.
     */
    @GetMapping("/{clubId}/applications")
    public ResponseEntity<List<Long>> getPlayerApplications(@PathVariable Long clubId) {
        List<Long> applicants = clubService.getPlayerApplications(clubId);
        return new ResponseEntity<>(applicants, HttpStatus.OK);
    }

    /**
     * Process a player's application to a Club.
     *
     * @param clubId   ID of the club.
     * @param playerId ID of the player.
     * @param body     DTO containing application status.
     * @return ResponseEntity with HTTP status
     */
    @PostMapping("/{clubId}/applications/{playerId}")
    public ResponseEntity<?> processApplication(@PathVariable Long clubId, @PathVariable Long playerId,
            @RequestBody ApplicationUpdateDTO body) {
        final String ACCEPTED_STATUS = "ACCEPTED";
        final String REJECTED_STATUS = "REJECTED";
        String status = body.getApplicationStatus();
        if (status.equals(ACCEPTED_STATUS)) {

            clubService.acceptApplication(clubId, playerId);
            return new ResponseEntity<>(HttpStatus.OK);

        } else if (status.equals(REJECTED_STATUS)) {

            clubService.rejectApplication(clubId, playerId);
            return new ResponseEntity<>(HttpStatus.OK);

        } else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }


    /**
     * Allow a player to leave a Club.
     *
     * @param clubId            ID of the club.
     * @param playerLeaveRequest DTO containing player ID.
     * @return ResponseEntity with the updated Club data or a success message and HTTP status.
     */
    @PatchMapping("/{clubId}/leavePlayer")
    public ResponseEntity<?> playerLeaveClub(@PathVariable Long clubId,
            @RequestBody PlayerLeaveRequest playerLeaveRequest) {
        try {
            Long playerId = playerLeaveRequest.getPlayerId();
            Club updatedClub = clubService.playerLeaveClub(clubId, playerId);

            if (updatedClub == null) {
                return new ResponseEntity<>("Club has been disbanded.", HttpStatus.OK);
            }

            // If successful and the player has left the club
            return new ResponseEntity<>(updatedClub, HttpStatus.OK);

        } catch (ClubNotFoundException e) {
            return new ResponseEntity<>("Club not found.", HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            if (e.getMessage().equals("You must transfer the captaincy before leaving the club.")) {
                return new ResponseEntity<>(e.getMessage(), HttpStatus.FORBIDDEN); // Use 403 for permission-related
                                                                                   // issues
            } else if (e.getMessage().equals("Player is not a member of this club.")) {
                return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
            } else {
                // For other unexpected errors, log and return a generic response
                return new ResponseEntity<>("An error occurred while trying to leave the club",
                        HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
    }

    /**
     * Update the rating of a Club.
     *
     * @param clubId         ID of the club.
     * @param ratingUpdateDTO DTO containing rating update data.
     * @return ResponseEntity with HTTP status.
     */
    @PutMapping("/{clubId}/rating")
    public ResponseEntity<Void> updateClubRating(
            @PathVariable Long clubId,
            @RequestBody ClubRatingUpdateDTO ratingUpdateDTO) {

        clubService.updateClubRating(clubId, ratingUpdateDTO);
        return ResponseEntity.ok().build();
    }
}