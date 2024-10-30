import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.crashcourse.kickoff.tms.client.ClubServiceClient;
import com.crashcourse.kickoff.tms.club.ClubProfile;
import com.crashcourse.kickoff.tms.match.dto.MatchUpdateDTO;
import com.crashcourse.kickoff.tms.match.service.MatchServiceImpl;

public class MatchServiceTest {
    private static double adjustedScore(int scoreDifference, int k) {
        // inspired by sigmoid with int k set by us
        return 1 / (1 + Math.exp(-(scoreDifference - k)));
    }

    // forced to take in winningClub param to know which club won in a draw (penalty, etc) -- but will affect less elo
    private static double[] calculateExpectedRating
    (double R1, double R2, double RD1, double RD2, int club1Score, 
    int club2Score, Long club1Id, Long club2Id, Long winningClubId) {
        // define constants for glicko-like rating calcs (but factoring in score difference later on)
        double K = 30; // sensitivity to elo change
        int k = 0; // sensitivity to score difference -- increasing this makes it less sensitive 
        
        // formula of glicko rating system
        double q = Math.log(10) / 400;
        double gRD2 = 1 / Math.sqrt(1 + (3 * Math.pow(q * RD2, 2)) / Math.pow(Math.PI, 2)); // g is a function you apply on RD2
        double E1 = 1 / (1 + Math.pow(10, gRD2 * (R2 - R1) / 400)); // expected score representation for club 1 -- read glicko formula

        int scoreDifference = club1Score - club2Score;
        if (scoreDifference == 0) {
            if (winningClubId.equals(club1Id)) {
                scoreDifference = 1;
            } else if (winningClubId.equals(club2Id)) {
                scoreDifference = -1;
            }
        }

        double S1 = adjustedScore(scoreDifference, k); // actual score rep for club 1
// System.out.println("scoreDifference: " + scoreDifference + "\tk: " + k);
// System.out.println("S1: " + S1 + "\tE1:" + E1);

        double newR1 = R1 + K * gRD2 * (S1 - E1); // new elo for club 1

        double dSquared1 = 1 / (Math.pow(q, 2) * Math.pow(gRD2, 2) * E1 * (1 - E1));
        double newRD1 = Math.sqrt(1 / ((1 / Math.pow(RD1, 2)) + (1 / dSquared1))); // new rating deviation for club 1

        return new double[]{newR1, newRD1};
    }

    @Test
    public void testUpdateElo_BigVictoryAgainstHigherRatedOpponent() {
        // Arrange
        ClubServiceClient clubServiceClient = mock(ClubServiceClient.class);
        MatchServiceImpl matchService = new MatchServiceImpl(clubServiceClient);

        // Mock club profiles
        ClubProfile club1Profile = new ClubProfile();
        club1Profile.setId(1L);
        club1Profile.setElo(1500);
        club1Profile.setRatingDeviation(200);

        ClubProfile club2Profile = new ClubProfile();
        club2Profile.setId(2L);
        club2Profile.setElo(1600);
        club2Profile.setRatingDeviation(30);

        when(clubServiceClient.getClubProfileById(1L, "jwtToken")).thenReturn(club1Profile);
        when(clubServiceClient.getClubProfileById(2L, "jwtToken")).thenReturn(club2Profile);

        // Prepare MatchUpdateDTO
        MatchUpdateDTO matchUpdateDTO = new MatchUpdateDTO(true, 1L, 2L, 5, 1, 1L);

        // Act
System.out.println("testUpdateElo_BigVictoryAgainstHigherRatedOpponent");
        matchService.updateElo(matchUpdateDTO, "jwtToken");

        // Calculate expected new rating and RD
        double R1 = 1500;
        double RD1 = 200;
        double R2 = 1600;
        double RD2 = 30;
        int club1Score = matchUpdateDTO.getClub1Score();
        int club2Score = matchUpdateDTO.getClub2Score(); 

        double[] expectedResults = calculateExpectedRating(R1, R2, RD1, RD2, club1Score, club2Score, club1Profile.getId(), club2Profile.getId(), matchUpdateDTO.getWinningClubId());
        double expectedNewR1 = expectedResults[0];
        double expectedNewRD1 = expectedResults[1];

        // Capture the arguments to verify ELO change
        ArgumentCaptor<Double> club1RatingCaptor = ArgumentCaptor.forClass(Double.class);
        ArgumentCaptor<Double> club1RDCaptor = ArgumentCaptor.forClass(Double.class);
        verify(clubServiceClient).updateClubRating(eq(1L), club1RatingCaptor.capture(), club1RDCaptor.capture(), eq("jwtToken"));

        double newR1 = club1RatingCaptor.getValue();
        double newRD1 = club1RDCaptor.getValue();

        // Assert
        assertEquals(expectedNewR1, newR1, 0.01);
        assertEquals(expectedNewRD1, newRD1, 0.01);
    }

    @Test
    public void testUpdateElo_SmallVictoryAgainstLowerRatedOpponent() {
        // Arrange
        ClubServiceClient clubServiceClient = mock(ClubServiceClient.class);
        MatchServiceImpl matchService = new MatchServiceImpl(clubServiceClient);

        // Mock club profiles
        ClubProfile club1Profile = new ClubProfile();
        club1Profile.setId(1L);
        club1Profile.setElo(1600);
        club1Profile.setRatingDeviation(30);

        ClubProfile club2Profile = new ClubProfile();
        club2Profile.setId(2L);
        club2Profile.setElo(1500);
        club2Profile.setRatingDeviation(200);

        when(clubServiceClient.getClubProfileById(1L, "jwtToken")).thenReturn(club1Profile);
        when(clubServiceClient.getClubProfileById(2L, "jwtToken")).thenReturn(club2Profile);

        // Prepare MatchUpdateDTO
        MatchUpdateDTO matchUpdateDTO = new MatchUpdateDTO(true, 1L, 2L, 2, 1, 1L);

        // Act
System.out.println("testUpdateElo_SmallVictoryAgainstLowerRatedOpponent");
        matchService.updateElo(matchUpdateDTO, "jwtToken");

        // Calculate expected new rating and RD for Club 1
        double R1 = 1600;
        double RD1 = 30;
        double R2 = 1500;
        double RD2 = 200;
        int club1Score = matchUpdateDTO.getClub1Score();
        int club2Score = matchUpdateDTO.getClub2Score(); 

        double[] expectedResults = calculateExpectedRating(R1, R2, RD1, RD2, club1Score, club2Score, club1Profile.getId(), club2Profile.getId(), matchUpdateDTO.getWinningClubId());
        double expectedNewR1 = expectedResults[0];
        double expectedNewRD1 = expectedResults[1];

        // Capture the arguments to verify ELO change
        ArgumentCaptor<Double> club1RatingCaptor = ArgumentCaptor.forClass(Double.class);
        ArgumentCaptor<Double> club1RDCaptor = ArgumentCaptor.forClass(Double.class);
        verify(clubServiceClient).updateClubRating(eq(1L), club1RatingCaptor.capture(), club1RDCaptor.capture(), eq("jwtToken"));

        double newR1 = club1RatingCaptor.getValue();
        double newRD1 = club1RDCaptor.getValue();

        // Assert
        assertEquals(expectedNewR1, newR1, 0.01);
        assertEquals(expectedNewRD1, newRD1, 0.01);
    }

    @Test
    public void testUpdateElo_DrawAgainstSimilarRatedOpponent() {
        // Arrange
        ClubServiceClient clubServiceClient = mock(ClubServiceClient.class);
        MatchServiceImpl matchService = new MatchServiceImpl(clubServiceClient);

        // Mock club profiles
        ClubProfile club1Profile = new ClubProfile();
        club1Profile.setId(1L);
        club1Profile.setElo(1500);
        club1Profile.setRatingDeviation(50);

        ClubProfile club2Profile = new ClubProfile();
        club2Profile.setId(2L);
        club2Profile.setElo(1500);
        club2Profile.setRatingDeviation(50);

        when(clubServiceClient.getClubProfileById(1L, "jwtToken")).thenReturn(club1Profile);
        when(clubServiceClient.getClubProfileById(2L, "jwtToken")).thenReturn(club2Profile);

        // Prepare MatchUpdateDTO
        MatchUpdateDTO matchUpdateDTO = new MatchUpdateDTO(true, 1L, 2L, 1, 1, 1L);

        // Act
System.out.println("testUpdateElo_DrawAgainstSimilarRatedOpponent");
        matchService.updateElo(matchUpdateDTO, "jwtToken");

        // Calculate expected new rating and RD
        double R1 = 1500;
        double RD1 = 50;
        double R2 = 1500;
        double RD2 = 50;
        int club1Score = matchUpdateDTO.getClub1Score();
        int club2Score = matchUpdateDTO.getClub2Score(); 

        double[] expectedResults = calculateExpectedRating(R1, R2, RD1, RD2, club1Score, club2Score, club1Profile.getId(), club2Profile.getId(), matchUpdateDTO.getWinningClubId());
        double expectedNewR1 = expectedResults[0];
        double expectedNewRD1 = expectedResults[1];

        // Capture the arguments to verify ELO change
        ArgumentCaptor<Double> club1RatingCaptor = ArgumentCaptor.forClass(Double.class);
        ArgumentCaptor<Double> club1RDCaptor = ArgumentCaptor.forClass(Double.class);
        verify(clubServiceClient).updateClubRating(eq(1L), club1RatingCaptor.capture(), club1RDCaptor.capture(), eq("jwtToken"));

        double newR1 = club1RatingCaptor.getValue();
        double newRD1 = club1RDCaptor.getValue();

        // Assert
        assertEquals(expectedNewR1, newR1, 0.01);
        assertEquals(expectedNewRD1, newRD1, 0.01);
    }

    @Test
    public void testUpdateElo_BigLossAgainstLowerRatedOpponent() {
        // Arrange
        ClubServiceClient clubServiceClient = mock(ClubServiceClient.class);
        MatchServiceImpl matchService = new MatchServiceImpl(clubServiceClient);

        // Mock club profiles
        ClubProfile club1Profile = new ClubProfile();
        club1Profile.setId(1L);
        club1Profile.setElo(1600);
        club1Profile.setRatingDeviation(30);

        ClubProfile club2Profile = new ClubProfile();
        club2Profile.setId(2L);
        club2Profile.setElo(1500);
        club2Profile.setRatingDeviation(200);

        when(clubServiceClient.getClubProfileById(1L, "jwtToken")).thenReturn(club1Profile);
        when(clubServiceClient.getClubProfileById(2L, "jwtToken")).thenReturn(club2Profile);

        // Prepare MatchUpdateDTO
        MatchUpdateDTO matchUpdateDTO = new MatchUpdateDTO(true, 1L, 2L, 0, 5, 2L);

        // Act
System.out.println("testUpdateElo_BigLossAgainstLowerRatedOpponent");
        matchService.updateElo(matchUpdateDTO, "jwtToken");

        // Calculate expected new rating and RD for Club 1
        double R1 = 1600;
        double RD1 = 30;
        double R2 = 1500;
        double RD2 = 200;
        int club1Score = matchUpdateDTO.getClub1Score();
        int club2Score = matchUpdateDTO.getClub2Score(); 

        double[] expectedResults = calculateExpectedRating(R1, R2, RD1, RD2, club1Score, club2Score, club1Profile.getId(), club2Profile.getId(), matchUpdateDTO.getWinningClubId());
        double expectedNewR1 = expectedResults[0];
        double expectedNewRD1 = expectedResults[1];

        // Capture the arguments to verify ELO change
        ArgumentCaptor<Double> club1RatingCaptor = ArgumentCaptor.forClass(Double.class);
        ArgumentCaptor<Double> club1RDCaptor = ArgumentCaptor.forClass(Double.class);
        verify(clubServiceClient).updateClubRating(eq(1L), club1RatingCaptor.capture(), club1RDCaptor.capture(), eq("jwtToken"));

        double newR1 = club1RatingCaptor.getValue();
        double newRD1 = club1RDCaptor.getValue();

        // Assert
        assertEquals(expectedNewR1, newR1, 0.01);
        assertEquals(expectedNewRD1, newRD1, 0.01);
    }

    @Test
    public void testUpdateElo_BigWinHighRatingDeviationImpact() {
        // Arrange
        ClubServiceClient clubServiceClient = mock(ClubServiceClient.class);
        MatchServiceImpl matchService = new MatchServiceImpl(clubServiceClient);

        // Club with high RD vs. low RD
        ClubProfile club1Profile = new ClubProfile();
        club1Profile.setId(1L);
        club1Profile.setElo(1500);
        club1Profile.setRatingDeviation(300); // High RD

        ClubProfile club2Profile = new ClubProfile();
        club2Profile.setId(2L);
        club2Profile.setElo(1500);
        club2Profile.setRatingDeviation(50); // Low RD

        when(clubServiceClient.getClubProfileById(1L, "jwtToken")).thenReturn(club1Profile);
        when(clubServiceClient.getClubProfileById(2L, "jwtToken")).thenReturn(club2Profile);

        // Prepare MatchUpdateDTO
        MatchUpdateDTO matchUpdateDTO = new MatchUpdateDTO(true, 1L, 2L, 9, 1, 1L);

        // Act
System.out.println("testUpdateElo_BigWinHighRatingDeviationImpact");
        matchService.updateElo(matchUpdateDTO, "jwtToken");

        // Calculate expected new rating and RD
        double R1 = 1500;
        double RD1 = 300;
        double R2 = 1500;
        double RD2 = 50;
        int club1Score = matchUpdateDTO.getClub1Score();
        int club2Score = matchUpdateDTO.getClub2Score(); 

        double[] expectedResults = calculateExpectedRating(R1, R2, RD1, RD2, club1Score, club2Score, club1Profile.getId(), club2Profile.getId(), matchUpdateDTO.getWinningClubId());
        double expectedNewR1 = expectedResults[0];
        double expectedNewRD1 = expectedResults[1];

        // Capture the arguments to verify ELO change
        ArgumentCaptor<Double> club1RatingCaptor = ArgumentCaptor.forClass(Double.class);
        ArgumentCaptor<Double> club1RDCaptor = ArgumentCaptor.forClass(Double.class);
        verify(clubServiceClient).updateClubRating(eq(1L), club1RatingCaptor.capture(), club1RDCaptor.capture(), eq("jwtToken"));

        double newR1 = club1RatingCaptor.getValue();
        double newRD1 = club1RDCaptor.getValue();

        // Assert
        assertEquals(expectedNewR1, newR1, 0.01);
        assertEquals(expectedNewRD1, newRD1, 0.01);
    }

    @Test
    public void testUpdateElo_NoChangeOnDrawWithEqualRatings() {
        // Arrange
        ClubServiceClient clubServiceClient = mock(ClubServiceClient.class);
        MatchServiceImpl matchService = new MatchServiceImpl(clubServiceClient);

        // Both clubs have the same rating and RD
        ClubProfile club1Profile = new ClubProfile();
        club1Profile.setId(1L);
        club1Profile.setElo(1500);
        club1Profile.setRatingDeviation(50);

        ClubProfile club2Profile = new ClubProfile();
        club2Profile.setId(2L);
        club2Profile.setElo(1500);
        club2Profile.setRatingDeviation(50);

        when(clubServiceClient.getClubProfileById(1L, "jwtToken")).thenReturn(club1Profile);
        when(clubServiceClient.getClubProfileById(2L, "jwtToken")).thenReturn(club2Profile);

        // Prepare MatchUpdateDTO
        MatchUpdateDTO matchUpdateDTO = new MatchUpdateDTO(true, 1L, 2L, 1, 1, 1L);

        // Act
System.out.println("testUpdateElo_NoChangeOnDrawWithEqualRatings");
        matchService.updateElo(matchUpdateDTO, "jwtToken");

        // Calculate expected new rating and RD
        double R1 = 1500;
        double RD1 = 50;
        double R2 = 1500;
        double RD2 = 50;
        int club1Score = matchUpdateDTO.getClub1Score();
        int club2Score = matchUpdateDTO.getClub2Score(); 

        double[] expectedResults = calculateExpectedRating(R1, R2, RD1, RD2, club1Score, club2Score, club1Profile.getId(), club2Profile.getId(), matchUpdateDTO.getWinningClubId());
        double expectedNewR1 = expectedResults[0];
        double expectedNewRD1 = expectedResults[1];

        // Capture the arguments to verify ELO change
        ArgumentCaptor<Double> club1RatingCaptor = ArgumentCaptor.forClass(Double.class);
        ArgumentCaptor<Double> club1RDCaptor = ArgumentCaptor.forClass(Double.class);
        verify(clubServiceClient).updateClubRating(eq(1L), club1RatingCaptor.capture(), club1RDCaptor.capture(), eq("jwtToken"));

        double newR1 = club1RatingCaptor.getValue();
        double newRD1 = club1RDCaptor.getValue();

        // Assert
        assertEquals(expectedNewR1, newR1, 0.01);
        assertEquals(expectedNewRD1, newRD1, 0.01);
    }
}