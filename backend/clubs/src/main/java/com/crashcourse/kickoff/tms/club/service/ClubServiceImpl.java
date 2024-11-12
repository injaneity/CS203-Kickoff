package com.crashcourse.kickoff.tms.club.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.crashcourse.kickoff.tms.club.dto.ClubRatingUpdateDTO;
import com.crashcourse.kickoff.tms.club.dto.PlayerApplicationDTO;
import com.crashcourse.kickoff.tms.club.exception.ClubAlreadyExistsException;
import com.crashcourse.kickoff.tms.club.exception.ClubNotFoundException;
import com.crashcourse.kickoff.tms.club.exception.PenaltyNotFoundException;
import com.crashcourse.kickoff.tms.club.exception.PlayerAlreadyAppliedException;
import com.crashcourse.kickoff.tms.club.exception.PlayerAlreadyInClubException;
import com.crashcourse.kickoff.tms.club.exception.PlayerLimitExceededException;
import com.crashcourse.kickoff.tms.club.model.ApplicationStatus;
import com.crashcourse.kickoff.tms.club.model.Club;
import com.crashcourse.kickoff.tms.club.model.ClubInvitation;
import com.crashcourse.kickoff.tms.club.model.ClubPenaltyStatus;
import com.crashcourse.kickoff.tms.club.model.ClubPenaltyStatus.PenaltyType;
import com.crashcourse.kickoff.tms.club.model.ClubProfile;
import com.crashcourse.kickoff.tms.club.model.PlayerApplication;
import com.crashcourse.kickoff.tms.club.repository.ClubInvitationRepository;
import com.crashcourse.kickoff.tms.club.repository.ClubRepository;
import com.crashcourse.kickoff.tms.club.repository.PlayerApplicationRepository;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ClubServiceImpl implements ClubService {

    private final ClubRepository clubRepository;
    private final PlayerApplicationRepository applicationRepository;
    private final ClubInvitationRepository clubInvitationRepository;

    /**
     * Create a new Club.
     *
     * @param club      Club entity containing club data.
     * @param creatorId ID of the player creating the club.
     * @return The created Club entity.
     * @throws Exception If the club name already exists or the player limit is exceeded.
     */
    @Transactional
    public Club createClub(@Valid Club club, Long creatorId) throws Exception {
        // // Find the PlayerProfile by ID
        // PlayerProfile creator = playerProfileRepository.findById(creatorId)
        // .orElseThrow(() -> new RuntimeException("PlayerProfile not found"));

        // club name not unique
        if (clubRepository.findByName(club.getName()).isPresent()) {
            throw new ClubAlreadyExistsException("Club name must be unique");
        }

        // set the player who created the club as the captain
        club.setCaptainId(creatorId);
        club.setPenaltyStatus(new ClubPenaltyStatus(null, PenaltyType.NONE));
        clubRepository.save(club);

        // player count exceeds the limit
        if (club.getPlayers().size() > Club.MAX_PLAYERS_IN_CLUB) {
            throw new PlayerLimitExceededException(Club.MAX_PLAYERS_IN_CLUB);
        }

        club = addPlayerToClub(club.getId(), creatorId);
        return clubRepository.save(club);
    }

    /**
     * Retrieve all Clubs.
     *
     * @return List of all Club entities.
     */
    public List<Club> getAllClubs() {
        return clubRepository.findAll();
    }

    /**
     * Retrieve a Club by its ID.
     *
     * @param id ID of the Club.
     * @return Optional containing the Club if found.
     * @throws ClubNotFoundException If the club is not found.
     */
    public Optional<Club> getClubById(Long id) {
        Optional<Club> club = clubRepository.findById(id);
        if (!club.isPresent()) {
            throw new ClubNotFoundException(id);
        }
        return club;
    }

    /**
     * Retrieve Clubs by a list of IDs.
     *
     * @param clubIds List of Club IDs.
     * @return List of Club entities.
     * @throws ClubNotFoundException If one or more clubs are not found.
     */
    public List<Club> getClubsByIds(@Positive(message = "Club ID must be positive") List<Long> clubIds) {
        List<Club> clubs = clubRepository.findAllById(clubIds);
        if (clubs.size() != clubIds.size()) {
            throw new ClubNotFoundException("One or more clubs not found for the provided IDs.");
        }
        return clubs;
    }

    /**
     * Retrieve a Club by a Player's ID.
     *
     * @param playerId ID of the player.
     * @return Optional containing the Club if found.
     */
    public Optional<Club> getClubByPlayerId(Long playerId) {
        return clubRepository.findClubByPlayerId(playerId);
    }

    /**
     * Update an existing Club.
     *
     * @param id                ID of the club to update.
     * @param clubDescription   Updated club description.
     * @return The updated Club entity.
     * @throws ClubNotFoundException       If the club is not found.
     */
    public Club updateClubDescription(Long id,  String clubDescription) {
        Optional<Club> clubOptional = clubRepository.findById(id);
        if (clubOptional.isPresent()) {
            Club club = clubOptional.get();
            club.setClubDescription(clubDescription);

            return clubRepository.save(club);
        }

        // no such club to update
        throw new ClubNotFoundException(id);
    }

    /**
     * Delete a Club by its ID.
     *
     * @param id ID of the club to delete.
     * @throws ClubNotFoundException If the club is not found.
     */
    public void deleteClub(Long id) {
        if (!clubRepository.existsById(id)) {
            throw new ClubNotFoundException(id);
        }
        clubRepository.deleteById(id);
    }

    /**
     * PLAYER-RELATED METHODS
     */

    /**
     * Add a player to a Club.
     *
     * @param clubId   ID of the club.
     * @param playerId ID of the player to add.
     * @return The updated Club entity.
     * @throws Exception If the club is full or the player is already a member.
     */
    public Club addPlayerToClub(Long clubId, Long playerId) throws Exception {
        // PlayerProfile player = playerProfileRepository.findById(playerId)
        // .orElseThrow(() -> new RuntimeException("PlayerProfile not found"));

        Club newClub = clubRepository.findById(clubId)
                .orElseThrow(() -> new ClubNotFoundException(clubId));

        if (newClub.getPlayers().size() >= Club.MAX_PLAYERS_IN_CLUB) {
            throw new PlayerLimitExceededException(Club.MAX_PLAYERS_IN_CLUB);
        }

        if (getClubByPlayerId(playerId).isPresent()) {
            throw new PlayerAlreadyInClubException("Player is already a member of a club");
        }

        if (newClub.getPlayers().contains(playerId)) {
            throw new PlayerAlreadyInClubException("Player is already a member of this club");
        }

        List<Club> allClubs = clubRepository.findAll();
        for (Club club : allClubs) {
            PlayerApplication playerApplication = applicationRepository.findByClubIdAndPlayerId(club.getId(), playerId);
            if (playerApplication == null) {
                continue;
            }
            club.getApplicants().remove(playerApplication.getId());
        }

        applicationRepository.deleteAllByPlayerId(playerId);

        newClub.getPlayers().add(playerId);

        clubRepository.save(newClub);
        return newClub;
    }

    /**
     * Retrieve all players in a Club.
     *
     * @param clubId ID of the club.
     * @return List of player IDs.
     * @throws ClubNotFoundException If the club is not found.
     */
    @Override
    public List<Long> getPlayers(Long clubId) {
        Optional<Club> clubOptional = clubRepository.findById(clubId);
        if (!clubOptional.isPresent()) {
            throw new ClubNotFoundException(clubId);
        }

        Club club = clubOptional.get();
        return club.getPlayers();
    }

    /**
     * Remove a player from a Club.
     *
     * @param clubId   ID of the club.
     * @param playerId ID of the player to remove.
     * @return The updated Club entity.
     * @throws Exception If the player is not a member of the club.
     */
    public Club removePlayerFromClub(Long clubId, Long playerId) throws Exception {

        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new ClubNotFoundException(clubId));

        boolean removed = club.getPlayers().remove(playerId);
        if (!removed) {
            throw new Exception("Player is not a member of this club");
        }

        return clubRepository.save(club);
    }

    /**
     * Transfer captaincy to another player in the Club.
     *
     * @param clubId          ID of the club.
     * @param currentCaptainId ID of the current captain.
     * @param newCaptainId     ID of the new captain.
     * @return The updated Club entity.
     * @throws Exception If the current captain is not authorized or the new captain is not a club member.
     */
    public Club transferCaptaincy(Long clubId, Long currentCaptainId, Long newCaptainId) throws Exception {
        // PlayerProfile currentCaptain =
        // playerProfileRepository.findById(currentCaptainId)
        // .orElseThrow(() -> new RuntimeException("currentCaptain not found"));

        // PlayerProfile newCaptain = playerProfileRepository.findById(newCaptainId)
        // .orElseThrow(() -> new RuntimeException("newCaptain not found"));

        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new ClubNotFoundException(clubId));

        if (!club.getCaptainId().equals(currentCaptainId)) {
            throw new Exception("Only the current captain can transfer the captaincy.");
        }

        if (!club.getPlayers().contains(newCaptainId)) {
            throw new Exception("The new captain must be a player in the club.");
        }

        club.setCaptainId(newCaptainId);
        return clubRepository.save(club);
    }

    /**
     * Check if a player is the captain of a Club.
     *
     * @param clubId   ID of the club.
     * @param playerId ID of the player.
     * @return True if the player is the captain, false otherwise.
     */
    @Override
    public boolean isCaptain(Long clubId, Long playerId) {
        Club club = clubRepository.findById(clubId).orElse(null);
        return club != null && club.getCaptainId().equals(playerId);
    }

    /**
     * INVITATION METHODS (CLUB INVITES PLAYER)
     */

    /**
     * Invite a player to join a Club.
     *
     * @param clubId    ID of the club.
     * @param playerId  ID of the player to invite.
     * @param captainId ID of the captain sending the invite.
     * @return The Club entity.
     * @throws Exception If the captain is not authorized.
     */
    @Override
    public Club invitePlayerToClub(Long clubId, Long playerId, Long captainId) throws Exception {
        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new ClubNotFoundException(clubId));

        if (!club.getCaptainId().equals(captainId)) {
            throw new Exception("Only the club captain can invite players.");
        }

        ClubInvitation invitation = new ClubInvitation();
        invitation.setClub(club);
        invitation.setPlayerId(playerId);
        invitation.setStatus(ApplicationStatus.PENDING);
        invitation.setInviteSentDate(LocalDateTime.now());

        clubInvitationRepository.save(invitation);

        return club;
    }

    /**
     * Accept an invitation to join a Club.
     *
     * @param playerId ID of the player accepting the invite.
     * @param clubId   ID of the club.
     * @return The updated Club entity.
     * @throws Exception If the club is full.
     */
    public Club acceptInvite(Long playerId, Long clubId) throws Exception {
        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new ClubNotFoundException(clubId));

        if (club.getPlayers().size() >= Club.MAX_PLAYERS_IN_CLUB) {
            throw new PlayerLimitExceededException(Club.MAX_PLAYERS_IN_CLUB);
        }

        club.getPlayers().add(playerId);

        clubRepository.save(club);

        return club;
    }

    /**
     * Retrieve all invitations for a player.
     *
     * @param playerId ID of the player.
     * @return List of ClubInvitation entities.
     * @throws Exception If an error occurs during retrieval.
     */
    @Override
    public List<ClubInvitation> getPlayerInvitations(Long playerId) throws Exception {
        return clubInvitationRepository.findByPlayerIdAndStatus(playerId, ApplicationStatus.PENDING);
    }

    /**
     * APPLICATION METHODS (PLAYER APPLIES TO CLUB)
     */

    /**
     * Apply to join a Club.
     *
     * @param applicationDTO DTO containing player application data.
     * @throws Exception If the club is full or the player has already applied.
     */
    public void applyToClub(PlayerApplicationDTO applicationDTO) throws Exception {

        Long playerId = applicationDTO.getPlayerId();

        Club club = clubRepository.findById(applicationDTO.getClubId())
                .orElseThrow(
                        () -> new ClubNotFoundException(applicationDTO.getClubId()));

        // Check if the club is full
        if (club.getPlayers().size() >= Club.MAX_PLAYERS_IN_CLUB) {
            throw new PlayerLimitExceededException(Club.MAX_PLAYERS_IN_CLUB);
        }

        // Check if the user has already applied to this club
        if (applicationRepository.existsByPlayerIdAndClub(playerId, club)) { // Use 'User' instead of 'PlayerProfile'
            PlayerApplication newPlayerApplication = applicationRepository.findByClubIdAndPlayerId(club.getId(), playerId);
            if (newPlayerApplication != null) {
                // Remove the player application from the repository
                applicationRepository.delete(newPlayerApplication);
            }
        }

        // Create a new PlayerApplication
        PlayerApplication application = new PlayerApplication();
        application.setClub(club);
        application.setPlayerId(playerId);
        application.setDesiredPosition(applicationDTO.getDesiredPosition());
        application.setStatus(ApplicationStatus.PENDING);

        // Save the application
        applicationRepository.save(application);

        /*
         * Add Applicant to Club
         */
        club.getApplicants().add(application.getId());
        clubRepository.save(club);
    }

    /**
     * Retrieve all PlayerApplications by a player's ID.
     *
     * @param playerId ID of the player.
     * @return List of PlayerApplication entities.
     */
    public List<PlayerApplication> getAllApplicationsByPlayerId(Long playerId) {
        return applicationRepository.findByPlayerId(playerId);
    }

    /**
     * Retrieve all player applications for a Club.
     *
     * @param clubId ID of the club.
     * @return List of player IDs who have applied.
     * @throws ClubNotFoundException If the club is not found.
     */
    @Override
    public List<Long> getPlayerApplications(Long clubId) {
        Optional<Club> clubOptional = clubRepository.findById(clubId);
        if (!clubOptional.isPresent()) {
            throw new ClubNotFoundException(clubId);
        }
        Club club = clubOptional.get();

        List<Long> playerIds = new ArrayList<>();

        for (Long applicationId : club.getApplicants()) {
            Optional<PlayerApplication> applicationOptional = applicationRepository.findById(applicationId);
            playerIds.add(applicationOptional.get().getPlayerId());
        }
        return playerIds;
    }

    /**
     * Accept a player's application to join a Club.
     *
     * @param clubId   ID of the club.
     * @param playerId ID of the player.
     * @throws ClubNotFoundException If the club is not found.
     */
    @Transactional
    @Override
    public void acceptApplication(Long clubId, Long playerId) {
        Optional<Club> clubOptional = clubRepository.findById(clubId);
        if (!clubOptional.isPresent()) {
            throw new ClubNotFoundException(clubId);
        }

        if (getClubByPlayerId(playerId).isPresent()) {
            throw new PlayerAlreadyInClubException("Player is already a member of a club");
        }
        
        Club newClub = clubOptional.get();

        /*
         * Find the application
         */
        PlayerApplication newPlayerApplication = applicationRepository.findByClubIdAndPlayerId(clubId, playerId);
        
        /*
         * Add to Players list
         * Remove from applicants list
         */
        newClub.getPlayers().add(playerId);
        newClub.getApplicants().remove(newPlayerApplication.getId());
        
        clubRepository.save(newClub);

        List<Club> allClubs = clubRepository.findAll();
        for (Club club : allClubs) {
            PlayerApplication playerApplication = applicationRepository.findByClubIdAndPlayerId(club.getId(), playerId);
            if (playerApplication == null) {
                continue;
            }
            club.getApplicants().remove(playerApplication.getId());
        }

        applicationRepository.deleteAllByPlayerId(playerId);
    }

    /**
     * Reject a player's application to join a Club.
     *
     * @param clubId   ID of the club.
     * @param playerId ID of the player.
     * @throws ClubNotFoundException If the club is not found.
     */
    @Transactional
    @Override
    public void rejectApplication(Long clubId, Long playerId) {

        Optional<Club> clubOptional = clubRepository.findById(clubId);
        if (!clubOptional.isPresent()) {
            throw new ClubNotFoundException(clubId);
        }
        Club club = clubOptional.get();

        /*
         * Find the application
         */
        PlayerApplication playerApplication = applicationRepository.findByClubIdAndPlayerId(clubId, playerId);

        playerApplication.setStatus(ApplicationStatus.REJECTED);
        applicationRepository.save(playerApplication);
        /*
         * Remove from applicants list
         */
        club.getApplicants().remove(playerApplication.getId());
        clubRepository.save(club);
    }

    /**
     * Allow a player to leave a Club.
     *
     * @param clubId   ID of the club.
     * @param playerId ID of the player.
     * @return The updated Club entity or null if the club is disbanded.
     * @throws Exception If the player is the captain and must transfer captaincy first.
     */
    @Transactional
    @Override
    public Club playerLeaveClub(Long clubId, Long playerId) throws Exception {
        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new ClubNotFoundException(clubId));

        if (club.getCaptainId().equals(playerId)) {
            // If the captain is the only player, disband the club
            if (club.getPlayers().size() == 1) {
                clubRepository.deleteById(clubId);
                return null;
            } else {
                throw new Exception("You must transfer the captaincy before leaving the club.");
            }
        }

        // Remove the player from the club if they are not the captain
        boolean removed = club.getPlayers().remove(playerId);
        if (!removed) {
            throw new Exception("Player is not a member of this club.");
        }

        return clubRepository.save(club);
    }

    /**
     * Update the rating of a Club.
     *
     * @param clubId          ID of the club.
     * @param ratingUpdateDTO DTO containing rating update data.
     * @throws ClubNotFoundException If the club is not found.
     */
    @Override
    public void updateClubRating(Long clubId, ClubRatingUpdateDTO ratingUpdateDTO) {
        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new ClubNotFoundException(clubId));

        club.setElo(ratingUpdateDTO.getRating());
        club.setRatingDeviation(ratingUpdateDTO.getRatingDeviation());

        clubRepository.save(club);
    }

    /**
     * Update the penalty status of a Club.
     *
     * @param clubId    ID of the club.
     * @param newStatus New penalty status to apply.
     * @return Updated ClubProfile entity.
     * @throws ClubNotFoundException If the club is not found.
     * @throws PenaltyNotFoundException If the penalty is not found.
     */
    @Override
    @Transactional
    public ClubProfile updateClubPenaltyStatus(Long clubId, ClubPenaltyStatus newStatus)
            throws ClubNotFoundException, PenaltyNotFoundException {

        // Check if club exists
        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new ClubNotFoundException(clubId));

        switch (newStatus.getPenaltyType()) {
            case PenaltyType.NONE:
                club.getPenaltyStatus().liftPenalty();;
                break;
            case PenaltyType.BLACKLISTED:
                club.getPenaltyStatus().applyPenalty(newStatus);
            default:
                break;
        }
        clubRepository.save(club);
        return new ClubProfile(club);
    }

    /**
     * Retrieve the penalty status of a Club by its ID.
     *
     * @param clubId ID of the club.
     * @return ClubPenaltyStatus entity.
     * @throws ClubNotFoundException If the club is not found.
     */
    @Override
    @Transactional
    public ClubPenaltyStatus getPenaltyStatusByClubId(Long clubId) throws ClubNotFoundException {
        // Check if club exists in the repository
        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new ClubNotFoundException(clubId));

        // Return the penalty status
        return club.getPenaltyStatus();
    }
}