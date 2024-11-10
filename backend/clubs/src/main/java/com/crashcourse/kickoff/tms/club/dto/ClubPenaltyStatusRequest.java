package com.crashcourse.kickoff.tms.club.dto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import lombok.Data;

@Data
public class ClubPenaltyStatusRequest {
    private String banUntil; // Accept any format or empty string

    private String penaltyType;

    // This method parses `banUntil` only if it's in the correct format
    public LocalDateTime getBanUntilAsLocalDateTime() {
        if (banUntil != null && !banUntil.isEmpty()) {
            try {
                return LocalDateTime.parse(banUntil, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
            } catch (DateTimeParseException e) {
                return null;
            }
        }
        
        // Return null if the format is incorrect or the value is empty
        return null;
    }
}
