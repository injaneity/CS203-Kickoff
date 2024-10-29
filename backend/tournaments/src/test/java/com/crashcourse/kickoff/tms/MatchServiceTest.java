import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.crashcourse.kickoff.tms.client.ClubServiceClient;
import com.crashcourse.kickoff.tms.club.ClubProfile;
import com.crashcourse.kickoff.tms.match.dto.MatchUpdateDTO;
import com.crashcourse.kickoff.tms.match.service.MatchServiceImpl;

public class MatchServiceTest {

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

        // Assert
        verify(clubServiceClient).updateClubRating(eq(1L), anyDouble(), anyDouble(), eq("jwtToken"));
        verify(clubServiceClient).updateClubRating(eq(2L), anyDouble(), anyDouble(), eq("jwtToken"));
    }

    // Additional tests for other scenarios
}