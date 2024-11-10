package com.crashcourse.kickoff.tms.client;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import com.crashcourse.kickoff.tms.client.exception.ClubProfileNotFoundException;
import com.crashcourse.kickoff.tms.client.exception.ClubRatingUpdateFailedException;
import com.crashcourse.kickoff.tms.client.exception.PenaltyStatusVerificationException;

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

    public ClubProfile getClubProfileById(Long clubId, String token) {
        String url = clubUrl + clubId;
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", jwtTokenProvider.getToken(token));

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        try {
            ResponseEntity<ClubProfile> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    requestEntity,
                    ClubProfile.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return response.getBody();
            } else {
                throw new ClubProfileNotFoundException(clubId);
            }
        } catch (HttpClientErrorException e) {
            throw new ClubProfileNotFoundException(clubId, e.getMessage());
        }
    }

    public void updateClubRating(Long clubId, double newRating, double newRD, String token) {
        String url = clubUrl + clubId + "/rating";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + jwtTokenProvider.getToken(token));
        System.out.println(url);
        System.out.println();
        System.out.println();

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("rating", newRating);
        requestBody.put("ratingDeviation", newRD);

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
        System.out.println(requestEntity);
        try {
            ResponseEntity<Void> response = restTemplate.exchange(
                    url,
                    HttpMethod.PUT,
                    requestEntity,
                    Void.class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new ClubRatingUpdateFailedException(clubId);
            }
        } catch (HttpClientErrorException e) {
            throw new ClubRatingUpdateFailedException(clubId, e.getMessage());
        }
    }

    public boolean verifyNoPenaltyStatus(Long clubId, String token) throws RuntimeException {
        String url = clubUrl + clubId + "/penaltystatus";
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers); // No body needed for GET request

        try {
            ResponseEntity<Boolean> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    requestEntity,
                    Boolean.class);

            // Check if the response is successful and return the boolean value
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return !response.getBody();
            } else {
                throw new PenaltyStatusVerificationException(clubId);
            }
        } catch (HttpClientErrorException e) {
            throw new PenaltyStatusVerificationException(clubId, e.getMessage());
        }
    }
}
