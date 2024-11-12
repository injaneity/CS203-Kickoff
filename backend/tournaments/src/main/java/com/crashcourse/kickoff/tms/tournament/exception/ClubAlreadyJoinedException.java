package com.crashcourse.kickoff.tms.tournament.exception;

/**
 * Exception thrown when a club attempts to join a tournament it has already joined.
 */
public class ClubAlreadyJoinedException extends RuntimeException {
    public ClubAlreadyJoinedException(String message) {
        super(message);
    }
}