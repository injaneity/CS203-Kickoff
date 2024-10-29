package com.crashcourse.kickoff.tms.client;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.crashcourse.kickoff.tms.club.ClubProfile;
import com.crashcourse.kickoff.tms.security.JwtTokenProvider;

@Component
public class ClubServiceClient {

    private final RestTemplate restTemplate;
    private final JwtTokenProvider jwtTokenProvider;

    /*
     * should this go into .env?
     */
    private String clubUrl = "http://localhost:8082/api/v1/clubs/";
    
    public ClubServiceClient(RestTemplate restTemplate, JwtTokenProvider jwtTokenProvider) {
        if (System.getenv("ALB_URL") != null) {
            clubUrl = System.getenv("ALB_URL");
            clubUrl += "clubs/";
        }
        this.restTemplate = restTemplate;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public ClubProfile getClubProfileById (Long clubId, String token) {
        String url = clubUrl + clubId;
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", jwtTokenProvider.getToken(token));

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        try {
            ResponseEntity<ClubProfile> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    requestEntity,
                    ClubProfile.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return response.getBody();
            } else {
                throw new RuntimeException("Failed to retrieve ClubProfile for ID: " + clubId);
            }
        } catch (HttpClientErrorException e) {
            throw new RuntimeException("Error retrieving ClubProfile for ID: " + clubId + ". Error: " + e.getMessage());
        }
    }

    public void updateClubRating(Long clubId, double newRating, double newRD, String token) {
        String url = clubUrl + clubId + "/rating";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", jwtTokenProvider.getToken(token));
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("rating", newRating);
        requestBody.put("ratingDeviation", newRD);

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Void> response = restTemplate.exchange(
                    url,
                    HttpMethod.PUT,
                    requestEntity,
                    Void.class
            );

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("Failed to update rating for Club ID: " + clubId);
            }
        } catch (HttpClientErrorException e) {
            throw new RuntimeException("Error updating rating for Club ID: " + clubId + ". Error: " + e.getMessage());
        }
    }
}
