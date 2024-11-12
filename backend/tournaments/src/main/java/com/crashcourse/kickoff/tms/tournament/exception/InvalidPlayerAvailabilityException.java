package com.crashcourse.kickoff.tms.tournament.exception;

public class InvalidPlayerAvailabilityException extends RuntimeException {
    public InvalidPlayerAvailabilityException() {
        super("\"Failed to update player availability: \"");
    }
}
