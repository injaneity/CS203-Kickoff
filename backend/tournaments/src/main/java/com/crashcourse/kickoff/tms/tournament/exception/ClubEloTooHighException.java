package com.crashcourse.kickoff.tms.tournament.exception;

public class ClubEloTooHighException extends RuntimeException {
    public ClubEloTooHighException(int maxRank) {
        super("Club does not meet tournament maximum elo requirement: " + maxRank);
    }
}