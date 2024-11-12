package com.crashcourse.kickoff.tms.bracket.exception;

public class ClubRatingUpdateException extends RuntimeException {
    public ClubRatingUpdateException() {
        super("Failed to update club ratings");
    }
}
