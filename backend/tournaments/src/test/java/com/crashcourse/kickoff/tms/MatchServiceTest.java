import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

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

    private Map<String, Double> calculateExpectedRating(
            double R1, double RD1, double R2, double RD2, double S1) {

        // Constants
        double PI = Math.PI;
        double q = Math.log(10) / 400;

        // Function to calculate g(RD)
        Function<Double, Double> g = (RD) -> 1 / Math.sqrt(1 + (3 * Math.pow(q * RD, 2)) / (PI * PI));

        double gRD2 = g.apply(RD2);
        double E1 = 1 / (1 + Math.pow(10, gRD2 * (R2 - R1) / 400));
        double K = 20; // Adjust as needed
        double newR1 = R1 + K * gRD2 * (S1 - E1);
        double dSquared1 = 1 / (Math.pow(q, 2) * Math.pow(gRD2, 2) * E1 * (1 - E1));
        double newRD1 = Math.sqrt(1 / ((1 / Math.pow(RD1, 2)) + (1 / dSquared1)));

        Map<String, Double> result = new HashMap<>();
        result.put("newRating", newR1);
        result.put("newRD", newRD1);
        return result;
    }

    @Test
    public void testUpdateElo_VictoryAgainstHigherRatedOpponent() {
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
        MatchUpdateDTO matchUpdateDTO = new MatchUpdateDTO(true, 1L, 2L, 2, 1, 1L);

        // Act
        matchService.updateElo(matchUpdateDTO, "jwtToken");

        // Calculate expected new rating and RD
        double R1 = 1500;
        double RD1 = 200;
        double R2 = 1600;
        double RD2 = 30;
        double S1 = 1.0; // Club1 wins

        Map<String, Double> expectedResults = calculateExpectedRating(R1, RD1, R2, RD2, S1);
        double expectedNewR1 = expectedResults.get("newRating");
        double expectedNewRD1 = expectedResults.get("newRD");

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
    public void testUpdateElo_VictoryAgainstLowerRatedOpponent() {
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
        MatchUpdateDTO matchUpdateDTO = new MatchUpdateDTO(true, 1L, 2L, 3, 1, 1L);

        // Act
        matchService.updateElo(matchUpdateDTO, "jwtToken");

        // Calculate expected new rating and RD for Club 1
        double R1 = 1600;
        double RD1 = 30;
        double R2 = 1500;
        double RD2 = 200;
        double S1 = 1.0; // Club1 wins

        Map<String, Double> expectedResults = calculateExpectedRating(R1, RD1, R2, RD2, S1);
        double expectedNewR1 = expectedResults.get("newRating");
        double expectedNewRD1 = expectedResults.get("newRD");

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
        MatchUpdateDTO matchUpdateDTO = new MatchUpdateDTO(true, 1L, 2L, 1, 1, 0L); // Assuming 0L for draws

        // Act
        matchService.updateElo(matchUpdateDTO, "jwtToken");

        // Calculate expected new rating and RD
        double R1 = 1500;
        double RD1 = 50;
        double R2 = 1500;
        double RD2 = 50;
        double S1 = 0.5; // Draw

        Map<String, Double> expectedResults = calculateExpectedRating(R1, RD1, R2, RD2, S1);
        double expectedNewR1 = expectedResults.get("newRating");
        double expectedNewRD1 = expectedResults.get("newRD");

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
    public void testUpdateElo_LossAgainstLowerRatedOpponent() {
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
        MatchUpdateDTO matchUpdateDTO = new MatchUpdateDTO(true, 1L, 2L, 1, 2, 2L);

        // Act
        matchService.updateElo(matchUpdateDTO, "jwtToken");

        // Calculate expected new rating and RD for Club 1
        double R1 = 1600;
        double RD1 = 30;
        double R2 = 1500;
        double RD2 = 200;
        double S1 = 0.0; // Club1 loses

        Map<String, Double> expectedResults = calculateExpectedRating(R1, RD1, R2, RD2, S1);
        double expectedNewR1 = expectedResults.get("newRating");
        double expectedNewRD1 = expectedResults.get("newRD");

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
    public void testUpdateElo_HighRatingDeviationImpact() {
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
        MatchUpdateDTO matchUpdateDTO = new MatchUpdateDTO(true, 1L, 2L, 2, 1, 1L);

        // Act
        matchService.updateElo(matchUpdateDTO, "jwtToken");

        // Calculate expected new rating and RD
        double R1 = 1500;
        double RD1 = 300;
        double R2 = 1500;
        double RD2 = 50;
        double S1 = 1.0; // Club1 wins

        Map<String, Double> expectedResults = calculateExpectedRating(R1, RD1, R2, RD2, S1);
        double expectedNewR1 = expectedResults.get("newRating");
        double expectedNewRD1 = expectedResults.get("newRD");

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
        MatchUpdateDTO matchUpdateDTO = new MatchUpdateDTO(true, 1L, 2L, 0, 0, 0L); // Draw

        // Act
        matchService.updateElo(matchUpdateDTO, "jwtToken");

        // Calculate expected new rating and RD
        double R1 = 1500;
        double RD1 = 50;
        double R2 = 1500;
        double RD2 = 50;
        double S1 = 0.5; // Draw

        Map<String, Double> expectedResults = calculateExpectedRating(R1, RD1, R2, RD2, S1);
        double expectedNewR1 = expectedResults.get("newRating");
        double expectedNewRD1 = expectedResults.get("newRD");

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