package com.crashcourse.kickoff.tms.tournament.exception;

public class MatchNotFoundException extends RuntimeException {
  public MatchNotFoundException(Long matchId) {
    super("Match not found for match with id: " + matchId);
  }
}
