
package com.crashcourse.kickoff.tms.bracket.repository;

import com.crashcourse.kickoff.tms.bracket.model.Round;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoundRepository extends JpaRepository<Round, Long> {
    Round findRoundByTournamentIdAndRoundNumber(Long tournamentId, Long roundNumber);
}
