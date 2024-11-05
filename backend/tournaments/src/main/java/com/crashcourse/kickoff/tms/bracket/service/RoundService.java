package com.crashcourse.kickoff.tms.bracket.service;

import java.util.*;

import com.crashcourse.kickoff.tms.bracket.model.Match;
import com.crashcourse.kickoff.tms.bracket.model.Round;
import com.crashcourse.kickoff.tms.bracket.dto.*;

public interface RoundService {
    Round createRound(int numberOfMatches, int roundNumber);
}
