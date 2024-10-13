package com.crashcourse.kickoff.tms.club.dto;

import com.crashcourse.kickoff.tms.club.Club;

import lombok.Getter;
import lombok.Setter;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

@Getter
@Setter
public class ClubCreationRequest {

    @Valid
    @NotNull(message = "Club details are required")
    private Club club;
}