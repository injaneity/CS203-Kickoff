package com.crashcourse.kickoff.tms.club.service;

import java.util.List;
import java.util.Optional;

import com.crashcourse.kickoff.tms.club.dto.ClubRatingUpdateDTO;
import com.crashcourse.kickoff.tms.club.dto.PlayerApplicationDTO;
import com.crashcourse.kickoff.tms.club.exception.ClubNotFoundException;
import com.crashcourse.kickoff.tms.club.exception.PenaltyNotFoundException;
import com.crashcourse.kickoff.tms.club.model.Club;
import com.crashcourse.kickoff.tms.club.model.ClubProfile;
import com.crashcourse.kickoff.tms.club.model.ClubInvitation;
import com.crashcourse.kickoff.tms.club.model.ClubPenaltyStatus;

import jakarta.validation.Valid;

public interface ClubService {

    /**
     * CLUB CRUD METHODS
     */

    Club createClub(@Valid Club club, Long creatorId) throws Exception;

    List<Club> getAllClubs();

    Optional<Club> getClubById(Long id);

    List<Club> getClubsByIds(List<Long> clubIds);

    Optional<Club> getClubByPlayerId(Long playerId);

    Club updateClubDescription(Long id,  String clubDescription);

    void deleteClub(Long id);

    /**
     * PLAYER CRUD METHODS
     */

    Club addPlayerToClub(Long clubId, Long playerId) throws Exception;

    Club invitePlayerToClub(Long clubId, Long playerId, Long captainId) throws Exception;

    List<Long> getPlayers(Long clubId);

    boolean isCaptain(Long clubId, Long playerId);

    Club removePlayerFromClub(Long clubId, Long playerId) throws Exception;

    Club playerLeaveClub(Long clubId, Long playerId) throws Exception;

    Club transferCaptaincy(Long clubId, Long currentCaptainId, Long newCaptainId) throws Exception;

    /**
     * INVITATION METHODS
     */

    Club acceptInvite(Long playerId, Long clubId) throws Exception;

    List<ClubInvitation> getPlayerInvitations(Long playerId) throws Exception;

    /**
     * APPLICATION METHODS
     */
    void applyToClub(PlayerApplicationDTO applicationDTO) throws Exception;

    List<Long> getPlayerApplications(Long clubId) throws Exception;

    void acceptApplication(Long clubId, Long playerId);

    void rejectApplication(Long clubId, Long playerId);

    void updateClubRating(Long clubId, ClubRatingUpdateDTO ratingUpdateDTO);

    ClubProfile updateClubPenaltyStatus(Long clubId, ClubPenaltyStatus newStatus)
            throws ClubNotFoundException, PenaltyNotFoundException;

    public ClubPenaltyStatus getPenaltyStatusByClubId(Long clubId) throws ClubNotFoundException;
}