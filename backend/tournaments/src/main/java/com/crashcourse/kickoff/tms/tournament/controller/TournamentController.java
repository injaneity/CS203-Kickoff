package com.crashcourse.kickoff.tms.tournament.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.crashcourse.kickoff.tms.bracket.dto.MatchUpdateDTO;
import com.crashcourse.kickoff.tms.bracket.model.Match;
import com.crashcourse.kickoff.tms.client.AmazonClient;
import com.crashcourse.kickoff.tms.client.exception.ClubProfileNotFoundException;
import com.crashcourse.kickoff.tms.security.JwtUtil;
import com.crashcourse.kickoff.tms.tournament.dto.*;
import com.crashcourse.kickoff.tms.tournament.exception.InvalidWinningClubException;
import com.crashcourse.kickoff.tms.tournament.exception.MatchNotFoundException;
import com.crashcourse.kickoff.tms.tournament.exception.TournamentNotFoundException;

import io.github.cdimascio.dotenv.Dotenv;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.web.multipart.MultipartFile;
import org.springframework.mock.web.MockMultipartFile;
import java.util.Base64;

import com.crashcourse.kickoff.tms.tournament.service.TournamentService;
import com.crashcourse.kickoff.tms.tournament.model.Tournament;
import com.crashcourse.kickoff.tms.tournament.model.TournamentFilter;

import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.net.Webhook;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * REST Controller for managing Tournaments.
 * Provides endpoints to create, retrieve, update, delete, and list tournaments.
 */
@RestController
@RequestMapping("/tournaments")
@RequiredArgsConstructor
public class TournamentController {

    private final TournamentService tournamentService;
    private final JwtUtil jwtUtil;

    public static final String BEARER_PREFIX = "Bearer ";

    @Autowired
    private AmazonClient amazonClient;

    /**
     * Create a new Tournament.
     *
     * @param tournamentCreateDTO DTO containing tournament creation data.
     * @return ResponseEntity with the created Tournament data and HTTP status.
     */
    @PostMapping
    public ResponseEntity<TournamentResponseDTO> createTournament(
            @Valid @RequestBody TournamentCreateDTO tournamentCreateDTO,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token) {
        token = token.substring(7);
        Long userIdFromToken = jwtUtil.extractUserId(token);
        TournamentResponseDTO createdTournament = tournamentService.createTournament(tournamentCreateDTO,
                userIdFromToken);
        return new ResponseEntity<>(createdTournament, HttpStatus.CREATED);
    }

    /**
     * Retrieve a Tournament by its ID.
     *
     * @param id ID of the tournament to retrieve.
     * @return ResponseEntity with the Tournament data and HTTP status.
     */
    @GetMapping("/{id}")
    public ResponseEntity<TournamentResponseDTO> getTournamentById(@PathVariable Long id) {
        TournamentResponseDTO tournament = tournamentService.getTournamentById(id);
        return ResponseEntity.ok(tournament);
    }

    /**
     * Retrieve all Tournaments.
     *
     * @return ResponseEntity with the list of Tournaments and HTTP status.
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getAllTournaments() {
        try {
            List<Tournament> tournaments = tournamentService.getAllTournaments();
            // Handle case where no tournaments are found
            if (tournaments.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body("No tournaments available.");
            }
            return ResponseEntity.ok(tournaments);
        } catch (Exception ex) {
            // Handle any other unexpected errors
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while retrieving tournaments.");
        }
    }

    /**
     * Update an existing Tournament.
     *
     * @param id                  ID of the tournament to update.
     * @param tournamentCreateDTO DTO containing updated tournament data.
     * @return ResponseEntity with the updated Tournament data and HTTP status.
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateTournament(
            @PathVariable Long id,
            @Valid @RequestBody TournamentUpdateDTO tournamentUpdateDTO,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String token) {

        if (token == null || !token.startsWith(BEARER_PREFIX)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Authorization token is missing or invalid" + token);
        }
        token = token.substring(7);
        Long userIdFromToken = jwtUtil.extractUserId(token);

        boolean isOwnerOfTournament = tournamentService.isOwnerOfTournament(id, userIdFromToken);

        if (!isOwnerOfTournament) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not authorized to update this tournament");
        }
        TournamentResponseDTO updatedTournament = tournamentService.updateTournament(id, tournamentUpdateDTO);
        return ResponseEntity.ok(updatedTournament);
    }

    /**
     * Start a Tournament.
     *
     * @param id    ID of the tournament to start.
     * @param token Authorization token from the request header.
     * @return ResponseEntity with the Tournament data and HTTP status.
     */
    @PostMapping("/{id}/start")
    public ResponseEntity<?> startTournament(@PathVariable Long id,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = true) String token) {
        if (token == null || !token.startsWith(BEARER_PREFIX)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Authorization token is missing or invalid." + token);
        }
        token = token.substring(7);
        Long userIdFromToken = jwtUtil.extractUserId(token);

        boolean isOwnerOfTournament = tournamentService.isOwnerOfTournament(id, userIdFromToken);

        if (!isOwnerOfTournament) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not authorized to start this tournament.");
        }
        return ResponseEntity.ok(tournamentService.startTournament(id, token));
    }

    /**
     * Update a Match within a Tournament.
     *
     * @param tournamentId   ID of the tournament.
     * @param matchId        ID of the match to update.
     * @param matchUpdateDTO DTO containing match update data.
     * @param token          Authorization token from the request header.
     * @return ResponseEntity with the updated Match data and HTTP status.
     */
    @PutMapping("{tournamentId}/{matchId}")
    public ResponseEntity<?> updateMatchInTournament(@PathVariable Long tournamentId, @PathVariable Long matchId,
            @RequestBody MatchUpdateDTO matchUpdateDTO,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = true) String token) {
        try {
            Match match = tournamentService.updateMatchInTournament(tournamentId, matchId, matchUpdateDTO, token);
            return new ResponseEntity<>(match, HttpStatus.OK);
        } catch (TournamentNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (MatchNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (ClubProfileNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (InvalidWinningClubException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Delete a Tournament by its ID.
     *
     * @param id ID of the tournament to delete.
     * @return ResponseEntity with HTTP status.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTournament(@PathVariable Long id) {
        tournamentService.deleteTournament(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Join a Tournament.
     *
     * @param tournamentJoinDTO DTO containing tournament creation data.
     * @return ResponseEntity with the new Tournament data and HTTP status.
     */
    @PostMapping("/join")
    public ResponseEntity<?> joinTournamentAsClub(
            @Valid @RequestBody TournamentJoinDTO tournamentJoinDTO,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = true) String token) {
        if (token == null || !token.startsWith(BEARER_PREFIX)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Authorization token is missing or invalid" + token);
        }

        TournamentResponseDTO joinedTournament = null;
        try {
            joinedTournament = tournamentService.joinTournamentAsClub(tournamentJoinDTO, token);
            return new ResponseEntity<>(joinedTournament, HttpStatus.CREATED);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Retrieve all Tournaments.
     *
     * @return ResponseEntity with the list of clubs for a given tournament.
     */
    @GetMapping("/{id}/clubs")
    public ResponseEntity<List<Long>> getClubsInTournament(@PathVariable Long id) {
        List<Long> clubIds = tournamentService.getAllClubsInTournament(id);
        return ResponseEntity.ok(clubIds);
    }

    /**
     * Remove a Club from a Tournament.
     *
     * @param tournamentId ID of the tournament.
     * @param clubId       ID of the club to remove.
     * @param token        Authorization token from the request header.
     * @return ResponseEntity with HTTP status.
     */
    @DeleteMapping("/{tournamentId}/clubs/{clubId}")
    public ResponseEntity<Void> removeClubFromTournament(
            @PathVariable Long tournamentId,
            @PathVariable Long clubId,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token) {

        // if (token == null || !token.startsWith(BEARER_PREFIX)) {
        // return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        // }

        // token = token.substring(7); // Remove BEARER_PREFIX from token
        // Long userIdFromToken = jwtUtil.extractUserId(token);

        // // Ensure the user is authorized (e.g., check if they are the host)
        // if (!tournamentService.isOwnerOfTournament(tournamentId, userIdFromToken)) {
        // return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        // }

        // Call the service method to remove the club
        tournamentService.removeClubFromTournament(tournamentId, clubId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Retrieve Tournaments for a specific Club based on a filter.
     *
     * @param clubId ID of the club.
     * @param filter Tournament filter (e.g., UPCOMING, ONGOING, COMPLETED).
     * @return ResponseEntity with the list of tournaments and HTTP status.
     */
    @GetMapping("/{clubId}/tournaments")
    public ResponseEntity<List<TournamentResponseDTO>> getTournamentsForClub(
            @PathVariable Long clubId,
            @RequestParam TournamentFilter filter) {
        List<TournamentResponseDTO> tournaments = tournamentService.getTournamentsForClub(clubId, filter);
        return ResponseEntity.ok(tournaments);
    }

    // @GetMapping("/player/{playerId}")
    // public ResponseEntity<List<TournamentResponseDTO>> getTournamentsForPlayer(
    // @PathVariable Long playerId,
    // @RequestParam TournamentFilter filter) {
    // List<TournamentResponseDTO> tournaments =
    // tournamentService.getTournamentsForPlayer(playerId, filter);
    // return ResponseEntity.ok(tournaments);
    // }

    /**
     * Update player availability for a Tournament.
     *
     * @param dto DTO containing player availability data.
     * @return ResponseEntity with the updated availability data and HTTP status.
     */
    @PutMapping("/availability")
    public ResponseEntity<?> updatePlayerAvailability(@RequestBody PlayerAvailabilityDTO dto) {

        Long tournamentId = dto.getTournamentId();
        Long playerId = dto.getPlayerId();
        Long clubId = dto.getClubId();
        System.out.println(tournamentId);
        System.out.println(playerId);
        System.out.println(clubId);
        boolean available = dto.isAvailable();
        PlayerAvailabilityDTO playerAvailabilityDTO = new PlayerAvailabilityDTO(tournamentId, playerId, clubId,
                available);
        tournamentService.updatePlayerAvailability(playerAvailabilityDTO);
        return ResponseEntity.ok(tournamentService.updatePlayerAvailability(playerAvailabilityDTO));
    }

    /**
     * Retrieve player availability for a Tournament.
     *
     * @param tournamentId ID of the tournament.
     * @return ResponseEntity with the list of player availability data and HTTP status.
     */
    @GetMapping("/{tournamentId}/availability")
    public ResponseEntity<List<PlayerAvailabilityDTO>> getPlayerAvailability(@PathVariable Long tournamentId) {
        List<PlayerAvailabilityDTO> availabilities = tournamentService.getPlayerAvailabilityForTournament(tournamentId);
        return ResponseEntity.ok(availabilities);
    }

    /**
     * Retrieve Tournaments hosted by a specific Host.
     *
     * @param hostId ID of the host.
     * @return ResponseEntity with the list of tournaments and HTTP status.
     */
    @GetMapping("/host/{hostId}")
    public ResponseEntity<List<Tournament>> getHostedTournaments(@PathVariable Long hostId) {
        List<Tournament> hostedTournaments = tournamentService.getHostedTournaments(hostId);
        return ResponseEntity.ok(hostedTournaments);
    }

    /**
     * Submit verification image for a Tournament that the host wants to host.
     *
     * @param id               ID of the tournament.
     * @param verificationData DTO containing verification image and venue information.
     * @return ResponseEntity with the updated Tournament data and HTTP status.
     */
    @PostMapping("/{id}/verify")
    public ResponseEntity<?> submitVerification(@PathVariable Long id,
            @RequestBody VerificationDataDTO verificationData) {
        try {
            // Use the `confirmationUrl` from `verificationData`
            String[] parts = verificationData.getVerificationImage().split(",");
            byte[] data = Base64.getDecoder().decode(parts[1]); // Skip the data URI prefix
            MultipartFile file = new MockMultipartFile("file", id + "-verificationImage.jpg", "image/jpeg", data);

            String imageUrl = this.amazonClient.uploadFile(file);
            Tournament verifiedTournament = tournamentService.submitVerification(id, imageUrl, verificationData.getVenueBooked());
            return ResponseEntity.ok(verifiedTournament);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Approve verification for a Tournament.
     *
     * @param id ID of the tournament.
     * @return ResponseEntity with the updated Tournament data and HTTP status.
     */
    @PostMapping("/{id}/approve")
    public ResponseEntity<?> approveVerification(@PathVariable Long id) {
        try {
            Tournament approvedTournament = tournamentService.approveVerification(id);
            return ResponseEntity.ok(approvedTournament);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Reject verification for a Tournament.
     *
     * @param id ID of the tournament.
     * @return ResponseEntity with the updated Tournament data and HTTP status.
     */
    @PostMapping("/{id}/reject")
    public ResponseEntity<?> rejectVerification(@PathVariable Long id) {
        try {
            Tournament rejectedTournament = tournamentService.rejectVerification(id);
            return ResponseEntity.ok(rejectedTournament);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Retrieve all Tournaments pending verification.
     *
     * @return ResponseEntity with the list of pending verifications and HTTP status.
     */
    @GetMapping("/pending-verifications")
    public ResponseEntity<?> getPendingVerifications() {
        return ResponseEntity.ok(tournamentService.getPendingVerifications());
    }

    /**
     * Retrieve all Tournaments with approved verification.
     *
     * @return ResponseEntity with the list of approved verifications and HTTP status.
     */
    @GetMapping("/approved-verifications")
    public ResponseEntity<?> getApprovedVerifications() {
        return ResponseEntity.ok(tournamentService.getApprovedVerifications());
    }

    /**
     * Retrieve all Tournaments with rejected verification.
     *
     * @return ResponseEntity with the list of rejected verifications and HTTP status.
     */
    @GetMapping("/rejected-verifications")
    public ResponseEntity<?> getRejectedVerifications() {
        return ResponseEntity.ok(tournamentService.getRejectedVerifications());
    }

    /**
     * Handle Stripe webhook events for payment processing.
     *
     * @param payload   The webhook payload from Stripe.
     * @param sigHeader The Stripe-Signature header.
     * @return ResponseEntity with HTTP status.
     */
    @PostMapping("/webhook")
    public ResponseEntity<?> handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {
        try {
            String endpointSecret = System.getenv("STRIPE_WEBHOOK_SECRET");
            if (endpointSecret == null || endpointSecret.isEmpty()) {
                Dotenv dotenv = Dotenv.load();
                endpointSecret = dotenv.get("STRIPE_WEBHOOK_SECRET");
            }

            if (endpointSecret == null || endpointSecret.isEmpty()) {
                throw new IllegalStateException("Missing Stripe webhook secret");
            }

            Event event = Webhook.constructEvent(payload, sigHeader, endpointSecret);

            if ("checkout.session.completed".equals(event.getType())) {
                // Get the object from the event
                Object stripeObject = event.getDataObjectDeserializer()
                    .deserializeUnsafe();
                    
                if (stripeObject instanceof Session) {
                    Session session = (Session) stripeObject;
                    String tournamentId = session.getClientReferenceId();
// System.out.println("Processing payment for tournament: " + tournamentId);
                    
                    if (tournamentId != null) {
                        try {
                            tournamentService.updateTournamentPaymentStatus(Long.parseLong(tournamentId));
// System.out.println("Successfully updated tournament status");
                            return ResponseEntity.ok().build();
                        } catch (TournamentNotFoundException e) {
// System.err.println("Tournament not found: " + e.getMessage());
                            return ResponseEntity.status(404).body("Tournament not found");
                        } catch (Exception e) {
// System.err.println("Error updating tournament: " + e.getMessage());
                            return ResponseEntity.status(500).body("Error updating tournament");
                        }
                    }
                } else {
// System.err.println("Unexpected object type: " + (stripeObject != null ? stripeObject.getClass().getName() : "null"));
                    return ResponseEntity.status(400).body("Unexpected object type in webhook");
                }
            }

            return ResponseEntity.ok().build();
        } catch (SignatureVerificationException e) {
// System.err.println("Webhook signature verification failed: " + e.getMessage());
            return ResponseEntity.status(400).body("Webhook signature verification failed: " + e.getMessage());
        } catch (Exception e) {
// System.err.println("Webhook error: " + e.getMessage());
            return ResponseEntity.status(400).body("Webhook error: " + e.getMessage());
        }
    }
    
    /**
     * Check the payment status of a Tournament.
     *
     * @param id ID of the tournament.
     * @return ResponseEntity with the payment status and HTTP status.
     */
    @GetMapping("/{id}/payment-status")
    public ResponseEntity<?> checkPaymentStatus(@PathVariable Long id) {
        try {
            Tournament tournament = tournamentService.findById(id);
            if (tournament == null) {
                return ResponseEntity.notFound().build();
            }

            Map<String, Object> response = new HashMap<>();
            response.put("paid", tournament.isVerificationPaid());
            response.put("status", tournament.getVerificationStatus());
            
            return ResponseEntity.ok(response);
        } catch (TournamentNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error checking payment status: " + e.getMessage());
        }
    }
}
