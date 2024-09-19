package com.crashcourse.kickoff.tms.tournament;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/*
 * Responsible for interactions with database
 * storing all created tournament objects
 */

@Repository
public interface TournamentRepository extends JpaRepository<Tournament, Long> {
}
