package com.crashcourse.kickoff.tms.tournament.exception;

public class InvalidWinningClubException extends RuntimeException {
    public InvalidWinningClubException(Long clubId) {
        super("Invalid winning club with id: " + clubId);
    }
}