package com.crashcourse.kickoff.tms.match.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.crashcourse.kickoff.tms.client.ClubServiceClient;
import com.crashcourse.kickoff.tms.club.ClubProfile;
import com.crashcourse.kickoff.tms.match.dto.MatchResponseDTO;
import com.crashcourse.kickoff.tms.match.dto.MatchUpdateDTO;
import com.crashcourse.kickoff.tms.match.model.Match;
import com.crashcourse.kickoff.tms.match.model.Round;
import com.crashcourse.kickoff.tms.match.repository.MatchRepository;
import com.crashcourse.kickoff.tms.match.repository.RoundRepository;
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
     * Maps Tournament entity to TournamentResponseDTO.
     *
     * @param tournament Tournament entity
     * @return TournamentResponseDTO
     */
    private MatchResponseDTO mapToResponseDTO(Match match) {
        return new MatchResponseDTO(
                match.getId(),
                match.isOver(),

                match.getRound().getTournament().getId(),
                match.getClub1Id(),
                match.getClub2Id(),

                match.getClub1Score(),
                match.getClub2Score(),
                match.getWinningClubId()
        );
    }

    private static double adjustedScore(int scoreDifference, int k) {
        // inspired by sigmoid with int k set by us
System.out.println("scoreDifference: " + scoreDifference + "\tk: " + k);
        return 1 / (1 + Math.exp(-(scoreDifference - k)));
    }

    // forced to take in winningClub param to know which club won in a draw (penalty, etc) -- but will affect less elo
    private static double[] calculateEloChange
    (double R1, double R2, double RD1, double RD2, int club1Score, 
    int club2Score) {
        // define constants for glicko-like rating calcs (but factoring in score difference later on)
        double K = 30; // sensitivity to elo change
        int k = 0; // sensitivity to score difference -- increasing this makes it less sensitive 
        
        // formula of glicko rating system
        double q = Math.log(10) / 400;
        double gRD2 = 1 / Math.sqrt(1 + (3 * Math.pow(q * RD2, 2)) / Math.pow(Math.PI, 2)); // g is a function you apply on RD2
        double E1 = 1 / (1 + Math.pow(10, gRD2 * (R2 - R1) / 400)); // expected score representation for club 1 -- read glicko formula

        double S1 = adjustedScore(club1Score - club2Score, k); // actual score rep for club 1
System.out.println("S1: " + S1 + "\tE1:" + E1);

        double newR1 = R1 + K * gRD2 * (S1 - E1); // new elo for club 1

        double dSquared1 = 1 / (Math.pow(q, 2) * Math.pow(gRD2, 2) * E1 * (1 - E1));
        double newRD1 = Math.sqrt(1 / ((1 / Math.pow(RD1, 2)) + (1 / dSquared1))); // new rating deviation for club 1

        return new double[]{newR1, newRD1};
    }

    @Override
    public void updateElo(MatchUpdateDTO matchUpdateDTO, String jwtToken) {
        // Extract club IDs and scores
        Long club1Id = matchUpdateDTO.getClub1Id();
        Long club2Id = matchUpdateDTO.getClub2Id();
        int club1Score = matchUpdateDTO.getClub1Score();
        int club2Score = matchUpdateDTO.getClub2Score();

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
        double[] newRatings1 = calculateEloChange(R1, R2, RD1, RD2, club1Score, club2Score);
        double newR1 = newRatings1[0];
        double newRD1 = newRatings1[1];
        double[] newRatings2 = calculateEloChange(R2, R1, RD2, RD1, club2Score, club1Score);
        double newR2 = newRatings2[0];
        double newRD2 = newRatings2[1];
System.out.println("newR1: " + newR1 + " newRD1: " + newRD1 + " newR2: " + newR2 + " newRD2: " + newRD2 + "\n\n");

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
