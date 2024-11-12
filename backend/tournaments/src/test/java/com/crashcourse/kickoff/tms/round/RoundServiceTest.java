package com.crashcourse.kickoff.tms.round;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;

import com.crashcourse.kickoff.tms.bracket.model.Match;
import com.crashcourse.kickoff.tms.bracket.model.Round;
import com.crashcourse.kickoff.tms.bracket.service.MatchService;
import com.crashcourse.kickoff.tms.bracket.service.RoundServiceImpl;
import com.crashcourse.kickoff.tms.bracket.repository.RoundRepository;

import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RoundServiceTest {
    @Mock
    private RoundRepository roundRepository;

    @Mock
    private MatchService matchService;

    @InjectMocks
    private RoundServiceImpl roundService;

    // ================= createRound =================
    /**
     * Tests successful creation of a Round when the specified Round exists and matches are created.
     */
    @Test
    void createRound_WithMatches_CreatesRoundAndMatchesSuccessfully() {
        // Arrange
        int numberOfMatches = 3;
        int roundNumber = 1;

        // Initialize a Round object to be saved
        Round savedRound = new Round();
        savedRound.setId(10L);
        savedRound.setRoundNumber(Long.valueOf(roundNumber));

        // Prepare Match objects to be created
        Match match1 = new Match();
        match1.setId(100L);
        match1.setMatchNumber(1L);
        match1.setRound(savedRound);

        Match match2 = new Match();
        match2.setId(101L);
        match2.setMatchNumber(2L);
        match2.setRound(savedRound);

        Match match3 = new Match();
        match3.setId(102L);
        match3.setMatchNumber(3L);
        match3.setRound(savedRound);

        // Mock the creation of Matches via MatchService
        when(matchService.createMatch(savedRound.getId(), 1L)).thenReturn(match1);
        when(matchService.createMatch(savedRound.getId(), 2L)).thenReturn(match2);
        when(matchService.createMatch(savedRound.getId(), 3L)).thenReturn(match3);

        // Mock the second save of Round (with matches)
        Round updatedRound = new Round();
        updatedRound.setId(10L);
        updatedRound.setRoundNumber(Long.valueOf(roundNumber));
        List<Match> matches = List.of(match1, match2, match3);
        updatedRound.setMatches(matches);

        when(roundRepository.save(any(Round.class))).thenReturn(updatedRound);

        // Act
        Round result = roundService.createRound(numberOfMatches, roundNumber);

        // Assert
        assertNotNull(result, "The created Round should not be null.");
        assertEquals(10L, result.getId(), "The Round ID should match the expected value.");
        assertEquals(Long.valueOf(roundNumber), result.getRoundNumber(), "The Round number should be set correctly.");
        assertEquals(3, result.getMatches().size(), "The Round should have three matches.");
        assertTrue(result.getMatches().containsAll(matches), "All created matches should be associated with the Round.");

        // Verify interactions
        verify(roundRepository, times(2)).save(any(Round.class));
        verify(matchService, times(3)).createMatch(eq(savedRound.getId()), anyLong());
    }

    /**
     * Tests successful creation of a Round without any Matches when numberOfMatches is zero.
     */
    @Test
    void createRound_NoMatches_CreatesRoundWithoutMatches() {
        // Arrange
        int numberOfMatches = 0;
        int roundNumber = 2;

        // Initialize a Round object to be saved
        Round savedRound = new Round();
        savedRound.setId(20L);
        savedRound.setRoundNumber(Long.valueOf(roundNumber));

        // Mock the second save of Round (still without matches)
        Round updatedRound = new Round();
        updatedRound.setId(20L);
        updatedRound.setRoundNumber(Long.valueOf(roundNumber));
        updatedRound.setMatches(new ArrayList<>()); // Empty list

        when(roundRepository.save(any(Round.class))).thenReturn(updatedRound);

        // Act
        Round result = roundService.createRound(numberOfMatches, roundNumber);

        // Assert
        assertNotNull(result, "The created Round should not be null.");
        assertEquals(20L, result.getId(), "The Round ID should match the expected value.");
        assertEquals(Long.valueOf(roundNumber), result.getRoundNumber(), "The Round number should be set correctly.");
        assertTrue(result.getMatches().isEmpty(), "The Round should have no matches.");

        // Verify interactions
        verify(roundRepository, times(2)).save(any(Round.class));
        verify(matchService, never()).createMatch(anyLong(), anyLong());
    }

    // ================= Additional Helper Tests (Optional) =================

    @Test
    void createRound_NegativeNumberOfMatches_ThrowsIllegalArgumentException() {
        // Arrange
        int numberOfMatches = -1;
        int roundNumber = 3;

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            roundService.createRound(numberOfMatches, roundNumber);
        }, "Expected createRound to throw IllegalArgumentException for negative number of matches.");

        assertEquals("Number of matches cannot be negative.", exception.getMessage(), "Exception message should match.");

        // Verify interactions
        verify(roundRepository, never()).save(any(Round.class));
        verify(matchService, never()).createMatch(anyLong(), anyLong());
    }

    @Test
    void createRound_RoundRepositorySaveThrowsException_PropagatesException() {
        // Arrange
        int numberOfMatches = 2;
        int roundNumber = 4;

        // Mock RoundRepository to throw an exception on save
        when(roundRepository.save(any(Round.class))).thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            roundService.createRound(numberOfMatches, roundNumber);
        }, "Expected createRound to propagate RuntimeException from RoundRepository.");

        assertEquals("Database error", exception.getMessage(), "Exception message should match.");

        // Verify interactions
        verify(roundRepository, times(1)).save(any(Round.class));
        verify(matchService, never()).createMatch(anyLong(), anyLong());
    }
}