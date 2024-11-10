package com.crashcourse.kickoff.tms.tournament.exception;

public class TournamentNotFoundException extends RuntimeException {
    public TournamentNotFoundException(Long tournamentId) {
        super("Tournament not found with id: " + tournamentId);
    }
}