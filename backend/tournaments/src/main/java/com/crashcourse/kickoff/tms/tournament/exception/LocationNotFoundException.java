package com.crashcourse.kickoff.tms.tournament.exception;

public class LocationNotFoundException extends RuntimeException {
    public LocationNotFoundException(Long locationId) {
        super("Location not found with id: " + locationId);
    }
}