package com.crashcourse.kickoff.tms.club.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.crashcourse.kickoff.tms.club.model.Club;
import com.crashcourse.kickoff.tms.club.model.PlayerApplication;

public interface PlayerApplicationRepository extends JpaRepository<PlayerApplication, Long> {
    boolean existsByPlayerIdAndClub(Long playerId, Club club);  // Should accept User and Club
    PlayerApplication findByClubIdAndPlayerId(Long clubId, Long playerId);
    void deleteAllByPlayerId(Long playerId);
    List<PlayerApplication> findByPlayerId(Long playerId);
}
