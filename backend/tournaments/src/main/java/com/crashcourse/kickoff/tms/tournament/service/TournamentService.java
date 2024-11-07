package com.crashcourse.kickoff.tms.tournament.service;

import java.util.List;

import com.crashcourse.kickoff.tms.bracket.dto.MatchUpdateDTO;
import com.crashcourse.kickoff.tms.bracket.model.Match;
import com.crashcourse.kickoff.tms.tournament.dto.PlayerAvailabilityDTO;
import com.crashcourse.kickoff.tms.tournament.dto.TournamentCreateDTO;
import com.crashcourse.kickoff.tms.tournament.dto.TournamentJoinDTO;
import com.crashcourse.kickoff.tms.tournament.dto.TournamentResponseDTO;
import com.crashcourse.kickoff.tms.tournament.dto.TournamentUpdateDTO;
import com.crashcourse.kickoff.tms.tournament.model.PlayerAvailability;
import com.crashcourse.kickoff.tms.tournament.model.Tournament;
import com.crashcourse.kickoff.tms.tournament.model.TournamentFilter;

public interface TournamentService {

    TournamentResponseDTO createTournament(TournamentCreateDTO tournamentCreateDTO, Long userIdFromToken);

    TournamentResponseDTO getTournamentById(Long tournamentId);

    List<Tournament> getAllTournaments();

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
    Tournament findById(Long id);
}
