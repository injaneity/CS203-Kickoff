package com.crashcourse.kickoff.tms.location.exception;

public class LocationNotFoundException extends RuntimeException {
    private static final String DEFAULT_MESSAGE = "Location not found with id: ";

    public LocationNotFoundException(Long id) {
        super(DEFAULT_MESSAGE + id);
    }
}