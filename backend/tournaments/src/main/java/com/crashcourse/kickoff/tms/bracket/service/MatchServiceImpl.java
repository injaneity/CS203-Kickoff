package com.crashcourse.kickoff.tms.bracket.service;

import org.springframework.stereotype.Service;

import com.crashcourse.kickoff.tms.bracket.dto.MatchUpdateDTO;
import com.crashcourse.kickoff.tms.bracket.exception.ClubProfileNotFoundException;
import com.crashcourse.kickoff.tms.bracket.exception.ClubRatingUpdateException;
import com.crashcourse.kickoff.tms.bracket.model.Match;
import com.crashcourse.kickoff.tms.bracket.model.Round;
import com.crashcourse.kickoff.tms.bracket.repository.MatchRepository;
import com.crashcourse.kickoff.tms.bracket.repository.RoundRepository;
import com.crashcourse.kickoff.tms.client.ClubServiceClient;
import com.crashcourse.kickoff.tms.club.ClubProfile;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MatchServiceImpl implements MatchService {

    private final MatchRepository matchRepository;
    private final RoundRepository roundRepository;
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
     * @param scoreDifference The difference between the scores of the two clubs (homeScore - awayScore).
     * @param scoreSensitivity The sensitivity parameter for the score difference; increasing this makes the function less sensitive.
     * @return The adjusted score as a double between 0 and 1.
     */
    private static double adjustedScore(int scoreDifference, int scoreSensitivity) {
        // Adjusted score using a sigmoid function; scoreSensitivity shifts the sigmoid curve
        return 1 / (1 + Math.exp(-(scoreDifference - scoreSensitivity)));
    }

    /**
     * Calculates the new Elo rating and rating deviation for a club based on the match result.
     *
     * @param clubElo               The current Elo rating of the club.
     * @param opponentElo           The current Elo rating of the opponent club.
     * @param clubRatingDeviation   The current rating deviation of the club.
     * @param opponentRatingDeviation The current rating deviation of the opponent club.
     * @param clubScore             The score of the club in the match.
     * @param opponentScore         The score of the opponent in the match.
     * @param clubWon               True if the club won the match; false otherwise.
     * @return An array containing the new Elo rating and new rating deviation for the club.
     */
    private static double[] calculateEloChange(
            double clubElo,
            double opponentElo,
            double clubRatingDeviation,
            double opponentRatingDeviation,
            int clubScore,
            int opponentScore,
            boolean clubWon) {

        // Constants
        final double ELO_SENSITIVITY_BASE = 30; // Base sensitivity to Elo change
        final int SCORE_SENSITIVITY = 0; // Sensitivity to score difference
        final double Q_SCALING_FACTOR = Math.log(10) / 400;
        final double RD_BASE = 50; // Reference RD value
        final int EXPECTED_SCORE_DENOMINATOR = 400;

        // Calculate the g(RD) function
        double gFunction = 1 / Math.sqrt(1 + (3 * Math.pow(Q_SCALING_FACTOR * opponentRatingDeviation, 2)) / Math.pow(Math.PI, 2));

        // Calculate the expected score
        double expectedScore = 1 / (1 + Math.pow(10, gFunction * (opponentElo - clubElo) / EXPECTED_SCORE_DENOMINATOR));

        // Calculate the score difference
        int scoreDifference = clubScore - opponentScore;

        // Account for draws with a declared winner
        if (scoreDifference == 0) {
            scoreDifference = clubWon ? 1 : -1;
        }

        // Calculate the adjusted match score
        double adjustedMatchScore = adjustedScore(scoreDifference, SCORE_SENSITIVITY);

        // Adjust K based on the player's RD
        double eloSensitivity = ELO_SENSITIVITY_BASE * (clubRatingDeviation / RD_BASE);

        // Update the club's Elo rating
        double eloChange = eloSensitivity * gFunction * (adjustedMatchScore - expectedScore);
        double newElo = clubElo + eloChange;

        // Calculate the variance (dSquared)
        double dSquared = 1 / (Math.pow(Q_SCALING_FACTOR, 2) * Math.pow(gFunction, 2) * expectedScore * (1 - expectedScore));

        // Update the club's rating deviation
        double newRatingDeviation = Math.sqrt(1 / ((1 / Math.pow(clubRatingDeviation, 2)) + (1 / dSquared)));

        return new double[]{newElo, newRatingDeviation};
    }

    /**
     * Updates the Elo ratings and rating deviations of two clubs based on the match result.
     *
     * @param matchUpdateDTO The MatchUpdateDTO containing match details such as club IDs, scores, and winning club ID.
     * @param jwtToken       The JWT token used for authentication when calling external services.
     */
    @Override
    public void updateElo(MatchUpdateDTO matchUpdateDTO, String jwtToken) {
        // get club IDs and scores
        Long homeClubId = matchUpdateDTO.getClub1Id();
        Long awayClubId = matchUpdateDTO.getClub2Id();
        int homeClubScore = matchUpdateDTO.getClub1Score();
        int awayClubScore = matchUpdateDTO.getClub2Score();
        Long winningClubId = matchUpdateDTO.getWinningClubId();

        // get club Profiles
        ClubProfile homeClubProfile = clubServiceClient.getClubProfileById(homeClubId, jwtToken);
        if (homeClubProfile == null) {
            throw new ClubProfileNotFoundException(homeClubId);
        }

        ClubProfile awayClubProfile = clubServiceClient.getClubProfileById(awayClubId, jwtToken);
        if (awayClubProfile == null) {
            throw new ClubProfileNotFoundException(awayClubId);
        }

        // get Elo ratings and rating deviations
        double homeClubElo = homeClubProfile.getElo();
        double homeClubRatingDeviation = homeClubProfile.getRatingDeviation();
        double awayClubElo = awayClubProfile.getElo();
        double awayClubRatingDeviation = awayClubProfile.getRatingDeviation();

        // Determine if the home club won
        boolean homeClubWon = homeClubId.equals(winningClubId);

        // Calculate new ratings for both clubs
        double[] homeClubNewRatings = calculateEloChange(
                homeClubElo,
                awayClubElo,
                homeClubRatingDeviation,
                awayClubRatingDeviation,
                homeClubScore,
                awayClubScore,
                homeClubWon
        );
        double homeClubNewElo = homeClubNewRatings[0];
        double homeClubNewRatingDeviation = homeClubNewRatings[1];

        double[] awayClubNewRatings = calculateEloChange(
                awayClubElo,
                homeClubElo,
                awayClubRatingDeviation,
                homeClubRatingDeviation,
                awayClubScore,
                homeClubScore,
                !homeClubWon
        );
        double awayClubNewElo = awayClubNewRatings[0];
        double awayClubNewRatingDeviation = awayClubNewRatings[1];

        /*
        * Ensure the winning team always gains at least 1 Elo point
        * Ensure the both team's RD decreases by at least 0.5
        * Cap the RD at a minimum of 30
        */ 

        // Apply Elo and RD adjustments for the home club
        homeClubNewElo = applyMinimumEloChange(homeClubWon, homeClubElo, homeClubNewElo);
        homeClubNewRatingDeviation = applyMinimumRatingDeviationChange(homeClubRatingDeviation, homeClubNewRatingDeviation);
    
        // Apply Elo and RD adjustments for the away club
        awayClubNewElo = applyMinimumEloChange(!homeClubWon, awayClubElo, awayClubNewElo);
        awayClubNewRatingDeviation = applyMinimumRatingDeviationChange(awayClubRatingDeviation, awayClubNewRatingDeviation);

        // update the clubs' ratings via the club service client
        try {
            clubServiceClient.updateClubRating(homeClubId, homeClubNewElo, homeClubNewRatingDeviation, jwtToken);
            clubServiceClient.updateClubRating(awayClubId, awayClubNewElo, awayClubNewRatingDeviation, jwtToken);
        } catch (Exception e) {
            throw new ClubRatingUpdateException();
        }
    }

    private static final double MINIMUM_ELO_GAIN = 1.0;
    private static final double MINIMUM_RD_DECREASE = 0.5;
    private static final double RD_CAP = 30.0;

    /**
     * Ensures the winning team gains at least a minimum Elo increase or that the losing team loses at least the minimum Elo.
     *
     * @param isWinningTeam   Boolean indicating if the club is the winning team.
     * @param clubElo         The original Elo of the club.
     * @param newClubElo      The newly calculated Elo of the club.
     * @return                The adjusted Elo value.
     */
    private static double applyMinimumEloChange(boolean isWinningTeam, double clubElo, double newClubElo) {
        double eloChange = newClubElo - clubElo;
        if (isWinningTeam && eloChange < MINIMUM_ELO_GAIN) {
            return clubElo + MINIMUM_ELO_GAIN;
        } else if (!isWinningTeam && eloChange > -MINIMUM_ELO_GAIN) {
            return clubElo - MINIMUM_ELO_GAIN;
        }
        return newClubElo;
    }

    /**
     * Ensures that the rating deviation decreases by at least the minimum amount and caps it at a specified minimum RD cap.
     *
     * @param originalRD     The original rating deviation.
     * @param newRD          The newly calculated rating deviation.
     * @return               The adjusted rating deviation value.
     */
    private static double applyMinimumRatingDeviationChange(double originalRD, double newRD) {
        double rdDecrease = originalRD - newRD;
        if (rdDecrease < MINIMUM_RD_DECREASE) {
            newRD = originalRD - MINIMUM_RD_DECREASE;
        }
        return Math.max(newRD, RD_CAP);
    }
}
