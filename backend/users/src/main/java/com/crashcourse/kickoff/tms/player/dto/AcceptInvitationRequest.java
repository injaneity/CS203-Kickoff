package com.crashcourse.kickoff.tms.player.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AcceptInvitationRequest {
    private Long clubId;
}
