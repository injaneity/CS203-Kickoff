package com.crashcourse.kickoff.tms.bracket.service;
import com.crashcourse.kickoff.tms.bracket.model.Round;

public interface RoundService {
    Round createRound(int numberOfMatches, int roundNumber);
}
