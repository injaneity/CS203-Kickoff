package com.crashcourse.kickoff.tms.tournament.exception;

public class ClubNotJoinedException extends RuntimeException {
    public ClubNotJoinedException(Long clubId) {
        super("Club " + clubId + " is not part of the tournament.");
    }
}
