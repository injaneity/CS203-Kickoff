package com.crashcourse.kickoff.tms.client.exception;

public class ClubProfileNotFoundAtClientException extends RuntimeException {
    public ClubProfileNotFoundAtClientException(Long clubId) {
        super("ClubProfile with ID " + clubId + " was not found.");
    }

    public ClubProfileNotFoundAtClientException(Long clubId, String message) {
        super("ClubProfile with ID " + clubId + " was not found. " + message);
    }
}