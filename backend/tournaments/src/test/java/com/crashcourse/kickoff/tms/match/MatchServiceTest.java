package com.crashcourse.kickoff.tms.match;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import com.crashcourse.kickoff.tms.bracket.dto.MatchUpdateDTO;
import com.crashcourse.kickoff.tms.bracket.model.Match;
import com.crashcourse.kickoff.tms.bracket.model.Round;
import com.crashcourse.kickoff.tms.bracket.repository.MatchRepository;
import com.crashcourse.kickoff.tms.bracket.repository.RoundRepository;
import com.crashcourse.kickoff.tms.bracket.service.MatchServiceImpl;
import com.crashcourse.kickoff.tms.client.ClubServiceClient;
import com.crashcourse.kickoff.tms.club.ClubProfile;

import jakarta.persistence.EntityNotFoundException;

@ExtendWith(MockitoExtension.class)
class MatchServiceTest {

    @Mock
    private ClubServiceClient clubServiceClient;

    @Mock
    private MatchRepository matchRepository;

    @Mock
    private RoundRepository roundRepository;

    @InjectMocks
    private MatchServiceImpl matchService;

    /**
     * Helper method to print the final Elo ratings and rating deviations.
     */
    private void printFinalRatings(String testName, double club1Elo, double club1RD, double club2Elo, double club2RD) {
        System.out.println(testName + " Results:");
        System.out.println("Club 1 New Elo: " + club1Elo + ", New RD: " + club1RD);
        System.out.println("Club 2 New Elo: " + club2Elo + ", New RD: " + club2RD);
        System.out.println("----------------------------------------");
    }

    // ================= createMatch =================
    @Test
    void createMatch_ExistingRound_SavesMatchSuccessfully() {
        // Arrange
        Long roundId = 1L;
        Long matchNumber = 1L;

        Round round = new Round();
        round.setId(roundId);

        Match matchToSave = new Match();
        matchToSave.setMatchNumber(matchNumber);
        matchToSave.setRound(round);

        Match savedMatch = new Match();
        savedMatch.setId(100L);
        savedMatch.setMatchNumber(matchNumber);
        savedMatch.setRound(round);

        when(roundRepository.findById(roundId)).thenReturn(Optional.of(round));
        when(matchRepository.save(any(Match.class))).thenReturn(savedMatch);

        // Act
        Match result = matchService.createMatch(roundId, matchNumber);

        // Assert
        assertNotNull(result, "The saved Match should not be null.");
        assertEquals(100L, result.getId(), "The saved Match should have the correct ID.");
        assertEquals(matchNumber, result.getMatchNumber(), "The Match number should be set correctly.");
        assertEquals(round, result.getRound(), "The Match should be associated with the correct Round.");

        // Verify interactions
        verify(roundRepository, times(1)).findById(roundId);
        verify(matchRepository, times(1)).save(any(Match.class));
    }

    @Test
    void createMatch_NonExistingRound_ThrowsEntityNotFoundException() {
        // Arrange
        Long roundId = 2L;
        Long matchNumber = 2L;

        when(roundRepository.findById(roundId)).thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            matchService.createMatch(roundId, matchNumber);
        }, "Expected createMatch to throw EntityNotFoundException for non-existing Round ID.");

        assertEquals("Round not found with id: " + roundId, exception.getMessage(), "Exception message should match.");

        // Verify interactions
        verify(roundRepository, times(1)).findById(roundId);
        verify(matchRepository, never()).save(any(Match.class));
    }

    @Test
    void testUpdateElo_HighEloClubBeatsLowEloClubMarginally() {
        // Arrange
        ClubProfile highEloClub = new ClubProfile();
        highEloClub.setId(1L);
        highEloClub.setElo(2000);
        highEloClub.setRatingDeviation(50);

        ClubProfile lowEloClub = new ClubProfile();
        lowEloClub.setId(2L);
        lowEloClub.setElo(500);
        lowEloClub.setRatingDeviation(50);

        when(clubServiceClient.getClubProfileById(1L, "jwtToken")).thenReturn(highEloClub);
        when(clubServiceClient.getClubProfileById(2L, "jwtToken")).thenReturn(lowEloClub);

        MatchUpdateDTO matchUpdateDTO = new MatchUpdateDTO(true, 1L, 2L, 2, 0, 1L);

        // Act
        matchService.updateElo(matchUpdateDTO, "jwtToken");

        // Capture Arguments
        ArgumentCaptor<Double> highEloClubRatingCaptor = ArgumentCaptor.forClass(Double.class);
        ArgumentCaptor<Double> highEloClubRDCaptor = ArgumentCaptor.forClass(Double.class);

        ArgumentCaptor<Double> lowEloClubRatingCaptor = ArgumentCaptor.forClass(Double.class);
        ArgumentCaptor<Double> lowEloClubRDCaptor = ArgumentCaptor.forClass(Double.class);

        // Verify interactions
        verify(clubServiceClient).updateClubRating(eq(1L), highEloClubRatingCaptor.capture(), highEloClubRDCaptor.capture(), eq("jwtToken"));
        verify(clubServiceClient).updateClubRating(eq(2L), lowEloClubRatingCaptor.capture(), lowEloClubRDCaptor.capture(), eq("jwtToken"));

        double highEloClubNewElo = highEloClubRatingCaptor.getValue();
        double highEloClubNewRD = highEloClubRDCaptor.getValue();

        double lowEloClubNewElo = lowEloClubRatingCaptor.getValue();
        double lowEloClubNewRD = lowEloClubRDCaptor.getValue();

        // Print the final ratings for verification
        printFinalRatings("testUpdateElo_HighEloClubBeatsLowEloClubMarginally", highEloClubNewElo, highEloClubNewRD, lowEloClubNewElo, lowEloClubNewRD);

        // Determine if the high-rated club's Elo increased or decreased
        boolean highEloClubIncreased = highEloClubNewElo > highEloClub.getElo();

        // Assert
        assertTrue(highEloClubIncreased, "High Elo club's rating should still increase after beating a much lower-rated opponent.");
    }

    @Test
    void testUpdateElo_BigVictoryAgainstHigherRatedOpponent() {
        // Arrange
        ClubProfile underdogClub = new ClubProfile();
        underdogClub.setId(1L);
        underdogClub.setElo(1500);
        underdogClub.setRatingDeviation(200);

        ClubProfile favoriteClub = new ClubProfile();
        favoriteClub.setId(2L);
        favoriteClub.setElo(1600);
        favoriteClub.setRatingDeviation(30);

        when(clubServiceClient.getClubProfileById(1L, "jwtToken")).thenReturn(underdogClub);
        when(clubServiceClient.getClubProfileById(2L, "jwtToken")).thenReturn(favoriteClub);

        MatchUpdateDTO matchUpdateDTO = new MatchUpdateDTO(true, 1L, 2L, 5, 1, 1L);

        // Act
        matchService.updateElo(matchUpdateDTO, "jwtToken");

        // Capture Arguments
        ArgumentCaptor<Double> underdogClubRatingCaptor = ArgumentCaptor.forClass(Double.class);
        ArgumentCaptor<Double> underdogClubRDCaptor = ArgumentCaptor.forClass(Double.class);

        ArgumentCaptor<Double> favoriteClubRatingCaptor = ArgumentCaptor.forClass(Double.class);
        ArgumentCaptor<Double> favoriteClubRDCaptor = ArgumentCaptor.forClass(Double.class);

        // Verify interactions
        verify(clubServiceClient).updateClubRating(eq(1L), underdogClubRatingCaptor.capture(), underdogClubRDCaptor.capture(), eq("jwtToken"));
        verify(clubServiceClient).updateClubRating(eq(2L), favoriteClubRatingCaptor.capture(), favoriteClubRDCaptor.capture(), eq("jwtToken"));

        double underdogNewElo = underdogClubRatingCaptor.getValue();
        double underdogNewRD = underdogClubRDCaptor.getValue();

        double favoriteNewElo = favoriteClubRatingCaptor.getValue();
        double favoriteNewRD = favoriteClubRDCaptor.getValue();

        // Print the final ratings for verification
        printFinalRatings("testUpdateElo_BigVictoryAgainstHigherRatedOpponent", underdogNewElo, underdogNewRD, favoriteNewElo, favoriteNewRD);

        // Assert that the underdog's rating increased
        assertTrue(underdogNewElo > underdogClub.getElo(), "Underdog's rating should increase after a big win against a higher-rated opponent.");
    }

    @Test
    void testUpdateElo_SmallVictoryAgainstLowerRatedOpponent() {
        // Arrange
        ClubProfile favoriteClub = new ClubProfile();
        favoriteClub.setId(1L);
        favoriteClub.setElo(1600);
        favoriteClub.setRatingDeviation(30);

        ClubProfile underdogClub = new ClubProfile();
        underdogClub.setId(2L);
        underdogClub.setElo(1500);
        underdogClub.setRatingDeviation(200);

        when(clubServiceClient.getClubProfileById(1L, "jwtToken")).thenReturn(favoriteClub);
        when(clubServiceClient.getClubProfileById(2L, "jwtToken")).thenReturn(underdogClub);

        MatchUpdateDTO matchUpdateDTO = new MatchUpdateDTO(true, 1L, 2L, 2, 1, 1L);

        // Act
        matchService.updateElo(matchUpdateDTO, "jwtToken");

        // Capture Arguments
        ArgumentCaptor<Double> favoriteClubRatingCaptor = ArgumentCaptor.forClass(Double.class);
        ArgumentCaptor<Double> favoriteClubRDCaptor = ArgumentCaptor.forClass(Double.class);

        ArgumentCaptor<Double> underdogClubRatingCaptor = ArgumentCaptor.forClass(Double.class);
        ArgumentCaptor<Double> underdogClubRDCaptor = ArgumentCaptor.forClass(Double.class);

        // Verify interactions
        verify(clubServiceClient).updateClubRating(eq(1L), favoriteClubRatingCaptor.capture(), favoriteClubRDCaptor.capture(), eq("jwtToken"));
        verify(clubServiceClient).updateClubRating(eq(2L), underdogClubRatingCaptor.capture(), underdogClubRDCaptor.capture(), eq("jwtToken"));

        double favoriteNewElo = favoriteClubRatingCaptor.getValue();
        double favoriteNewRD = favoriteClubRDCaptor.getValue();

        double underdogNewElo = underdogClubRatingCaptor.getValue();
        double underdogNewRD = underdogClubRDCaptor.getValue();

        // Print the final ratings for verification
        printFinalRatings("testUpdateElo_SmallVictoryAgainstLowerRatedOpponent", favoriteNewElo, favoriteNewRD, underdogNewElo, underdogNewRD);

        // Assert that the favorite's rating did not decrease
        assertTrue(favoriteNewElo >= favoriteClub.getElo(), "Favorite's rating should not decrease after winning.");
    }

    @Test
    void testUpdateElo_PenaltyWinAgainstSimilarRatedOpponent() {
        // Arrange
        ClubProfile club1 = new ClubProfile();
        club1.setId(1L);
        club1.setElo(1500);
        club1.setRatingDeviation(50);

        ClubProfile club2 = new ClubProfile();
        club2.setId(2L);
        club2.setElo(1500);
        club2.setRatingDeviation(50);

        when(clubServiceClient.getClubProfileById(1L, "jwtToken")).thenReturn(club1);
        when(clubServiceClient.getClubProfileById(2L, "jwtToken")).thenReturn(club2);

        MatchUpdateDTO matchUpdateDTO = new MatchUpdateDTO(true, 1L, 2L, 1, 1, 1L);

        // Act
        matchService.updateElo(matchUpdateDTO, "jwtToken");

        // Capture Arguments
        ArgumentCaptor<Double> club1RatingCaptor = ArgumentCaptor.forClass(Double.class);
        ArgumentCaptor<Double> club1RDCaptor = ArgumentCaptor.forClass(Double.class);

        ArgumentCaptor<Double> club2RatingCaptor = ArgumentCaptor.forClass(Double.class);
        ArgumentCaptor<Double> club2RDCaptor = ArgumentCaptor.forClass(Double.class);

        // Verify interactions
        verify(clubServiceClient).updateClubRating(eq(1L), club1RatingCaptor.capture(), club1RDCaptor.capture(), eq("jwtToken"));
        verify(clubServiceClient).updateClubRating(eq(2L), club2RatingCaptor.capture(), club2RDCaptor.capture(), eq("jwtToken"));

        double club1NewElo = club1RatingCaptor.getValue();
        double club1NewRD = club1RDCaptor.getValue();

        double club2NewElo = club2RatingCaptor.getValue();
        double club2NewRD = club2RDCaptor.getValue();

        // Print the final ratings for verification
        printFinalRatings("testUpdateElo_PenaltyWinAgainstSimilarRatedOpponent", club1NewElo, club1NewRD, club2NewElo, club2NewRD);

        // Assert that the rating change is minimal
        assertTrue(Math.abs(club1NewElo - club1.getElo()) < 10, "Rating should not change significantly after a penalty win against a similar-rated opponent.");
    }

    @Test
    void testUpdateElo_BigLossAgainstLowerRatedOpponent() {
        // Arrange
        ClubProfile favoriteClub = new ClubProfile();
        favoriteClub.setId(1L);
        favoriteClub.setElo(1600);
        favoriteClub.setRatingDeviation(30);

        ClubProfile underdogClub = new ClubProfile();
        underdogClub.setId(2L);
        underdogClub.setElo(1500);
        underdogClub.setRatingDeviation(200);

        when(clubServiceClient.getClubProfileById(1L, "jwtToken")).thenReturn(favoriteClub);
        when(clubServiceClient.getClubProfileById(2L, "jwtToken")).thenReturn(underdogClub);

        MatchUpdateDTO matchUpdateDTO = new MatchUpdateDTO(true, 1L, 2L, 0, 5, 2L);

        // Act
        matchService.updateElo(matchUpdateDTO, "jwtToken");

        // Capture Arguments
        ArgumentCaptor<Double> favoriteClubRatingCaptor = ArgumentCaptor.forClass(Double.class);
        ArgumentCaptor<Double> favoriteClubRDCaptor = ArgumentCaptor.forClass(Double.class);

        ArgumentCaptor<Double> underdogClubRatingCaptor = ArgumentCaptor.forClass(Double.class);
        ArgumentCaptor<Double> underdogClubRDCaptor = ArgumentCaptor.forClass(Double.class);

        // Verify interactions
        verify(clubServiceClient).updateClubRating(eq(1L), favoriteClubRatingCaptor.capture(), favoriteClubRDCaptor.capture(), eq("jwtToken"));
        verify(clubServiceClient).updateClubRating(eq(2L), underdogClubRatingCaptor.capture(), underdogClubRDCaptor.capture(), eq("jwtToken"));

        double favoriteNewElo = favoriteClubRatingCaptor.getValue();
        double favoriteNewRD = favoriteClubRDCaptor.getValue();

        double underdogNewElo = underdogClubRatingCaptor.getValue();
        double underdogNewRD = underdogClubRDCaptor.getValue();

        // Print the final ratings for verification
        printFinalRatings("testUpdateElo_BigLossAgainstLowerRatedOpponent", favoriteNewElo, favoriteNewRD, underdogNewElo, underdogNewRD);

        // Assert that the favorite's rating decreased
        assertTrue(favoriteNewElo < favoriteClub.getElo(), "Favorite's rating should decrease after a big loss against a lower-rated opponent.");
    }

    @Test
    void testUpdateElo_BigWinHighRatingDeviationImpact() {
        // Arrange
        ClubProfile highRDClub = new ClubProfile();
        highRDClub.setId(1L);
        highRDClub.setElo(1500);
        highRDClub.setRatingDeviation(300); // High RD

        ClubProfile lowRDClub = new ClubProfile();
        lowRDClub.setId(2L);
        lowRDClub.setElo(1500);
        lowRDClub.setRatingDeviation(50); // Low RD

        when(clubServiceClient.getClubProfileById(1L, "jwtToken")).thenReturn(highRDClub);
        when(clubServiceClient.getClubProfileById(2L, "jwtToken")).thenReturn(lowRDClub);

        MatchUpdateDTO matchUpdateDTO = new MatchUpdateDTO(true, 1L, 2L, 9, 1, 1L);

        // Act
        matchService.updateElo(matchUpdateDTO, "jwtToken");

        // Capture Arguments
        ArgumentCaptor<Double> highRDClubRatingCaptor = ArgumentCaptor.forClass(Double.class);
        ArgumentCaptor<Double> highRDClubRDCaptor = ArgumentCaptor.forClass(Double.class);

        ArgumentCaptor<Double> lowRDClubRatingCaptor = ArgumentCaptor.forClass(Double.class);
        ArgumentCaptor<Double> lowRDClubRDCaptor = ArgumentCaptor.forClass(Double.class);

        // Verify interactions
        verify(clubServiceClient).updateClubRating(eq(1L), highRDClubRatingCaptor.capture(), highRDClubRDCaptor.capture(), eq("jwtToken"));
        verify(clubServiceClient).updateClubRating(eq(2L), lowRDClubRatingCaptor.capture(), lowRDClubRDCaptor.capture(), eq("jwtToken"));

        double highRDClubNewElo = highRDClubRatingCaptor.getValue();
        double highRDClubNewRD = highRDClubRDCaptor.getValue();

        double lowRDClubNewElo = lowRDClubRatingCaptor.getValue();
        double lowRDClubNewRD = lowRDClubRDCaptor.getValue();

        // Print the final ratings for verification
        printFinalRatings("testUpdateElo_BigWinHighRatingDeviationImpact", highRDClubNewElo, highRDClubNewRD, lowRDClubNewElo, lowRDClubNewRD);

        // Assert that the high RD club's rating changed significantly
        assertTrue(Math.abs(highRDClubNewElo - highRDClub.getElo()) > 10, "High RD club's rating should change significantly after a big win.");
    }

    @Test
    void testUpdateElo_WinningTeamAlwaysGainsAtLeastOneElo() {
        // Arrange
        ClubProfile highRatedClub = new ClubProfile();
        highRatedClub.setId(1L);
        highRatedClub.setElo(2000);
        highRatedClub.setRatingDeviation(50);

        ClubProfile lowRatedClub = new ClubProfile();
        lowRatedClub.setId(2L);
        lowRatedClub.setElo(1000);
        lowRatedClub.setRatingDeviation(50);

        when(clubServiceClient.getClubProfileById(1L, "jwtToken")).thenReturn(highRatedClub);
        when(clubServiceClient.getClubProfileById(2L, "jwtToken")).thenReturn(lowRatedClub);

        MatchUpdateDTO matchUpdateDTO = new MatchUpdateDTO(true, 1L, 2L, 1, 0, 1L);

        // Act
        matchService.updateElo(matchUpdateDTO, "jwtToken");

        // Capture Arguments
        ArgumentCaptor<Double> highRatedClubEloCaptor = ArgumentCaptor.forClass(Double.class);
        ArgumentCaptor<Double> highRatedClubRDCaptor = ArgumentCaptor.forClass(Double.class);

        // Verify interactions
        verify(clubServiceClient).updateClubRating(eq(1L), highRatedClubEloCaptor.capture(), highRatedClubRDCaptor.capture(), eq("jwtToken"));

        double newElo = highRatedClubEloCaptor.getValue();

        // Assert that the winning team gained at least 1 Elo point
        assertTrue(newElo - highRatedClub.getElo() >= 1.0, "Winning team should gain at least 1 Elo point.");
    }
}