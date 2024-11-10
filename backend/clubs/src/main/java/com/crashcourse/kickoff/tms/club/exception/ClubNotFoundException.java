package com.crashcourse.kickoff.tms.club.exception;
public class ClubNotFoundException extends RuntimeException {
    public ClubNotFoundException(Long clubId) {
        super("Club with ID " + clubId + " not found.");
    }
    
    public ClubNotFoundException(String message) {
        super(message);
    }
}
