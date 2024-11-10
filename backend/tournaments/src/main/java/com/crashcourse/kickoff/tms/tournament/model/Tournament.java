package com.crashcourse.kickoff.tms.tournament.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.crashcourse.kickoff.tms.location.model.Location;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.crashcourse.kickoff.tms.bracket.model.Bracket;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Tournament {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private boolean isOver;

    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;

    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "location_id")
    private Location location;

    private Integer maxTeams = 0;
    private TournamentFormat tournamentFormat;
    private KnockoutFormat knockoutFormat;
    private List<Float> prizePool;

    private Integer minRank;
    private Integer maxRank;

    private Long host;

    public enum VerificationStatus {
        AWAITING_PAYMENT,
        PAYMENT_COMPLETED,
        PENDING,
        APPROVED,
        REJECTED
    }

    @Enumerated(EnumType.STRING)
    private VerificationStatus verificationStatus = VerificationStatus.AWAITING_PAYMENT;

    private String verificationImageUrl;
    private boolean venueBooked;

    @ElementCollection
    @CollectionTable(name = "tournament_club_ids", joinColumns = @JoinColumn(name = "tournament_id"))
    @Column(name = "club_id")
    private List<Long> joinedClubIds = new ArrayList<>();

    @JsonManagedReference
    @OneToMany(mappedBy = "tournament", cascade = CascadeType.ALL)  
    private List<PlayerAvailability> playerAvailabilities = new ArrayList<>();

    @OneToOne(mappedBy = "tournament", cascade = CascadeType.ALL, orphanRemoval = true)
    private Bracket bracket;

    private boolean verificationPaid = false;

    public String getFormat() {
        return tournamentFormat.toString();
    }

    public String getLocationName() {
        return location.getName();
    }

    public boolean isVerificationPaid() {
        return verificationPaid;
    }

    public void setVerificationPaid(boolean verificationPaid) {
        this.verificationPaid = verificationPaid;
    }

    public VerificationStatus getVerificationStatus() {
        return verificationStatus != null ? verificationStatus : VerificationStatus.AWAITING_PAYMENT;
    }

    public boolean getVenueBooked() {
        return venueBooked;
    }

}
