package com.crashcourse.kickoff.tms.club.model;

import java.time.LocalDateTime;

import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Embeddable
@NoArgsConstructor
@AllArgsConstructor
public class ClubPenaltyStatus {
    
    private LocalDateTime banUntil;

    private PenaltyType penaltyType;

    // Enum for the type of penalty
    public enum PenaltyType {
        NONE,
        BLACKLISTED,
        REPORTED
    }

    // Override the getter to include time-based logic
    public PenaltyType getPenaltyType() {
        // If there is a ban in effect, return BAN, else return NONE
        if (banUntil != null && LocalDateTime.now().isBefore(banUntil)) {
            return penaltyType;
        }
        return PenaltyType.NONE;
    }

    // Check if the penalty is currently active
    public boolean isActive() {
        return banUntil != null && LocalDateTime.now().isBefore(banUntil);
    }

    // Apply a penalty until a specific time
    public void applyPenalty(LocalDateTime until, PenaltyType type) {
        this.banUntil = until;
        this.penaltyType = type;
    }

    // Lift any active penalty
    public void liftPenalty() {
        this.banUntil = null;
        this.penaltyType = PenaltyType.NONE;
    }
}
