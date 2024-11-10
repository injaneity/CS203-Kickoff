package com.crashcourse.kickoff.tms.client.exception;

public class PenaltyStatusVerificationException extends RuntimeException {
    public PenaltyStatusVerificationException(Long clubId) {
        super("Failed to verify penalty status for Club ID: " + clubId);
    }
    
    public PenaltyStatusVerificationException(Long clubId, String message) {
        super("Failed to verify penalty status for Club ID: " + clubId + ". " + message);
    }
}