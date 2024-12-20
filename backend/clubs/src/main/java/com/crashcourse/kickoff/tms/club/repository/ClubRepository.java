package com.crashcourse.kickoff.tms.club.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.crashcourse.kickoff.tms.club.model.Club;

@Repository
public interface ClubRepository extends JpaRepository<Club, Long> {
    // add query methods (remember that each method's implementation is through the method name)
    Optional<Club> findByName(String name);  

    @Query(value = "SELECT c.* FROM club c JOIN club_players cp ON c.id = cp.club_id WHERE cp.players = :playerId", nativeQuery = true)
    Optional<Club> findClubByPlayerId(@Param("playerId") Long playerId);
}