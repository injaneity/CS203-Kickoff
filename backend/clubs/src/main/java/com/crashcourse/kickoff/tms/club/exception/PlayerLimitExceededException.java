package com.crashcourse.kickoff.tms.club.exception;
public class PlayerLimitExceededException extends RuntimeException {
    public PlayerLimitExceededException(int maxNumOfPlayers) {
        super("A club cannot have more than " + maxNumOfPlayers + " players.");
    }
}