package com.crashcourse.kickoff.tms.club.dto;

import lombok.Data;

@Data
public class ClubRatingUpdateDTO {
    private double rating;
    private double ratingDeviation;
}