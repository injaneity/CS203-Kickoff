package com.crashcourse.kickoff.tms.bracket.service;

import java.util.*;

import com.crashcourse.kickoff.tms.bracket.model.*;
import com.crashcourse.kickoff.tms.tournament.model.Tournament;
import com.crashcourse.kickoff.tms.bracket.dto.MatchUpdateDTO;

public interface BracketService {
    Bracket createBracket(Long tournamentId, List<Long> joinedClubIds, String jwtToken);
    Match updateMatch(Tournament tournament, Match match, MatchUpdateDTO matchUpdateDTO);
    void seedClubs(Round firstRound, List<Long> clubIds, String jwtToken);
}