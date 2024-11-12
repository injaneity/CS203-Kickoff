package com.crashcourse.kickoff.tms.tournament.exception;

public class NoClubIndicateAvailabilityException extends RuntimeException {
    public NoClubIndicateAvailabilityException() {
        super("You must join a club before indicating availability.");
    }
}
