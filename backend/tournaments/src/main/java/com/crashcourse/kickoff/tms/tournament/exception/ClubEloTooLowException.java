package com.crashcourse.kickoff.tms.tournament.exception;

public class ClubEloTooLowException extends RuntimeException {
    public ClubEloTooLowException(int minRank) {
        super("Club does not meet tournament minimum elo requirement: " + minRank);
    }
}