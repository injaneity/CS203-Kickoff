package com.crashcourse.kickoff.tms.bracket.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.crashcourse.kickoff.tms.client.ClubServiceClient;
import com.crashcourse.kickoff.tms.club.ClubProfile;
import com.crashcourse.kickoff.tms.bracket.dto.MatchResponseDTO;
import com.crashcourse.kickoff.tms.bracket.dto.MatchUpdateDTO;
import com.crashcourse.kickoff.tms.bracket.model.Match;
import com.crashcourse.kickoff.tms.bracket.model.Round;
import com.crashcourse.kickoff.tms.bracket.repository.MatchRepository;
import com.crashcourse.kickoff.tms.bracket.repository.RoundRepository;
import com.crashcourse.kickoff.tms.tournament.repository.TournamentRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MatchServiceImpl implements MatchService {

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private RoundRepository roundRepository;

    @Autowired
    private TournamentRepository tournamentRepository;

    private final ClubServiceClient clubServiceClient;

    /*
     * Matches can now only be created through
     * create bracket
     */
    @Override
    public Match createMatch(Long roundId, Long matchNumber) {
        Match match = new Match();
        match.setMatchNumber(matchNumber);

        Round round = roundRepository.findById(roundId)
            .orElseThrow(() -> new EntityNotFoundException("Round not found with id: " + roundId));
        match.setRound(round);
        return matchRepository.save(match);
    }
    

    @Override
    public Match getMatchById(Long id) {
        return matchRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Match not found with ID: " + id));
    }

    /**
     * Calculates the adjusted score using a sigmoid function based on the score difference.
     *
     * @param scoreDifference The difference between the scores of the two clubs (club1Score - club2Score).
     * @param k               The sensitivity parameter for the score difference; increasing k makes the function less sensitive. Base is implemented as 0 now.
     * @return                The adjusted score as a double between 0 and 1.
     */
    private static double adjustedScore(int scoreDifference, int k) {
        // inspired by sigmoid with int k set by us
        return 1 / (1 + Math.exp(-(scoreDifference - k)));
    }

    /**
     * Calculates the new Elo rating and rating deviation for a club based on the match result.
     *
     * @param R1            The current Elo rating of the club.
     * @param R2            The current Elo rating of the opponent club.
     * @param RD1           The current rating deviation of the club.
     * @param RD2           The current rating deviation of the opponent club.
     * @param club1Score    The score of the club in the match.
     * @param club2Score    The score of the opponent club in the match.
     * @param club1Id       The ID of the club.
     * @param club2Id       The ID of the opponent club.
     * @param winningClubId The ID of the winning club; used to resolve draws (penalty shootouts).
     * @return              A double array containing the new Elo rating [0] and new rating deviation [1] for the club.
     */
    private static double[] calculateEloChange
    (double R1, double R2, double RD1, double RD2, int club1Score, 
    int club2Score, Long club1Id, Long club2Id, Long winningClubId) {
        // define constants for glicko-like rating calcs (but factoring in score difference later on)
        double K = 30; // sensitivity to elo change
        int k = 0; // sensitivity to score difference -- increasing this makes it less sensitive 
        
        // formula of glicko rating system
        double q = Math.log(10) / 400;
        double gRD2 = 1 / Math.sqrt(1 + (3 * Math.pow(q * RD2, 2)) / Math.pow(Math.PI, 2)); // g is a function you apply on RD2
        double E1 = 1 / (1 + Math.pow(10, gRD2 * (R2 - R1) / 400)); // expected score representation for club 1 -- read glicko formula

        int scoreDifference = club1Score - club2Score;

        // if they win on penalty, take it as they won marginally by 1 goal
        if (scoreDifference == 0) {
            if (winningClubId.equals(club1Id)) {
                scoreDifference = 1;
            } else if (winningClubId.equals(club2Id)) {
                scoreDifference = -1;
            }
        }

        double S1 = adjustedScore(scoreDifference, k); // actual score rep for club 1
        double newR1 = R1 + K * gRD2 * (S1 - E1); // new elo for club 1

        double dSquared1 = 1 / (Math.pow(q, 2) * Math.pow(gRD2, 2) * E1 * (1 - E1));
        double newRD1 = Math.sqrt(1 / ((1 / Math.pow(RD1, 2)) + (1 / dSquared1))); // new rating deviation for club 1

        return new double[]{newR1, newRD1};
    }

    /**
     * Updates the Elo ratings and rating deviations of two clubs based on the match result.
     *
     * @param matchUpdateDTO The MatchUpdateDTO containing match details such as club IDs, scores, and winning club ID.
     * @param jwtToken       The JWT token used for authentication when calling external services.
     */
    @Override
    public void updateElo(MatchUpdateDTO matchUpdateDTO, String jwtToken) {
        // Extract club IDs and scores
        Long club1Id = matchUpdateDTO.getClub1Id();
        Long club2Id = matchUpdateDTO.getClub2Id();
        int club1Score = matchUpdateDTO.getClub1Score();
        int club2Score = matchUpdateDTO.getClub2Score();
        Long winningClubId = matchUpdateDTO.getWinningClubId();

        // Fetch Club Profiles
        ClubProfile club1Profile = clubServiceClient.getClubProfileById(club1Id, jwtToken);
        if (club1Profile == null) {
            throw new RuntimeException("Club 1 profile not found.");
        }

        ClubProfile club2Profile = clubServiceClient.getClubProfileById(club2Id, jwtToken);
        if (club2Profile == null) {
            throw new RuntimeException("Club 2 profile not found.");
        }

        double R1 = club1Profile.getElo();
        double RD1 = club1Profile.getRatingDeviation();
        double R2 = club2Profile.getElo();
        double RD2 = club2Profile.getRatingDeviation();
        
        // Calculate new elo and RD for both clubs
        double[] newRatings1 = calculateEloChange(R1, R2, RD1, RD2, club1Score, club2Score, club1Id, club2Id, winningClubId);
        double newR1 = newRatings1[0];
        double newRD1 = newRatings1[1];
        
        // order of the clubids dont matter, just to check if they drew
        double[] newRatings2 = calculateEloChange(R2, R1, RD2, RD1, club2Score, club1Score, club1Id, club2Id, winningClubId);
        double newR2 = newRatings2[0];
        double newRD2 = newRatings2[1];

        // update local club profiles for completeness -- do i even need to update actually, probably not
        club1Profile.setElo(newR1);
        club1Profile.setRatingDeviation(newRD1);
        club2Profile.setElo(newR2);
        club2Profile.setRatingDeviation(newRD2);

        // send updates to club microservice -- puts to clubcontroller, calling clubservice method, that updates the club's elo
        try {
            clubServiceClient.updateClubRating(club1Id, newR1, newRD1, jwtToken);
            clubServiceClient.updateClubRating(club2Id, newR2, newRD2, jwtToken);
        } catch (Exception e) {
            // Log the error and handle it appropriately
            throw new RuntimeException("Failed to update club ratings: " + e.getMessage(), e);
        }
    }
}
