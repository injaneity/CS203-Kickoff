package com.crashcourse.kickoff.tms.bracket.repository;

import com.crashcourse.kickoff.tms.bracket.model.Bracket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for Match entities.
 */
@Repository
public interface BracketRepository extends JpaRepository<Bracket, Long> {
    // Add custom query methods if needed
}
