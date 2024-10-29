package com.crashcourse.kickoff.tms.match.service;

import java.util.*;
import java.util.function.Function;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import com.crashcourse.kickoff.tms.match.model.Match;
import com.crashcourse.kickoff.tms.match.model.Round;

import com.crashcourse.kickoff.tms.match.repository.MatchRepository;
import com.crashcourse.kickoff.tms.match.repository.RoundRepository;
import com.crashcourse.kickoff.tms.client.ClubServiceClient;
import com.crashcourse.kickoff.tms.club.ClubProfile;
import com.crashcourse.kickoff.tms.match.dto.*;
import com.crashcourse.kickoff.tms.tournament.model.Tournament;
import com.crashcourse.kickoff.tms.tournament.repository.TournamentRepository;

import lombok.RequiredArgsConstructor;
import jakarta.persistence.EntityNotFoundException;

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

        // Validate Winning Club
        Long winningClubId = matchUpdateDTO.getWinningClubId();
        if (!winningClubId.equals(club1Id) && !winningClubId.equals(club2Id)) {
            throw new RuntimeException("Invalid winning club");
        }

        // Constants
        double PI = Math.PI;
        double q = Math.log(10) / 400;

        // Function to calculate g(RD)
        Function<Double, Double> g = (RD) -> 1 / Math.sqrt(1 + (3 * Math.pow(q * RD, 2)) / (PI * PI));

        double R1 = club1Profile.getElo();
        double RD1 = club1Profile.getRatingDeviation();
        double R2 = club2Profile.getElo();
        double RD2 = club2Profile.getRatingDeviation();
        
        // Club 1 Calculations
        double gRD2 = g.apply(RD2);
        double E1 = 1 / (1 + Math.pow(10, gRD2 * (R2 - R1) / 400));
        double S1 = (club1Score > club2Score) ? 1.0 : (club1Score == club2Score) ? 0.5 : 0.0;
        double K = 20; // Adjust as needed
        double newR1 = R1 + K * gRD2 * (S1 - E1);
        double dSquared1 = 1 / (Math.pow(q, 2) * Math.pow(gRD2, 2) * E1 * (1 - E1));
        double newRD1 = Math.sqrt(1 / ((1 / Math.pow(RD1, 2)) + (1 / dSquared1)));

        // Club 2 Calculations
        double gRD1 = g.apply(RD1);
        double E2 = 1 / (1 + Math.pow(10, gRD1 * (R1 - R2) / 400));
        double S2 = (club2Score > club1Score) ? 1.0 : (club2Score == club1Score) ? 0.5 : 0.0;
        double newR2 = R2 + K * gRD1 * (S2 - E2);
        double dSquared2 = 1 / (Math.pow(q, 2) * Math.pow(gRD1, 2) * E2 * (1 - E2));
        double newRD2 = Math.sqrt(1 / ((1 / Math.pow(RD2, 2)) + (1 / dSquared2)));

        // Update Club Profiles -- do i even need to update actually, probably not
        club1Profile.setElo(newR1);
        club1Profile.setRatingDeviation(newRD1);

        club2Profile.setElo(newR2);
        club2Profile.setRatingDeviation(newRD2);

        // Send updates back to Club Microservice
        clubServiceClient.updateClubRating(club1Id, newR1, newRD1, jwtToken);
        clubServiceClient.updateClubRating(club2Id, newR2, newRD2, jwtToken);
    }
}
