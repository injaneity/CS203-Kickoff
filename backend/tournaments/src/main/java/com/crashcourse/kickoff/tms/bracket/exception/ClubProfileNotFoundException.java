package com.crashcourse.kickoff.tms.bracket.exception;

public class ClubProfileNotFoundException extends RuntimeException {
    public ClubProfileNotFoundException(Long clubId) {
        super("Club with ID " + clubId + " profile not found.");
    }
}
