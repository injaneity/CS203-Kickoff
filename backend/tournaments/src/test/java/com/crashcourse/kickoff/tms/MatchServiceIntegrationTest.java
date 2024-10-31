// package com.crashcourse.kickoff.tms;

// import com.crashcourse.kickoff.tms.match.service.MatchServiceImpl;

// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.extension.ExtendWith;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.context.SpringBootTest;
// import org.springframework.boot.test.web.client.TestRestTemplate;
// import org.springframework.test.context.junit.jupiter.SpringExtension;

// import static org.junit.jupiter.api.Assertions.*;

// import java.net.http.HttpHeaders;
// import java.util.HashMap;
// import java.util.Map;
// import java.util.function.Function;

// import org.springframework.boot.test.web.server.LocalServerPort;
// import org.springframework.http.HttpEntity;
// import org.springframework.http.HttpMethod;
// import org.springframework.http.HttpStatus;
// import org.springframework.http.ResponseEntity;

// import com.crashcourse.kickoff.tms.club.ClubProfile;
// import com.crashcourse.kickoff.tms.match.dto.MatchUpdateDTO;
// import com.crashcourse.kickoff.tms.match.service.MatchService;

// @SpringBootTest(classes = KickoffTournamentManagementApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
// @ExtendWith(SpringExtension.class)
// public class MatchServiceIntegrationTest {

//     @LocalServerPort
//     private int port;

//     @Autowired
//     private MatchServiceImpl matchService;

//     @Autowired
//     private TestRestTemplate testRestTemplate;

//     // ClubService base URL (will be set up in the test)
//     private String clubServiceBaseUrl;

//     @BeforeEach
//     public void setUp() {
//         // Assuming ClubService is running on port 8082 or you can configure it to run on a random port as well
//         // For the purpose of this test, we'll assume it's running on port 8082
//         clubServiceBaseUrl = "http://localhost:8082/api/v1/clubs/";
//     }

//     @Test
//     public void testUpdateEloIntegration() {
//         // Arrange
//         String jwtToken = "validJwtToken"; // USE ACTUAL JWT TOKEN HERE!!

//         // Create two clubs in the ClubService
//         Long club1Id = createClubInClubService("Club 1", 1500.0, 200.0, jwtToken);
//         Long club2Id = createClubInClubService("Club 2", 1600.0, 30.0, jwtToken);

//         // Prepare MatchUpdateDTO
//         MatchUpdateDTO matchUpdateDTO = new MatchUpdateDTO(true, club1Id, club2Id, 2, 1, club1Id);

//         // Act
//         matchService.updateElo(matchUpdateDTO, jwtToken);

//         // Fetch updated club profiles from ClubService
//         ClubProfile updatedClub1Profile = getClubProfileFromClubService(club1Id, jwtToken);
//         ClubProfile updatedClub2Profile = getClubProfileFromClubService(club2Id, jwtToken);

//         // Assert
//         assertNotNull(updatedClub1Profile);
//         assertNotNull(updatedClub2Profile);

//         // Calculate expected new rating and RD for Club 1
//         double R1 = 1500.0;
//         double RD1 = 200.0;
//         double R2 = 1600.0;
//         double RD2 = 30.0;
//         double S1 = 1.0; // Club1 wins

//         Map<String, Double> expectedResults = calculateExpectedRating(R1, RD1, R2, RD2, S1);
//         double expectedNewR1 = expectedResults.get("newRating");
//         double expectedNewRD1 = expectedResults.get("newRD");

//         // Verify that the club's ratings have been updated correctly
//         assertEquals(expectedNewR1, updatedClub1Profile.getElo(), 0.01);
//         assertEquals(expectedNewRD1, updatedClub1Profile.getRatingDeviation(), 0.01);
//     }

//     private Long createClubInClubService(String clubName, double elo, double ratingDeviation, String jwtToken) {
//         String url = clubServiceBaseUrl;

//         // Create a new club object
//         Map<String, Object> clubData = new HashMap<>();
//         clubData.put("name", clubName);
//         clubData.put("elo", elo);
//         clubData.put("ratingDeviation", ratingDeviation);

//         // Set headers
//         HttpHeaders headers = new HttpHeaders();
//         headers.setContentType(MediaType.APPLICATION_JSON);
//         headers.setBearerAuth(jwtToken);

//         HttpEntity<Map<String, Object>> request;
//         request = new HttpEntity<>(clubData, headers);

//         // Send POST request to create club
//         ResponseEntity<ClubProfile> response = testRestTemplate.postForEntity(url, request, ClubProfile.class);

//         assertEquals(HttpStatus.CREATED, response.getStatusCode());
//         ClubProfile createdClub = response.getBody();
//         assertNotNull(createdClub);

//         return createdClub.getId();
//     }

//     private ClubProfile getClubProfileFromClubService(Long clubId, String jwtToken) {
//         String url;
//         url = clubServiceBaseUrl + clubId;

//         // Set headers
//         HttpHeaders headers = new HttpHeaders();
//         headers.setBearerAuth(jwtToken);

//         HttpEntity<Void> request = new HttpEntity<>(headers);

//         // Send GET request to fetch club profile
//         ResponseEntity<ClubProfile> response = testRestTemplate.exchange(
//                 url, HttpMethod.GET, request, ClubProfile.class);

//         assertEquals(HttpStatus.OK, response.getStatusCode());
//         return response.getBody();
//     }

//     private Map<String, Double> calculateExpectedRating( double R1, double RD1, double R2, double RD2, double S1) {
//         double PI = Math.PI;
//         double q = Math.log(10) / 400;

//         // Function to calculate g(RD)
//         Function<Double, Double> g = (RD) -> 1 / Math.sqrt(1 + (3 * Math.pow(q * RD, 2)) / (PI * PI));

//         double gRD2 = g.apply(RD2);
//         double E1 = 1 / (1 + Math.pow(10, gRD2 * (R2 - R1) / 400));
//         double K = 20;
//         double newR1 = R1 + K * gRD2 * (S1 - E1);
//         double dSquared1 = 1 / (Math.pow(q, 2) * Math.pow(gRD2, 2) * E1 * (1 - E1));
//         double newRD1 = Math.sqrt(1 / ((1 / Math.pow(RD1, 2)) + (1 / dSquared1)));

//         Map<String, Double> result = new HashMap<>();
//         result.put("newRating", newR1);
//         result.put("newRD", newRD1);
//         return result;
//     }
// }