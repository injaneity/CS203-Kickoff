package com.crashcourse.kickoff.tms;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.crashcourse.kickoff.tms.bracket.dto.MatchUpdateDTO;
import com.crashcourse.kickoff.tms.bracket.service.MatchServiceImpl;
import com.crashcourse.kickoff.tms.client.ClubServiceClient;
import com.crashcourse.kickoff.tms.club.ClubProfile;

class MatchServiceTest {

    /**
     * Helper method to print the final Elo ratings and rating deviations.
     */
    private void printFinalRatings(String testName, double club1Elo, double club1RD, double club2Elo, double club2RD) {
        System.out.println(testName + " Results:");
        System.out.println("Club 1 New Elo: " + club1Elo + ", New RD: " + club1RD);
        System.out.println("Club 2 New Elo: " + club2Elo + ", New RD: " + club2RD);
        System.out.println("----------------------------------------");
    }

    /**
     * Tests the Elo rating update when a high-rated club defeats a low-rated club with a score of 2-0.
     * Determines whether the high-rated club's Elo increases or decreases.
     */
    @Test
    void testUpdateElo_HighEloClubBeatsLowEloClubMarginally() {
        // Arrange
        ClubServiceClient clubServiceClient = mock(ClubServiceClient.class);
        MatchServiceImpl matchService = new MatchServiceImpl(clubServiceClient);

        // Mock club profiles
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

        // Prepare MatchUpdateDTO
        MatchUpdateDTO matchUpdateDTO = new MatchUpdateDTO(true, 1L, 2L, 2, 0, 1L);

        // Act
        matchService.updateElo(matchUpdateDTO, "jwtToken");

        // Capture the arguments to verify Elo change for both clubs
        ArgumentCaptor<Double> highEloClubRatingCaptor = ArgumentCaptor.forClass(Double.class);
        ArgumentCaptor<Double> highEloClubRDCaptor = ArgumentCaptor.forClass(Double.class);

        ArgumentCaptor<Double> lowEloClubRatingCaptor = ArgumentCaptor.forClass(Double.class);
        ArgumentCaptor<Double> lowEloClubRDCaptor = ArgumentCaptor.forClass(Double.class);

        // Verify that the clubServiceClient's updateClubRating method was called with the expected parameters
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
        assertEquals(true, highEloClubIncreased, "High Elo club's rating should still increase after beating a much lower-rated opponent.");
    }

    @Test
    void testUpdateElo_BigVictoryAgainstHigherRatedOpponent() {
        // Arrange
        ClubServiceClient clubServiceClient = mock(ClubServiceClient.class);
        MatchServiceImpl matchService = new MatchServiceImpl(clubServiceClient);

        // Mock club profiles
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

        // Prepare MatchUpdateDTO
        MatchUpdateDTO matchUpdateDTO = new MatchUpdateDTO(true, 1L, 2L, 5, 1, 1L);

        // Act
        matchService.updateElo(matchUpdateDTO, "jwtToken");

        // Capture the arguments to verify Elo change for both clubs
        ArgumentCaptor<Double> underdogClubRatingCaptor = ArgumentCaptor.forClass(Double.class);
        ArgumentCaptor<Double> underdogClubRDCaptor = ArgumentCaptor.forClass(Double.class);

        ArgumentCaptor<Double> favoriteClubRatingCaptor = ArgumentCaptor.forClass(Double.class);
        ArgumentCaptor<Double> favoriteClubRDCaptor = ArgumentCaptor.forClass(Double.class);

        verify(clubServiceClient).updateClubRating(eq(1L), underdogClubRatingCaptor.capture(), underdogClubRDCaptor.capture(), eq("jwtToken"));
        verify(clubServiceClient).updateClubRating(eq(2L), favoriteClubRatingCaptor.capture(), favoriteClubRDCaptor.capture(), eq("jwtToken"));

        double underdogNewElo = underdogClubRatingCaptor.getValue();
        double underdogNewRD = underdogClubRDCaptor.getValue();

        double favoriteNewElo = favoriteClubRatingCaptor.getValue();
        double favoriteNewRD = favoriteClubRDCaptor.getValue();

        // Print the final ratings for verification
        printFinalRatings("testUpdateElo_BigVictoryAgainstHigherRatedOpponent", underdogNewElo, underdogNewRD, favoriteNewElo, favoriteNewRD);

        // Assert that the underdog's rating increased
        assertEquals(true, underdogNewElo > underdogClub.getElo(), "Underdog's rating should increase after a big win against a higher-rated opponent.");
    }

    @Test
    void testUpdateElo_SmallVictoryAgainstLowerRatedOpponent() {
        // Arrange
        ClubServiceClient clubServiceClient = mock(ClubServiceClient.class);
        MatchServiceImpl matchService = new MatchServiceImpl(clubServiceClient);

        // Mock club profiles
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

        // Prepare MatchUpdateDTO
        MatchUpdateDTO matchUpdateDTO = new MatchUpdateDTO(true, 1L, 2L, 2, 1, 1L);

        // Act
        matchService.updateElo(matchUpdateDTO, "jwtToken");

        // Capture the arguments to verify Elo change for both clubs
        ArgumentCaptor<Double> favoriteClubRatingCaptor = ArgumentCaptor.forClass(Double.class);
        ArgumentCaptor<Double> favoriteClubRDCaptor = ArgumentCaptor.forClass(Double.class);

        ArgumentCaptor<Double> underdogClubRatingCaptor = ArgumentCaptor.forClass(Double.class);
        ArgumentCaptor<Double> underdogClubRDCaptor = ArgumentCaptor.forClass(Double.class);

        verify(clubServiceClient).updateClubRating(eq(1L), favoriteClubRatingCaptor.capture(), favoriteClubRDCaptor.capture(), eq("jwtToken"));
        verify(clubServiceClient).updateClubRating(eq(2L), underdogClubRatingCaptor.capture(), underdogClubRDCaptor.capture(), eq("jwtToken"));

        double favoriteNewElo = favoriteClubRatingCaptor.getValue();
        double favoriteNewRD = favoriteClubRDCaptor.getValue();

        double underdogNewElo = underdogClubRatingCaptor.getValue();
        double underdogNewRD = underdogClubRDCaptor.getValue();

        // Print the final ratings for verification
        printFinalRatings("testUpdateElo_SmallVictoryAgainstLowerRatedOpponent", favoriteNewElo, favoriteNewRD, underdogNewElo, underdogNewRD);

        // Assert that the favorite's rating did not decrease
        assertEquals(true, favoriteNewElo >= favoriteClub.getElo(), "Favorite's rating should not decrease after winning.");
    }

    @Test
    void testUpdateElo_PenaltyWinAgainstSimilarRatedOpponent() {
        // Arrange
        ClubServiceClient clubServiceClient = mock(ClubServiceClient.class);
        MatchServiceImpl matchService = new MatchServiceImpl(clubServiceClient);

        // Mock club profiles
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

        // Prepare MatchUpdateDTO
        MatchUpdateDTO matchUpdateDTO = new MatchUpdateDTO(true, 1L, 2L, 1, 1, 1L);

        // Act
        matchService.updateElo(matchUpdateDTO, "jwtToken");

        // Capture the arguments to verify Elo change for both clubs
        ArgumentCaptor<Double> club1RatingCaptor = ArgumentCaptor.forClass(Double.class);
        ArgumentCaptor<Double> club1RDCaptor = ArgumentCaptor.forClass(Double.class);

        ArgumentCaptor<Double> club2RatingCaptor = ArgumentCaptor.forClass(Double.class);
        ArgumentCaptor<Double> club2RDCaptor = ArgumentCaptor.forClass(Double.class);

        verify(clubServiceClient).updateClubRating(eq(1L), club1RatingCaptor.capture(), club1RDCaptor.capture(), eq("jwtToken"));
        verify(clubServiceClient).updateClubRating(eq(2L), club2RatingCaptor.capture(), club2RDCaptor.capture(), eq("jwtToken"));

        double club1NewElo = club1RatingCaptor.getValue();
        double club1NewRD = club1RDCaptor.getValue();

        double club2NewElo = club2RatingCaptor.getValue();
        double club2NewRD = club2RDCaptor.getValue();

        // Print the final ratings for verification
        printFinalRatings("testUpdateElo_PenaltyWinAgainstSimilarRatedOpponent", club1NewElo, club1NewRD, club2NewElo, club2NewRD);

        // Assert that the rating change is minimal
        assertEquals(true, Math.abs(club1NewElo - club1.getElo()) < 10, "Rating should not change significantly after a penalty win against a similar-rated opponent.");
    }

    @Test
    void testUpdateElo_BigLossAgainstLowerRatedOpponent() {
        // Arrange
        ClubServiceClient clubServiceClient = mock(ClubServiceClient.class);
        MatchServiceImpl matchService = new MatchServiceImpl(clubServiceClient);

        // Mock club profiles
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

        // Prepare MatchUpdateDTO
        MatchUpdateDTO matchUpdateDTO = new MatchUpdateDTO(true, 1L, 2L, 0, 5, 2L);

        // Act
        matchService.updateElo(matchUpdateDTO, "jwtToken");

        // Capture the arguments to verify Elo change for both clubs
        ArgumentCaptor<Double> favoriteClubRatingCaptor = ArgumentCaptor.forClass(Double.class);
        ArgumentCaptor<Double> favoriteClubRDCaptor = ArgumentCaptor.forClass(Double.class);

        ArgumentCaptor<Double> underdogClubRatingCaptor = ArgumentCaptor.forClass(Double.class);
        ArgumentCaptor<Double> underdogClubRDCaptor = ArgumentCaptor.forClass(Double.class);

        verify(clubServiceClient).updateClubRating(eq(1L), favoriteClubRatingCaptor.capture(), favoriteClubRDCaptor.capture(), eq("jwtToken"));
        verify(clubServiceClient).updateClubRating(eq(2L), underdogClubRatingCaptor.capture(), underdogClubRDCaptor.capture(), eq("jwtToken"));

        double favoriteNewElo = favoriteClubRatingCaptor.getValue();
        double favoriteNewRD = favoriteClubRDCaptor.getValue();

        double underdogNewElo = underdogClubRatingCaptor.getValue();
        double underdogNewRD = underdogClubRDCaptor.getValue();

        // Print the final ratings for verification
        printFinalRatings("testUpdateElo_BigLossAgainstLowerRatedOpponent", favoriteNewElo, favoriteNewRD, underdogNewElo, underdogNewRD);

        // Assert that the favorite's rating decreased
        assertEquals(true, favoriteNewElo < favoriteClub.getElo(), "Favorite's rating should decrease after a big loss against a lower-rated opponent.");
    }

    @Test
    void testUpdateElo_BigWinHighRatingDeviationImpact() {
        // Arrange
        ClubServiceClient clubServiceClient = mock(ClubServiceClient.class);
        MatchServiceImpl matchService = new MatchServiceImpl(clubServiceClient);

        // Club with high RD vs. low RD
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

        // Prepare MatchUpdateDTO
        MatchUpdateDTO matchUpdateDTO = new MatchUpdateDTO(true, 1L, 2L, 9, 1, 1L);

        // Act
        matchService.updateElo(matchUpdateDTO, "jwtToken");

        // Capture the arguments to verify Elo change for both clubs
        ArgumentCaptor<Double> highRDClubRatingCaptor = ArgumentCaptor.forClass(Double.class);
        ArgumentCaptor<Double> highRDClubRDCaptor = ArgumentCaptor.forClass(Double.class);

        ArgumentCaptor<Double> lowRDClubRatingCaptor = ArgumentCaptor.forClass(Double.class);
        ArgumentCaptor<Double> lowRDClubRDCaptor = ArgumentCaptor.forClass(Double.class);

        verify(clubServiceClient).updateClubRating(eq(1L), highRDClubRatingCaptor.capture(), highRDClubRDCaptor.capture(), eq("jwtToken"));
        verify(clubServiceClient).updateClubRating(eq(2L), lowRDClubRatingCaptor.capture(), lowRDClubRDCaptor.capture(), eq("jwtToken"));

        double highRDClubNewElo = highRDClubRatingCaptor.getValue();
        double highRDClubNewRD = highRDClubRDCaptor.getValue();

        double lowRDClubNewElo = lowRDClubRatingCaptor.getValue();
        double lowRDClubNewRD = lowRDClubRDCaptor.getValue();

        // Print the final ratings for verification
        printFinalRatings("testUpdateElo_BigWinHighRatingDeviationImpact", highRDClubNewElo, highRDClubNewRD, lowRDClubNewElo, lowRDClubNewRD);

        // Assert that the high RD club's rating changed significantly
        assertEquals(true, Math.abs(highRDClubNewElo - highRDClub.getElo()) > 10, "High RD club's rating should change significantly after a big win.");
    }

    @Test
    void testUpdateElo_WinningTeamAlwaysGainsAtLeastOneElo() {
        // Arrange
        ClubServiceClient clubServiceClient = mock(ClubServiceClient.class);
        MatchServiceImpl matchService = new MatchServiceImpl(clubServiceClient);

        // High-rated club vs. low-rated club
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

        // Prepare MatchUpdateDTO where the high-rated club wins
        MatchUpdateDTO matchUpdateDTO = new MatchUpdateDTO(true, 1L, 2L, 1, 0, 1L);

        // Act
        matchService.updateElo(matchUpdateDTO, "jwtToken");

        // Capture the updated Elo and RD
        ArgumentCaptor<Double> highRatedClubEloCaptor = ArgumentCaptor.forClass(Double.class);
        ArgumentCaptor<Double> highRatedClubRDCaptor = ArgumentCaptor.forClass(Double.class);

        verify(clubServiceClient).updateClubRating(eq(1L), highRatedClubEloCaptor.capture(), highRatedClubRDCaptor.capture(), eq("jwtToken"));

        double newElo = highRatedClubEloCaptor.getValue();
        double newRD = highRatedClubRDCaptor.getValue();

        printFinalRatings("testUpdateElo_WinningTeamAlwaysGainsAtLeastOneElo", newElo, newRD, 0, 0);
        // Assert that the high-rated club gained at least 1 Elo point
        assertEquals(true, newElo - highRatedClub.getElo() >= 1.0, "Winning team should gain at least 1 Elo point.");


        // Assert that the RD decreased by at least 0.5 and is not below 30
        assertEquals(true, highRatedClub.getRatingDeviation() - newRD >= 0.5, "Winning team's RD should decrease by at least 0.5.");
        assertEquals(true, newRD >= 30.0, "RD should not drop below 30.");
    }
}