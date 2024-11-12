package com.crashcourse.kickoff.tms.bracket.exception;

public class InsufficientMatchesException extends RuntimeException {
    public InsufficientMatchesException() {
        super("Not enough matches to seed all clubs.");
    }
}
