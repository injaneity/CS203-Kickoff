package com.crashcourse.kickoff.tms.bracket.model;

import java.util.*;

import com.crashcourse.kickoff.tms.tournament.model.Tournament;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;


@Entity
@Data
@Table(name = "GAME") // Match is a reserved SQL keyword
public class Match {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private boolean isOver;
    private Long matchNumber;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "round_id")
    private Round round;

    /*
     * Clubs
     */
    private Long club1Id;
    private Long club2Id;

    /*
     * Score
     */
    private int club1Score;
    private int club2Score;

    private Long winningClubId;

}
