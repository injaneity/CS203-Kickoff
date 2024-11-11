package com.crashcourse.kickoff.tms.player.dto;
import java.util.List;

import com.crashcourse.kickoff.tms.player.model.PlayerPosition;
import com.crashcourse.kickoff.tms.player.model.PlayerStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class PlayerProfileResponseDTO {
    private Long id;
    private String username;
    private String profileDescription;
    private List<PlayerPosition> preferredPositions;
    private PlayerStatus status;
    private String profilePictureUrl;
}
