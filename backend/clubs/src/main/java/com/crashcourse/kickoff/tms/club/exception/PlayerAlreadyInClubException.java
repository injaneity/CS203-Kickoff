package com.crashcourse.kickoff.tms.club.exception;
public class PlayerAlreadyInClubException extends RuntimeException {
    public PlayerAlreadyInClubException(String message) {
        super(message);
    }
}