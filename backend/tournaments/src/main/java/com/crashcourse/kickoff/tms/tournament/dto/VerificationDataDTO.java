package com.crashcourse.kickoff.tms.tournament.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VerificationDataDTO {
    private boolean venueBooked;
    private String confirmationUrl;
    private String paymentConfirmationId;
    private String verificationImage;

    public boolean getVenueBooked() {
        return venueBooked;
    }
}
