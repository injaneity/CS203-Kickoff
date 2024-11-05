package com.crashcourse.kickoff.tms.club.dto;

import java.time.LocalDateTime;

import com.crashcourse.kickoff.tms.club.model.ClubPenaltyStatus.PenaltyType;
import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

@Data
public class ClubPenaltyStatusRequest {
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime banUntil;
    private String penaltyType;
}
