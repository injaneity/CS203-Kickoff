package com.crashcourse.kickoff.tms.player.dto;

import com.crashcourse.kickoff.tms.player.PlayerStatus;

public class PlayerStatusRequest {
    private PlayerStatus playerStatus;

    // Getters and Setters
    public PlayerStatus getPlayerStatus() {
        return playerStatus;
    }

    public void setPlayerStatus(PlayerStatus playerStatus) {
        this.playerStatus = playerStatus;
    }
}
