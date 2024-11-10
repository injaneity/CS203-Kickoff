package com.crashcourse.kickoff.tms.tournament.exception;

public class BracketAlreadyCreatedException extends RuntimeException {
    public BracketAlreadyCreatedException(Long tournamentId) {
        super("Bracket has already been created for tournament with id: " + tournamentId);
    }
}