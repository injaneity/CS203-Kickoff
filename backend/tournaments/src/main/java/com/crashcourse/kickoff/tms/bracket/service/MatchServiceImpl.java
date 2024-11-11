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
     * @param kScore          The sensitivity parameter for the score difference; increasing k makes the function less sensitive. Base is implemented as 0 now.
     * @return                The adjusted score as a double between 0 and 1.
     */
    private static double adjustedScore(int scoreDifference, int kScore) {
        // inspired by sigmoid with int k set by us
        return 1 / (1 + Math.exp(-(scoreDifference - kScore)));
    }

    /**
     * 
     * @param club1Elo
     * @param club2Elo
     * @param club1RatingDeviation
     * @param club2RatingDeviation
     * @param club1Score
     * @param club2Score
     * @param club1Id
     * @param club2Id
     * @param winningClubId
     * @return              // array of ELO and RD
     */
    private static double[] calculateEloChange
    (double homeClubElo, double awayClubElo, double homeClubRatingDeviation, double awayClubRatingDeviation, 
    int homeClubScore, int awayClubScore, boolean homeClubWin) {
        // define constants for glicko-like rating calcs (but factoring in score difference later on)

        /*
         * GLICKO FORMULA CONSTANTS
         */
        final double K_ELO_SENSITIVITY = 30; // sensitivity to elo change
        final int k_SCORE_SENSITIVITY = 0; // sensitivity to score difference -- increasing this makes it less sensitive 
        final double q_SCALING_FACTOR = Math.log(10) / 400;

        // formula of glicko rating system
        double gRD2 = 1 / Math.sqrt(1 + (3 * Math.pow(q_SCALING_FACTOR * awayClubRatingDeviation, 2)) / Math.pow(Math.PI, 2)); // g is a function you apply on RD2
        double homeClubExpectedScore = 1 / (1 + Math.pow(10, gRD2 * (awayClubElo - homeClubElo) / 400)); // expected score representation for club 1 -- read glicko formula

        int scoreDifference = homeClubScore - awayClubScore;

        /*
         * Account for exceptional wins via draw
         */
        if (scoreDifference == 0) {
            if (homeClubWin) {
                scoreDifference = 1;
            } else {
                scoreDifference = -1;
            }
        }

        double homeClubWeightedScore = adjustedScore(scoreDifference, k_SCORE_SENSITIVITY); // actual score rep for club 1
        double newR1 = homeClubElo + K_ELO_SENSITIVITY * gRD2 * (homeClubWeightedScore - homeClubExpectedScore); // new elo for club 1

        double dSquared1 = 1 / (Math.pow(q_SCALING_FACTOR, 2) * Math.pow(gRD2, 2) * homeClubExpectedScore * (1 - homeClubExpectedScore));
        double newRD1 = Math.sqrt(1 / ((1 / Math.pow(homeClubRatingDeviation, 2)) + (1 / dSquared1))); // new rating deviation for club 1

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

        double club1Elo = club1Profile.getElo();
        double club1RatingDeviation = club1Profile.getRatingDeviation();
        double club2Elo = club2Profile.getElo();
        double club2RatingDeviation = club2Profile.getRatingDeviation();
        
        // Calculate new elo and RD for both clubs
        boolean homeClubWin = club1Id.equals(winningClubId);
        double[] newRatings1 = calculateEloChange(club1Elo, club2Elo, club1RatingDeviation, club2RatingDeviation, 
                                                club1Score, club2Score, homeClubWin);
        double club1NewElo = newRatings1[0];
        double club1NewRatingDeviation = newRatings1[1];
        
        // order of the clubids dont matter, just to check if they drew
        double[] newRatings2 = calculateEloChange(club2Elo, club1Elo, club2RatingDeviation, club1RatingDeviation, 
                                                club2Score, club1Score, !homeClubWin);
        double club2NewElo = newRatings2[0];
        double club2NewRatingDeviation = newRatings2[1];

        // update local club profiles for completeness -- do i even need to update actually, probably not
        club1Profile.setElo(club1NewElo);
        club1Profile.setRatingDeviation(club1NewRatingDeviation);
        club2Profile.setElo(club2NewElo);
        club2Profile.setRatingDeviation(club2NewRatingDeviation);

        // send updates to club microservice -- puts to clubcontroller, calling clubservice method, that updates the club's elo
        try {
            clubServiceClient.updateClubRating(club1Id, club1NewElo, club1NewRatingDeviation, jwtToken);
            clubServiceClient.updateClubRating(club2Id, club2NewElo, club2NewRatingDeviation, jwtToken);
        } catch (Exception e) {
            // Log the error and handle it appropriately
            throw new RuntimeException("Failed to update club ratings: " + e.getMessage(), e);
        }
    }
}
