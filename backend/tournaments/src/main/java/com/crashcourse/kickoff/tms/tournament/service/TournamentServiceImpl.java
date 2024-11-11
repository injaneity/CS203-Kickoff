package com.crashcourse.kickoff.tms.tournament.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.crashcourse.kickoff.tms.bracket.dto.MatchUpdateDTO;
import com.crashcourse.kickoff.tms.bracket.model.Bracket;
import com.crashcourse.kickoff.tms.bracket.model.Match;
import com.crashcourse.kickoff.tms.bracket.repository.MatchRepository;
import com.crashcourse.kickoff.tms.bracket.service.BracketService;
import com.crashcourse.kickoff.tms.bracket.service.MatchService;
import com.crashcourse.kickoff.tms.client.ClubServiceClient;
import com.crashcourse.kickoff.tms.client.exception.ClubProfileNotFoundException;
import com.crashcourse.kickoff.tms.club.ClubProfile;
import com.crashcourse.kickoff.tms.location.model.Location;
import com.crashcourse.kickoff.tms.location.repository.LocationRepository;
import com.crashcourse.kickoff.tms.security.JwtTokenProvider;
import com.crashcourse.kickoff.tms.security.JwtUtil;
import com.crashcourse.kickoff.tms.tournament.dto.PlayerAvailabilityDTO;
import com.crashcourse.kickoff.tms.tournament.dto.TournamentCreateDTO;
import com.crashcourse.kickoff.tms.tournament.dto.TournamentJoinDTO;
import com.crashcourse.kickoff.tms.tournament.dto.TournamentResponseDTO;
import com.crashcourse.kickoff.tms.tournament.dto.TournamentUpdateDTO;
import com.crashcourse.kickoff.tms.tournament.exception.BracketAlreadyCreatedException;
import com.crashcourse.kickoff.tms.tournament.exception.ClubAlreadyJoinedException;
import com.crashcourse.kickoff.tms.tournament.exception.InvalidWinningClubException;
import com.crashcourse.kickoff.tms.tournament.exception.TournamentFullException;
import com.crashcourse.kickoff.tms.tournament.exception.TournamentHasNoClubsException;
import com.crashcourse.kickoff.tms.tournament.exception.TournamentNotFoundException;
import com.crashcourse.kickoff.tms.tournament.exception.LocationNotFoundException;
import com.crashcourse.kickoff.tms.tournament.exception.MatchNotFoundException;
import com.crashcourse.kickoff.tms.tournament.model.PlayerAvailability;
import com.crashcourse.kickoff.tms.tournament.model.Tournament;
import com.crashcourse.kickoff.tms.tournament.model.TournamentFilter;
import com.crashcourse.kickoff.tms.tournament.model.TournamentFormat;
import com.crashcourse.kickoff.tms.tournament.repository.PlayerAvailabilityRepository;
import com.crashcourse.kickoff.tms.tournament.repository.TournamentRepository;

import lombok.RequiredArgsConstructor;

/**
 * Implementation of TournamentService.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class TournamentServiceImpl implements TournamentService {

    private final TournamentRepository tournamentRepository;
    private final LocationRepository locationRepository;
    private final MatchRepository matchRepository;
    private final PlayerAvailabilityRepository playerAvailabilityRepository;

    private final BracketService bracketService;
    private final MatchService matchService;

    private final JwtUtil jwtUtil;

    @Autowired
    private final JwtTokenProvider jwtTokenProvider;

    /*
     * Microservice Communication
     */
    private final ClubServiceClient clubServiceClient;

    /**
     * Creates a new Tournament.
     *
     * @param dto             DTO containing tournament creation data.
     * @param userIdFromToken ID of the user creating the tournament, extracted from JWT token.
     * @return TournamentResponseDTO containing the created tournament's data.
     */
    @Override
    public TournamentResponseDTO createTournament(TournamentCreateDTO dto, Long userIdFromToken) {
        Tournament tournament = mapToEntity(dto, userIdFromToken);
        Tournament savedTournament = tournamentRepository.save(tournament);
        return mapToResponseDTO(savedTournament);
    }

    /**
     * Retrieves a Tournament by its ID.
     *
     * @param id ID of the tournament to retrieve.
     * @return TournamentResponseDTO containing the tournament's data.
     * @throws TournamentNotFoundException if the tournament with the given ID does not exist.
     */
    @Override
    @Transactional(readOnly = true)
    public TournamentResponseDTO getTournamentById(Long id) {
        Tournament tournament = tournamentRepository.findById(id)
                .orElseThrow(() -> new TournamentNotFoundException(id));
        return mapToResponseDTO(tournament);
    }

    /**
     * Retrieves all Tournaments.
     *
     * @return List of all Tournament entities.
     */
    @Override
    @Transactional(readOnly = true)
    public List<Tournament> getAllTournaments() {
        return tournamentRepository.findAll();
    }

    /**
     * Updates an existing Tournament.
     *
     * @param id  ID of the tournament to update.
     * @param dto DTO containing updated tournament data.
     * @return TournamentResponseDTO containing the updated tournament's data.
     * @throws TournamentNotFoundException if the tournament with the given ID does not exist.
     * @throws LocationNotFoundException   if the specified location does not exist.
     */
    @Override
    @Transactional
    public TournamentResponseDTO updateTournament(Long id, TournamentUpdateDTO dto) {
        Tournament existingTournament = tournamentRepository.findById(id)
                .orElseThrow(() -> new TournamentNotFoundException(id));

        existingTournament.setName(dto.getName());
        existingTournament.setStartDateTime(dto.getStartDateTime());
        existingTournament.setEndDateTime(dto.getEndDateTime());

        Location location = locationRepository.findById(dto.getLocation().getId())
                .orElseThrow(
                        () -> new LocationNotFoundException(dto.getLocation().getId()));
        existingTournament.setLocation(location);
        location.getTournaments().add(existingTournament);

        existingTournament.setPrizePool(dto.getPrizePool());
        existingTournament.setMinRank(dto.getMinRank());
        existingTournament.setMaxRank(dto.getMaxRank());

        Tournament updatedTournament = tournamentRepository.save(existingTournament);
        return mapToResponseDTO(updatedTournament);
    }

    /**
     * Starts a Tournament by generating its bracket.
     *
     * @param id       ID of the tournament to start.
     * @param jwtToken JWT token for authorization.
     * @return TournamentResponseDTO containing the tournament's data after starting.
     * @throws TournamentNotFoundException      if the tournament with the given ID does not exist.
     * @throws TournamentHasNoClubsException    if the tournament has no clubs joined.
     * @throws BracketAlreadyCreatedException   if the tournament's bracket has already been created.
     */
    public TournamentResponseDTO startTournament(Long id, String jwtToken) {
        Tournament tournament = tournamentRepository.findById(id)
                .orElseThrow(() -> new TournamentNotFoundException(id));
        if (tournament.getJoinedClubIds() == null || tournament.getJoinedClubIds().isEmpty()) {
            throw new TournamentHasNoClubsException(id);
        }
        /*
         * Prevent re-creation of bracket
         */
        if (tournament.getBracket() != null) {
            throw new BracketAlreadyCreatedException(id);
        }

        Bracket bracket = bracketService.createBracket(id, tournament.getJoinedClubIds(), jwtToken);
        tournament.setBracket(bracket);
        Tournament savedTournament = tournamentRepository.save(tournament);
        return mapToResponseDTO(savedTournament);
    }

    /**
     * Updates a Match within a Tournament.
     *
     * @param tournamentId    ID of the tournament containing the match.
     * @param matchId         ID of the match to update.
     * @param matchUpdateDTO  DTO containing match update data.
     * @param jwtToken        JWT token for authorization.
     * @return The updated Match entity.
     * @throws TournamentNotFoundException    if the tournament does not exist.
     * @throws MatchNotFoundException         if the match does not exist.
     * @throws ClubProfileNotFoundException   if one of the clubs does not exist.
     * @throws InvalidWinningClubException    if the winning club ID is invalid.
     */
    public Match updateMatchInTournament(Long tournamentId, Long matchId, MatchUpdateDTO matchUpdateDTO,
            String jwtToken) {

        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new TournamentNotFoundException(tournamentId));

        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new MatchNotFoundException(matchId));

        Long club1Id = matchUpdateDTO.getClub1Id();
        Long club2Id = matchUpdateDTO.getClub2Id();

        /*
         * Validation for Club1 - microservice interaction handled in ClubServiceClient
         */
        ClubProfile clubProfile = clubServiceClient.getClubProfileById(club1Id, jwtToken);
        if (clubProfile == null) {
            throw new ClubProfileNotFoundException(club1Id);
        }

        /*
         * Validation for Club2
         */
        clubProfile = clubServiceClient.getClubProfileById(club2Id, jwtToken);
        if (clubProfile == null) {
            throw new ClubProfileNotFoundException(club2Id);
        }

        /*
         * Validation for Winning Club
         */
        Long winningClubId = matchUpdateDTO.getWinningClubId();
        if (!winningClubId.equals(matchUpdateDTO.getClub1Id()) &&
                !winningClubId.equals(matchUpdateDTO.getClub2Id())) {
            throw new InvalidWinningClubException(winningClubId);
        }

        /*
         * Update Clubs
         */
        match.setClub1Id(matchUpdateDTO.getClub1Id());
        match.setClub2Id(matchUpdateDTO.getClub2Id());

        /*
         * Update Score
         */
        match.setClub1Score(matchUpdateDTO.getClub1Score());
        match.setClub2Score(matchUpdateDTO.getClub2Score());
        match.setWinningClubId(winningClubId);

        /*
         * Update Elo
         */
        matchService.updateElo(matchUpdateDTO, jwtToken);

        Match updatedMatch = bracketService.updateMatch(tournament, match, matchUpdateDTO);
        return updatedMatch;
    }

    /**
     * Deletes a Tournament by its ID.
     *
     * @param id ID of the tournament to delete.
     * @throws TournamentNotFoundException if the tournament with the given ID does not exist.
     */
    @Override
    public void deleteTournament(Long id) {
        if (!tournamentRepository.existsById(id)) {
            throw new TournamentNotFoundException(id);
        }
        tournamentRepository.deleteById(id);
    }

    /**
     * Maps TournamentCreateDTO to Tournament entity.
     *
     * @param dto   TournamentCreateDTO containing tournament creation data.
     * @param host  ID of the host user creating the tournament.
     * @return Tournament entity.
     * @throws LocationNotFoundException if the specified location does not exist.
     */
    private Tournament mapToEntity(TournamentCreateDTO dto, Long host) {
        Tournament tournament = new Tournament();
        tournament.setName(dto.getName());
        tournament.setStartDateTime(dto.getStartDateTime());
        tournament.setEndDateTime(dto.getEndDateTime());

        Location location = locationRepository.findById(dto.getLocation().getId())
                .orElseThrow(
                        () -> new LocationNotFoundException(dto.getLocation().getId()));
        tournament.setLocation(location);
        location.getTournaments().add(tournament);

        tournament.setMaxTeams(dto.getMaxTeams());
        tournament.setTournamentFormat(dto.getTournamentFormat());
        tournament.setKnockoutFormat(dto.getKnockoutFormat());
        tournament.setPrizePool(dto.getPrizePool());
        tournament.setMinRank(dto.getMinRank());
        tournament.setMaxRank(dto.getMaxRank());
        /*
         * If you want i can add a custom exception ,im leaving this for now
         */

        tournament.setHost(host);

        return tournament;
    }

    /**
     * Maps Tournament entity to TournamentResponseDTO.
     *
     * @param tournament Tournament entity.
     * @return TournamentResponseDTO containing tournament data.
     */
    private TournamentResponseDTO mapToResponseDTO(Tournament tournament) {
        TournamentResponseDTO.LocationDTO locationDTO = new TournamentResponseDTO.LocationDTO(
                tournament.getLocation().getId(),
                tournament.getLocation().getName());

        List<Long> clubIds = tournament.getJoinedClubIds().stream().toList();

        return new TournamentResponseDTO(
                tournament.getId(),
                tournament.getName(),
                tournament.isOver(),
                tournament.getStartDateTime(),
                tournament.getEndDateTime(),
                locationDTO,
                tournament.getMaxTeams(),
                tournament.getTournamentFormat().toString(),
                tournament.getKnockoutFormat() != null ? tournament.getKnockoutFormat().toString() : null,
                tournament.getPrizePool(),
                tournament.getMinRank(),
                tournament.getMaxRank(),
                clubIds,
                tournament.getHost(),
                tournament.getVerificationStatus() != null ? tournament.getVerificationStatus().toString() : null,
                tournament.getVenueBooked(),
                tournament.getBracket());
    }

    /**
     * Allows a Club to join a Tournament.
     *
     * @param dto      TournamentJoinDTO containing join data.
     * @param jwtToken JWT token for authorization.
     * @return TournamentResponseDTO containing updated tournament data.
     * @throws TournamentNotFoundException   if the tournament does not exist.
     * @throws ClubAlreadyJoinedException    if the club has already joined the tournament.
     * @throws TournamentFullException       if the tournament has reached its maximum capacity.
     * @throws RuntimeException              for various validation failures.
     */
    @Transactional
    @Override
    public TournamentResponseDTO joinTournamentAsClub(TournamentJoinDTO dto, String jwtToken) {
        Long tournamentId = dto.getTournamentId();
        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new TournamentNotFoundException(tournamentId));

        /*
         * Refer to ClubController.java
         * Now receiving a ClubProfile instead of a list of player IDs
         */
        Long clubId = dto.getClubId();

        if (!clubServiceClient.verifyNoPenaltyStatus(clubId)) {
            throw new RuntimeException(
                    "Club is blacklisted or contains blacklisted players. Unable to join tournmanet.");
        }

        ClubProfile clubProfile = clubServiceClient.getClubProfileById(clubId, jwtToken);
        if (clubProfile == null) {
            throw new RuntimeException("Club profile not found.");
        }

        /*
         * Validate to check if user is Captain of club
         */
        try {
            Long userIdFromToken = jwtUtil.extractUserId(jwtTokenProvider.getToken(jwtToken));
            if (clubProfile.getCaptainId() == null || !clubProfile.getCaptainId().equals(userIdFromToken)) {
                throw new RuntimeException("Only a club captain can join the tournament for the club.");
            }
        } catch (Exception e) {
            System.out.println(e);
        }

        List<Long> players = clubProfile.getPlayers();
        // if (players == null || tournament.getTournamentFormat().getNumberOfPlayers()
        // > players.size()) {
        // throw new NotEnoughPlayersException("Club does not have enough players.");
        // }

        int requiredPlayerCount = tournament.getTournamentFormat() == TournamentFormat.FIVE_SIDE ? 5 : 7;
        long availablePlayerCount = playerAvailabilityRepository
                .findByTournamentIdAndClubIdAndAvailableTrue(tournamentId, clubId)
                .stream().count();

        if (tournament.getJoinedClubIds() != null && tournament.getJoinedClubIds().contains(clubId)) {
            throw new ClubAlreadyJoinedException("Club has already joined the tournament.");
        }

        if (tournament.getJoinedClubIds().size() >= tournament.getMaxTeams()) {
            throw new TournamentFullException("Tournament is already full.");
        }

        /*
         * Validation for elo range
         */
        double elo = clubProfile.getElo();
        if (tournament.getMinRank() != null && elo < tournament.getMinRank()) {
            throw new RuntimeException("Club does not meet tournament minimum elo requirement.");
        }
        if (tournament.getMaxRank() != null && elo > tournament.getMaxRank()) {
            throw new RuntimeException("Club exceeds tournament maximum elo requirement.");
        }

        tournament.getJoinedClubIds().add(clubId);
        Tournament updatedTournament = tournamentRepository.save(tournament);
        return mapToResponseDTO(updatedTournament);
    }

    /**
     * Removes a Club from a Tournament.
     *
     * @param tournamentId ID of the tournament.
     * @param clubId       ID of the club to remove.
     * @throws TournamentNotFoundException if the tournament does not exist.
     * @throws RuntimeException            if the club is not part of the tournament.
     */
    public void removeClubFromTournament(Long tournamentId, Long clubId) {
        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new TournamentNotFoundException(tournamentId));

        // check clubId is part of the tournament
        if (!tournament.getJoinedClubIds().contains(clubId)) {
            throw new RuntimeException("Club is not part of the tournament");
        }

        // Remove the club from the tournament
        tournament.getJoinedClubIds().remove(clubId);

        // Save the tournament after modification
        tournamentRepository.save(tournament);
    }

    /**
     * Retrieves all Club IDs participating in a Tournament.
     *
     * @param id ID of the tournament.
     * @return List of Club IDs.
     * @throws TournamentNotFoundException if the tournament does not exist.
     */
    @Override
    @Transactional(readOnly = true)
    public List<Long> getAllClubsInTournament(Long id) {
        Tournament tournament = tournamentRepository.findById(id)
                .orElseThrow(() -> new TournamentNotFoundException(id));
        return tournament.getJoinedClubIds();
    }

    /**
     * Checks if a user in the claim is indeed is the owner of the Tournament.
     *
     * @param tournamentId ID of the tournament.
     * @param profileId    ID of the user profile.
     * @return True if the user is the owner, false otherwise.
     */
    public boolean isOwnerOfTournament(Long tournamentId, Long profileId) {
        Optional<Tournament> tournamentOpt = tournamentRepository.findById(tournamentId);
        if (tournamentOpt.isPresent()) {
            Tournament tournament = tournamentOpt.get();
            return tournament.getHost().equals(profileId);
        }
        return false;
    }

    /**
     * Retrieves Tournaments for a Club based on a filter.
     *
     * @param clubId ID of the club.
     * @param filter Filter specifying the tournament status (UPCOMING, CURRENT, PAST).
     * @return List of TournamentResponseDTOs.
     */
    @Override
    @Transactional(readOnly = true)
    public List<TournamentResponseDTO> getTournamentsForClub(Long clubId, TournamentFilter filter) {
        List<Tournament> tournaments;

        switch (filter) {
            case UPCOMING:
                tournaments = tournamentRepository.findUpcomingTournamentsForClub(clubId);
                break;
            case CURRENT:
                tournaments = tournamentRepository.findCurrentTournamentsForClub(clubId);
                break;
            case PAST:
                tournaments = tournamentRepository.findPastTournamentsForClub(clubId);
                break;
            default:
                throw new IllegalArgumentException("Invalid filter type");
        }

        return tournaments.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    // @Override
    // @Transactional(readOnly = true)
    // public List<TournamentResponseDTO> getTournamentsForPlayer(Long playerId,
    // TournamentFilter filter) {
    // List<Tournament> tournaments;

    // String clubServiceUrl = "http://localhost:8082/clubs/" + clubId + "/players";

    // JwtUtil help = new JwtUtil();
    // String jwtToken = help.generateJwtToken();

    // HttpHeaders headers = new HttpHeaders();
    // headers.set("Authorization", "Bearer " + jwtToken);
    // HttpEntity<Long> request = new HttpEntity<>(clubId, headers);
    // System.out.println((request));

    // ResponseEntity<List<Long>> response = restTemplate.exchange(
    // clubServiceUrl,
    // HttpMethod.GET,
    // request,
    // new ParameterizedTypeReference<List<Long>>() {}
    // );
    // System.out.println(response);

    // switch (filter) {
    // case UPCOMING:
    // tournaments = tournamentRepository.findUpcomingTournamentsForClub(clubId);
    // break;
    // case CURRENT:
    // tournaments = tournamentRepository.findCurrentTournamentsForClub(clubId);
    // break;
    // case PAST:
    // tournaments = tournamentRepository.findPastTournamentsForClub(clubId);
    // break;
    // default:
    // throw new IllegalArgumentException("Invalid filter type");
    // }

    // return tournaments.stream()
    // .map(this::mapToResponseDTO)
    // .collect(Collectors.toList());
    // }

    /**
     * Updates player availability for a Tournament.
     *
     * @param dto PlayerAvailabilityDTO containing availability data.
     * @return PlayerAvailability entity.
     * @throws RuntimeException if the club ID is null or if saving fails.
     */
    @Override
    public PlayerAvailability updatePlayerAvailability(PlayerAvailabilityDTO dto) {

        Long clubId = dto.getClubId();

        if (clubId == null) {
            throw new RuntimeException("You must join a club before indicating availability.");
        }

        Tournament tournament = tournamentRepository.findById(dto.getTournamentId())
                .orElseThrow(
                        () -> new TournamentNotFoundException(dto.getTournamentId()));

        PlayerAvailability playerAvailability = playerAvailabilityRepository
                .findByTournamentIdAndPlayerId(dto.getTournamentId(), dto.getPlayerId())
                .orElse(new PlayerAvailability());

        playerAvailability.setTournament(tournament);
        playerAvailability.setPlayerId(dto.getPlayerId());
        playerAvailability.setClubId(clubId);
        playerAvailability.setAvailable(dto.isAvailable());

        try {
            playerAvailabilityRepository.save(playerAvailability);
        } catch (Exception e) {
            throw new RuntimeException("Failed to update player availability: " + e.getMessage(), e);
        }

        return playerAvailability;
    }

    /**
     * Retrieves player availability for a Tournament.
     *
     * @param tournamentId ID of the tournament.
     * @return List of PlayerAvailabilityDTOs.
     */
    @Override
    public List<PlayerAvailabilityDTO> getPlayerAvailabilityForTournament(Long tournamentId) {
        List<PlayerAvailability> availabilities = playerAvailabilityRepository.findByTournamentId(tournamentId);
        return availabilities.stream()
                .map(availability -> new PlayerAvailabilityDTO(
                        tournamentId,
                        availability.getPlayerId(),
                        availability.getClubId(),
                        availability.isAvailable()))
                .collect(Collectors.toList());
    }

    /**
     * Retrieves Tournaments hosted by a specific Host.
     *
     * @param host ID of the host.
     * @return List of Tournament entities.
     */
    public List<Tournament> getHostedTournaments(Long host) {
        List<Tournament> hostedTournaments = tournamentRepository.findByHost(host);
        return hostedTournaments;
    }

    /**
     * Submits verification data for a Tournament.
     *
     * @param id             ID of the tournament.
     * @param confirmationUrl URL of the verification image.
     * @param venueBooked    Boolean indicating if the venue is booked.
     * @return Tournament entity after updating verification data.
     * @throws TournamentNotFoundException if the tournament does not exist.
     */
    @Override
    public Tournament submitVerification(Long id, String confirmationUrl, boolean venueBooked) {
        Tournament tournament = tournamentRepository.findById(id)
                .orElseThrow(() -> new TournamentNotFoundException(id));

        tournament.setVerificationImageUrl(confirmationUrl);
        tournament.setVenueBooked(venueBooked);
        tournament.setVerificationStatus(Tournament.VerificationStatus.PENDING);

        return tournamentRepository.save(tournament);
    }

    /**
     * Approves verification for a Tournament.
     *
     * @param id ID of the tournament.
     * @return Tournament entity after approval.
     * @throws TournamentNotFoundException if the tournament does not exist.
     */
    @Override
    public Tournament approveVerification(Long id) {
        Tournament tournament = tournamentRepository.findById(id)
                .orElseThrow(() -> new TournamentNotFoundException(id));

        tournament.setVerificationStatus(Tournament.VerificationStatus.APPROVED);
        Tournament savedTournament = tournamentRepository.save(tournament);
        System.out.println("Approved tournament saved with status: " + savedTournament.getVerificationStatus());
        return savedTournament;
    }

    /**
     * Rejects verification for a Tournament.
     *
     * @param id ID of the tournament.
     * @return Tournament entity after rejection.
     * @throws TournamentNotFoundException if the tournament does not exist.
     */
    @Override
    public Tournament rejectVerification(Long id) {
        Tournament tournament = tournamentRepository.findById(id)
                .orElseThrow(() -> new TournamentNotFoundException(id));

        tournament.setVerificationStatus(Tournament.VerificationStatus.REJECTED);
        return tournamentRepository.save(tournament);
    }

    /**
     * Retrieves all Tournaments pending verification.
     *
     * @return List of Tournament entities with pending verification status.
     */
    @Override
    public List<Tournament> getPendingVerifications() {
        return tournamentRepository.findByVerificationStatus(Tournament.VerificationStatus.PENDING);
    }

    /**
     * Retrieves all Tournaments with approved verification.
     *
     * @return List of Tournament entities with approved verification status.
     */
    @Override
    public List<Tournament> getApprovedVerifications() {
        return tournamentRepository.findByVerificationStatus(Tournament.VerificationStatus.APPROVED);
    }

    /**
     * Retrieves all Tournaments with rejected verification.
     *
     * @return List of Tournament entities with rejected verification status.
     */
    @Override
    public List<Tournament> getRejectedVerifications() {
        return tournamentRepository.findByVerificationStatus(Tournament.VerificationStatus.REJECTED);
    }

    /**
     * Retrieves a Tournament by its ID.
     *
     * @param id ID of the tournament.
     * @return Tournament entity.
     * @throws TournamentNotFoundException if the tournament does not exist.
     */
    @Override
    public Tournament findById(Long id) {
        return tournamentRepository.findById(id)
            .orElseThrow(() -> new TournamentNotFoundException(id));
    }
    
    /**
     * Updates the payment status of a Tournament after successful payment.
     *
     * @param tournamentId ID of the tournament.
     * @throws TournamentNotFoundException if the tournament does not exist.
     */
    @Override
    public void updateTournamentPaymentStatus(Long tournamentId) {
        Tournament tournament = tournamentRepository.findById(tournamentId)
            .orElseThrow(() -> new TournamentNotFoundException(tournamentId));

        tournament.setVerificationPaid(true);
        tournament.setVerificationStatus(Tournament.VerificationStatus.PAYMENT_COMPLETED);

        tournamentRepository.save(tournament);
    }
}
