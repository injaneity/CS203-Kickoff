package com.crashcourse.kickoff.tms.tournament.exception;

public class InvalidJoinRoleException extends RuntimeException {
    public InvalidJoinRoleException() {
        super("\"Only a club captain can join the tournament for the club.\"");
    }
}
