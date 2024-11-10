package com.crashcourse.kickoff.tms.tournament.exception;

public class TournamentHasNoClubsException extends RuntimeException {
  public TournamentHasNoClubsException(Long tournamentId) {
    super("Tournament with id: " + tournamentId + " has no clubs");
  }
}
