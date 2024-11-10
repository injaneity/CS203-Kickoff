package com.crashcourse.kickoff.tms.client.exception;

public class ClubProfileNotFoundException extends RuntimeException {
    public ClubProfileNotFoundException(Long clubId) {
        super("ClubProfile with ID " + clubId + " was not found.");
    }

    public ClubProfileNotFoundException(Long clubId, String message) {
        super("ClubProfile with ID " + clubId + " was not found. " + message);
    }
}