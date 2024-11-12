package com.crashcourse.kickoff.tms.bracket.dto;


import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MatchUpdateDTO {
    /*
     * Match Id covered in Path Variable
     */
    private boolean isOver;
    
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
