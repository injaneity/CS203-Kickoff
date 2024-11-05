package com.crashcourse.kickoff.tms.bracket.service;

import com.crashcourse.kickoff.tms.bracket.dto.MatchUpdateDTO;
import com.crashcourse.kickoff.tms.bracket.model.Match;

public interface MatchService {
    Match createMatch(Long tournamentId, Long matchNumber);
    Match getMatchById(Long id);
    void updateElo(MatchUpdateDTO matchUpdateDTO, String jwtToken);

    // MatchResponseDTO updateMatch(Long tournamentId, Long matchId, MatchUpdateDTO matchUpdateDTO);
}
