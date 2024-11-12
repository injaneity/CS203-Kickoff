package com.crashcourse.kickoff.tms.tournament.exception;

public class BlacklistedFromTournamentException extends RuntimeException {
    public BlacklistedFromTournamentException() {
        super("Club is blacklisted or contains blacklisted players. Unable to join tournament.");
    }
}
