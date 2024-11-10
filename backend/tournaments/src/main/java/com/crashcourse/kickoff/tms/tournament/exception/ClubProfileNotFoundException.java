package com.crashcourse.kickoff.tms.tournament.exception;

public class ClubProfileNotFoundException extends RuntimeException {
    public ClubProfileNotFoundException(Long clubId) {
        super("Club profile not found for club with id: " + clubId);
    }
}