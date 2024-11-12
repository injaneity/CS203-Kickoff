package com.crashcourse.kickoff.tms.bracket.exception;

public class NextRoundNotFoundException extends RuntimeException {
    public NextRoundNotFoundException() {
        super("Next round not found");
    }
}
