package com.crashcourse.kickoff.tms.client.exception;

public class ClubRatingUpdateFailedException extends RuntimeException {
    public ClubRatingUpdateFailedException(Long clubId) {
        super("Failed to update rating for Club ID: " + clubId);
    }
    
    public ClubRatingUpdateFailedException(Long clubId, String message) {
        super("Failed to update rating for Club ID: " + clubId + ". " + message);
    }
}