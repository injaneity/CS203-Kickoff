// package com.crashcourse.kickoff.tms.client;

// import org.springframework.stereotype.Component;
// import org.springframework.web.client.RestTemplate;

// import com.crashcourse.kickoff.tms.security.JwtTokenProvider;

// @Component
// public class TournamentServiceClient {

//     private final RestTemplate restTemplate;
//     private final JwtTokenProvider jwtTokenProvider;

//     /*
//      * should this go into .env?
//      */
//     private String tournamentUrl = "http://localhost:8080/api/v1/tournaments/";

//     public TournamentServiceClient(RestTemplate restTemplate, JwtTokenProvider jwtTokenProvider) {
//         if (System.getenv("ALB_URL") != null) {
//             tournamentUrl = System.getenv("ALB_URL");
//             tournamentUrl += "tournament/";
//         }
//         this.restTemplate = restTemplate;
//         this.jwtTokenProvider = jwtTokenProvider;
//     }

//     // public ClubProfile getClubProfileById (Long clubId, String token) {
//     //     String url = tournamentUrl + clubId;
//     //     HttpHeaders headers = new HttpHeaders();
//     //     headers.set("Authorization", jwtTokenProvider.getToken(token));

//     //     HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

//     //     try {
//     //         ResponseEntity<ClubProfile> response = restTemplate.exchange(
//     //                 url,
//     //                 HttpMethod.GET,
//     //                 requestEntity,
//     //                 ClubProfile.class
//     //         );

//     //         if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
//     //             return response.getBody();
//     //         } else {
//     //             throw new RuntimeException("Failed to retrieve ClubProfile for ID: " + clubId);
//     //         }
//     //     } catch (HttpClientErrorException e) {
//     //         throw new RuntimeException("Error retrieving ClubProfile for ID: " + clubId + ". Error: " + e.getMessage());
//     //     }
//     // }
// }
