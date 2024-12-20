package com.crashcourse.kickoff.tms.bracket.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;

import com.crashcourse.kickoff.tms.client.ClubServiceClient;
import com.crashcourse.kickoff.tms.club.ClubProfile;

import com.crashcourse.kickoff.tms.bracket.dto.MatchUpdateDTO;
import com.crashcourse.kickoff.tms.bracket.model.*;
import com.crashcourse.kickoff.tms.bracket.repository.*;
import com.crashcourse.kickoff.tms.bracket.exception.*;

import com.crashcourse.kickoff.tms.tournament.model.Tournament;
import com.crashcourse.kickoff.tms.tournament.repository.TournamentRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BracketServiceImpl implements BracketService {

    private final TournamentRepository tournamentRepository;
    private final BracketRepository bracketRepository;
    private final RoundRepository roundRepository;
    private final MatchRepository matchRepository;
    private final RoundService roundService;
    private final ClubServiceClient clubServiceClient;

    @Override
    public Bracket createBracket(Long tournamentId, List<Long> joinedClubIds, String jwtToken) {
        /*
         * Validation
         */
        int numberOfClubs = joinedClubIds.size();
        if (numberOfClubs == 0) {
            throw new EntityNotFoundException("No clubs found");
        }
        Tournament tournament = tournamentRepository.findById(tournamentId)
            .orElseThrow(() -> new EntityNotFoundException("Tournament not found with id: " + tournamentId));
        
        /*
         * Create Bracket
         */
        int numberOfRounds = (int) Math.ceil(Math.log(numberOfClubs) / Math.log(2));
        Bracket bracket = new Bracket();
        List<Round> bracketRounds = new ArrayList<>();

        while (numberOfRounds > 0) {
            int size = (int) Math.pow(2, numberOfRounds - 1.0);
            bracketRounds.add(roundService.createRound(size, numberOfRounds));
            numberOfRounds--;
        }

        bracket.setRounds(bracketRounds);
        Round firstRound = bracketRounds.get(0);
        seedClubs(firstRound, joinedClubIds, jwtToken);
        promoteByes(bracket, bracketRounds.get(0), bracketRounds.get(1));
        bracketRepository.save(bracket);        

        bracket.setTournament(tournament);
        tournament.setBracket(bracket);

        return bracket;
    }

    /*
     * Seeding Algorithm
     */
    private List<Integer> generateStandardSeedOrder(int bracketSize) {
        if ((bracketSize & (bracketSize - 1)) != 0) {
            throw new IllegalArgumentException("Bracket size must be a power of 2");
        }
        return generateSeedsRecursively(bracketSize);
    }

    private List<Integer> generateSeedsRecursively(int bracketSize) {
        if (bracketSize == 1) {
            List<Integer> seed = new ArrayList<>();
            seed.add(1);
            return seed;
        }

        List<Integer> prevSeeds = generateSeedsRecursively(bracketSize / 2);
        List<Integer> mirroredSeeds = new ArrayList<>();

        for (int seed : prevSeeds) {
            mirroredSeeds.add(bracketSize + 1 - seed);
        }

        List<Integer> combinedSeeds = new ArrayList<>();
        for (int i = 0; i < prevSeeds.size(); i++) {
            combinedSeeds.add(prevSeeds.get(i));
            combinedSeeds.add(mirroredSeeds.get(i));
        }

        return combinedSeeds;
    }

    public void seedIntoMatches(List<Match> matches, List<Integer> seedPositions, List<ClubProfile> clubs, int byes) {
        int seedIndex = 0;
    
        for (Match match : matches) {

            boolean byeRound1 = assignClubToMatch(match, seedPositions, clubs, seedIndex, 1);
            if (byeRound1) {
                byes--;
            }
            seedIndex++;
    
            if (seedIndex < seedPositions.size()) {
                boolean byeRound2 = assignClubToMatch(match, seedPositions, clubs, seedIndex, 2);
                if (byeRound2) {
                    byes--;
                }
                seedIndex++;
            }
    
            matchRepository.save(match);
        }
    }

    private boolean assignClubToMatch(Match match, List<Integer> seedPositions,  List<ClubProfile> clubs,
                                        int seedIndex, int clubPosition) {
        if (seedIndex >= seedPositions.size()) {
            return false;
        }
        int numberOfClubs = clubs.size();

        int seed = seedPositions.get(seedIndex);
        Long clubId = (seed <= numberOfClubs) ? clubs.get(seed - 1).getId() : null;

        if (clubPosition == 1) {
            match.setClub1Id(clubId);
        } else {
            match.setClub2Id(clubId);
        }

        return clubId == null;
    }

    @Override
    public void seedClubs(Round firstRound, List<Long> clubIds, String jwtToken) {
        List<ClubProfile> clubs = new ArrayList<>();
        for (Long id : clubIds) {
            ClubProfile club = clubServiceClient.getClubProfileById(id, jwtToken);
            if (club != null) {
                clubs.add(club);
            }
        }

        clubs.sort(Comparator.comparingDouble(ClubProfile::getElo).reversed());

        int numberOfClubs = clubs.size();
        int bracketSize = (int) Math.pow(2, Math.ceil(Math.log(numberOfClubs) / Math.log(2)));
        int byes = bracketSize - numberOfClubs;

        List<Match> matches = firstRound.getMatches();
        int totalMatches = matches.size();


        List<Integer> seedPositions = generateStandardSeedOrder(bracketSize);
        if (totalMatches * 2 < seedPositions.size()) {
            throw new InsufficientMatchesException();
        }

        seedIntoMatches(matches, seedPositions, clubs, byes);

        roundRepository.save(firstRound);
    }

    public void promoteByes(Bracket bracket, Round firstRound, Round secondRound) {
        if (firstRound == null) {
            return;
        }
    
        for (Match match : firstRound.getMatches()) {
            if (isBye(match)) {
                processByeMatch(bracket, match, secondRound);
            }
        }
    }

    private boolean isBye(Match match) {
        Long club1Id = match.getClub1Id();
        Long club2Id = match.getClub2Id();
        return (club1Id == null && club2Id != null) || (club2Id == null && club1Id != null);
    }

    private void processByeMatch(Bracket bracket, Match match, Round secondRound) {
        Long club1Id = match.getClub1Id();
        Long club2Id = match.getClub2Id();
        Long winningClubId = (club1Id != null) ? club1Id : club2Id;

        match.setOver(true);
        match.setWinningClubId(winningClubId);
        matchRepository.save(match);
    
        if (secondRound == null) {
            bracket.setWinningClubId(winningClubId);
            bracketRepository.save(bracket);
        } else {
            promoteToNextRound(match.getMatchNumber(), winningClubId, secondRound);
        }
    }

    public void promoteToNextRound(Long matchNumber, Long winningClubId, Round secondRound) {
        List<Match> secondRoundMatches = secondRound.getMatches();
        int nextMatchIndex = (int) Math.ceil(matchNumber / 2.0) - 1;
        if (nextMatchIndex < 0 || nextMatchIndex >= secondRoundMatches.size()) {
            throw new EntityNotFoundException("Invalid match index for next round. Match Number: " + matchNumber);
        }

        Match nextMatch = secondRoundMatches.get(nextMatchIndex);
        if (matchNumber % 2 == 1) {
            nextMatch.setClub1Id(winningClubId);
        } else {
            nextMatch.setClub2Id(winningClubId);
        }

        matchRepository.save(nextMatch);
    }

    @Override
    public Match updateMatch(Tournament tournament, Match match, MatchUpdateDTO matchUpdateDTO) {

        Bracket bracket = tournament.getBracket();

        Long matchNumber = match.getMatchNumber();
        Long club1Id = matchUpdateDTO.getClub1Id();
        Long club2Id = matchUpdateDTO.getClub2Id();
        Long winningClubId = matchUpdateDTO.getWinningClubId();

        /*
         * If over, send winner to next round
         */
        if (matchUpdateDTO.isOver()) {

            /*
             * Validation for No Clubs: if seeding is done correctly, there should be
             * no empty matches since every preceding match will have at least 1 club
             */
            if (club1Id == null && club2Id == null) {
                throw new NoClubsInMatchException();
            }

            match.setOver(true);
            
            /*
             * Note that roundNumber counts downwards 
             * so we know if its the last round
             */
            if (match.getRound().getRoundNumber() == 1) {
                bracket.setWinningClubId(winningClubId);
                bracketRepository.save(bracket);
                tournament.setOver(true);
                tournamentRepository.save(tournament);

            } else {
                /*
                 * -1 for next round, -1 since we use 1 index
                 */
                List<Round> rounds = bracket.getRounds();
                Long currentRoundNumber = match.getRound().getRoundNumber();
                
                Round nextRound = rounds.stream()
                    .filter(r -> r.getRoundNumber() == currentRoundNumber - 1)
                    .findFirst()
                    .orElseThrow(NextRoundNotFoundException::new);

                List<Match> matches = nextRound.getMatches();

                /*
                 * -1 to account for 1 indexing again
                 */
                Match nextMatch = matches.get((int)Math.ceil(matchNumber/2.0) - 1);
                
                if (matchNumber % 2 == 1) {
                    nextMatch.setClub1Id(winningClubId);
                } else {
                    nextMatch.setClub2Id(winningClubId);
                }
                matchRepository.save(nextMatch);
            }
        }
        
        return matchRepository.save(match);
    }

}
