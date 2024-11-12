package com.crashcourse.kickoff.tms.bracket.exception;

public class NoClubsInMatchException extends RuntimeException {
    public NoClubsInMatchException() {
        super("\"No clubs in match.\"");
    }
}
