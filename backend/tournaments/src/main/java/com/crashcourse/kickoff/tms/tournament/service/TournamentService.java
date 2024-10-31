package com.crashcourse.kickoff.tms.tournament.service;

import java.util.List;

import com.crashcourse.kickoff.tms.tournament.dto.*;
import com.crashcourse.kickoff.tms.tournament.model.PlayerAvailability;
import com.crashcourse.kickoff.tms.tournament.model.Tournament;
import com.crashcourse.kickoff.tms.tournament.model.TournamentFilter;
import com.crashcourse.kickoff.tms.match.model.Bracket;
import com.crashcourse.kickoff.tms.match.model.Match;
import com.crashcourse.kickoff.tms.match.dto.MatchUpdateDTO;

public interface TournamentService {

    TournamentResponseDTO createTournament(TournamentCreateDTO tournamentCreateDTO, Long userIdFromToken);

    TournamentResponseDTO getTournamentById(Long tournamentId);

    List<TournamentResponseDTO> getAllTournaments();

    TournamentResponseDTO updateTournament(Long tournamentId, TournamentUpdateDTO tournamentUpdateDTO);

    TournamentResponseDTO startTournament(Long tournamentId, String jwtToken);
    
    Match updateMatchInTournament(Long tournamentId, Long matchId, MatchUpdateDTO matchUpdateDTO, String token);

    void deleteTournament(Long tournamentId);

    TournamentResponseDTO joinTournamentAsClub(TournamentJoinDTO tournamentJoinDTO, String token);

    List<Long> getAllClubsInTournament(Long tournamentId);

    void removeClubFromTournament(Long tournamentId, Long clubId);

    boolean isOwnerOfTournament(Long tournamentId, Long profileId);

    List<TournamentResponseDTO> getTournamentsForClub(Long clubId, TournamentFilter filter);

    PlayerAvailability updatePlayerAvailability(PlayerAvailabilityDTO dto);  

    List<PlayerAvailabilityDTO> getPlayerAvailabilityForTournament(Long tournamentId);

    List<Tournament> getHostedTournaments(Long host);

    Tournament submitVerification(Long id, String confirmationUrl);
    Tournament approveVerification(Long tournamentId);
    Tournament rejectVerification(Long tournamentId);
    List<Tournament> getPendingVerifications();
    List<Tournament> getApprovedVerifications();
    List<Tournament> getRejectedVerifications();
}
