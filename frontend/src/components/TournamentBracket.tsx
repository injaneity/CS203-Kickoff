import React, { useState, useEffect } from 'react';
import { Match, Round } from '../types/bracket';
import { Button } from "./ui/button";
import { Dialog, DialogContent, DialogHeader, DialogTitle } from "./ui/dialog";
import { Input } from "./ui/input";
import { Tournament } from '../types/tournament';
import { updateMatchInTournament } from '../services/tournamentService';
import { toast } from 'react-hot-toast';
import { getClubProfileById } from '../services/clubService';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from './ui/select';

interface TournamentBracketProps {
  tournament: Tournament;
  isHost: boolean;
  onMatchUpdate: () => void;
}

const TournamentBracket: React.FC<TournamentBracketProps> = ({ tournament, isHost, onMatchUpdate }) => {
  const [winningClubId, setWinningClubId] = useState<number | undefined>(undefined);
  const [selectedMatch, setSelectedMatch] = useState<Match>();
  const [isScoreDialogOpen, setIsScoreDialogOpen] = useState(false);
  const [club1Score, setClub1Score] = useState<number>(0);
  const [club2Score, setClub2Score] = useState<number>(0);
  const [clubNames, setClubNames] = useState<{ [key: number]: string }>({});

  useEffect(() => {
    const fetchClubNames = async () => {
      const uniqueClubIds = new Set<number>();

      // Collect all club IDs from matches
      tournament.bracket?.rounds.forEach(round => {
        round.matches.forEach(match => {
          if (match.club1Id) uniqueClubIds.add(match.club1Id);
          if (match.club2Id) uniqueClubIds.add(match.club2Id);
        });
      });

      // Fetch club names for all unique IDs
      const clubNamesMap: { [key: number]: string } = {};
      await Promise.all(
        Array.from(uniqueClubIds).map(async (clubId) => {
          try {
            const clubProfile = await getClubProfileById(clubId);
            clubNamesMap[clubId] = clubProfile.name;
          } catch (error) {
            console.error(`Failed to fetch club name for ID ${clubId}:`, error);
            clubNamesMap[clubId] = `Team ${clubId}`;
          }
        })
      );

      setClubNames(clubNamesMap);
    };

    if (tournament.bracket) {
      fetchClubNames();
    }
  }, [tournament.bracket]);

  if (!tournament.bracket) {
    return <div>Tournament bracket not available</div>;
  }

  const getClubName = (clubId: number | null | undefined, match: Match | undefined) => {

    // Special handling for walk-overs in the first round
    const isFirstRound = match && tournament.bracket?.rounds.some(round =>
      round.matches.includes(match) &&
      round.roundNumber === Math.max(...tournament.bracket?.rounds.map(r => r.roundNumber) || [])
    );

    if (isFirstRound && !clubId) {
      return <span className="text-yellow-400">WALK</span>;
    }

    if (!clubId) return 'TBD';
    return clubNames[clubId] || `Team ${clubId}`;
  };

  const handleMatchClick = (match: Match) => {
    if (isHost && !match.over && match.club1Id && match.club2Id) {
      setSelectedMatch(match);
      setClub1Score(match.club1Score);
      setClub2Score(match.club2Score);
      setWinningClubId(match.club1Id)
      setIsScoreDialogOpen(true);
    }
  };

  const handleScoreSubmit = async () => {
    if (!selectedMatch || !tournament.id) return;

    let winner = winningClubId || 0;
    if (club1Score > club2Score) {
      setWinningClubId(selectedMatch.club1Id || 0)
      winner = selectedMatch.club1Id || 0
    } else if (club1Score < club2Score) {
      setWinningClubId(selectedMatch.club2Id || 0)
      winner = selectedMatch.club2Id || 0
    }

    // Ensure a winning club is selected
    if (!winningClubId) {
      toast.error('Please select a winning club.');
      return;
    }

    try {
      await updateMatchInTournament(tournament.id, selectedMatch.id, {
        isOver: true,
        club1Id: selectedMatch.club1Id!,
        club2Id: selectedMatch.club2Id!,
        club1Score: club1Score,
        club2Score: club2Score,
        winningClubId: winner
      });
      setIsScoreDialogOpen(false);
      onMatchUpdate();
      toast.success('Match score updated successfully');
    } catch (error) {
      console.error('Error updating match:', error);
      toast.error('Failed to update match score');
    }
  };

  const renderMatch = (match: Match) => {
    const isWalkOver = match.over && (match.club1Id === null || match.club2Id === null);

    const matchStyle = `
      ${match.over ? 'bg-gray-700' : 'bg-gray-800'} 
      ${isHost && !match.over && match.club1Id && match.club2Id ? 'cursor-pointer hover:bg-gray-600' : ''}
      p-4 rounded-lg w-64
      border-2 border-gray-600
      shadow-md
      transition-colors duration-200
    `;

    return (
      <div
        key={match.id}
        className={matchStyle}
        onClick={() => handleMatchClick(match)}
      >
        <div className="flex justify-between items-center">
          <div className="flex-1">
            <div className="flex justify-between items-center">
              <div className={`${match.winningClubId === match.club1Id ? 'font-bold text-green-500' : ''} py-1 flex-1`}>
                {getClubName(match.club1Id, match)}
              </div>
              {match.over && !isWalkOver && (
                <div className="ml-2 px-2 py-1 bg-gray-700 rounded text-base">
                  {match.club1Score}
                </div>
              )}
            </div>
            <div className="border-t border-gray-600 my-2" />
            <div className="flex justify-between items-center">
              <div className={`${match.winningClubId === match.club2Id ? 'font-bold text-green-500' : ''} py-1 flex-1`}>
                {getClubName(match.club2Id, match)}
              </div>
              {match.over && !isWalkOver && (
                <div className="ml-2 px-2 py-1 bg-gray-700 rounded text-base">
                  {match.club2Score}
                </div>
              )}
            </div>
          </div>
          <div className="text-sm text-gray-400 ml-2">
            Match {match.matchNumber}
          </div>
        </div>
      </div>
    );
  };

  const getRoundName = (roundNumber: number) => {
    switch (roundNumber) {
      case 1: return 'Finals';
      case 2: return 'Semi-Finals';
      case 3: return 'Quarter-Finals';
      case 4: return 'Round of 16';
      case 5: return 'Round of 32';
      default: return `Round ${roundNumber}`;
    }
  };

  const renderRound = (round: Round, totalRounds: number) => {
    const matches = round.matches;
    const baseSpacing = 8;

    // For first round
    if (round.roundNumber === totalRounds) {
      return (
        <div key={round.id} className="flex-1 flex flex-col items-center">
          <h3 className="text-lg font-semibold mb-4 text-center">{getRoundName(round.roundNumber)}</h3>
          <div className="flex flex-col" style={{ gap: `${baseSpacing}rem` }}>
            {matches.map(match => (
              <div key={match.id} className="flex flex-col">
                {renderMatch(match)}
              </div>
            ))}
          </div>
        </div>
      );
    }

    // For Semi-Finals and Finals (or equivalent rounds)
    return (
      <div key={round.id} className="flex-1 flex flex-col items-center">
        <h3 className="text-lg font-semibold mb-4 text-center">{getRoundName(round.roundNumber)}</h3>
        <div className="flex flex-col">
          {matches.map((match, idx) => {
            const roundsFromEnd = totalRounds - round.roundNumber;
            const scaleFactor = Math.pow(2, roundsFromEnd - 1);

            // If this is a single-match round (Finals or equivalent), position in middle
            if (matches.length === 1) {
              const firstMatchOffset = baseSpacing * scaleFactor;
              const secondMatchOffset = baseSpacing * 2.9 * scaleFactor;
              return (
                <div
                  key={match.id}
                  className="flex flex-col"
                  style={{
                    marginTop: `${(firstMatchOffset + secondMatchOffset) / (6 - totalRounds)}rem`
                  }}
                >
                  {renderMatch(match)}
                </div>
              );
            }

            // For rounds with multiple matches
            const firstMatchOffset = baseSpacing * scaleFactor;
            const secondMatchOffset = baseSpacing * 2.9 * scaleFactor;

            return (
              <div
                key={match.id}
                className="flex flex-col"
                style={{
                  marginTop: idx === 0 ? `${firstMatchOffset}rem` : `${secondMatchOffset}rem`
                }}
              >
                {renderMatch(match)}
              </div>
            );
          })}
        </div>
      </div>
    );
  };

  // Sort rounds from first to last (e.g., Round of 16 -> Finals)
  const sortedRounds = [...tournament.bracket.rounds].sort((a, b) => b.roundNumber - a.roundNumber);
  const totalRounds = sortedRounds.length;

  return (
    <div className="mt-8">
      <div className="flex justify-start items-start gap-16 overflow-x-auto pb-8 px-4">
        {sortedRounds.map((round) => renderRound(round, totalRounds))}
      </div>

      <Dialog open={isScoreDialogOpen} onOpenChange={setIsScoreDialogOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Update Match Score</DialogTitle>
          </DialogHeader>
          <div className="space-y-4">
            <div>
              <label className="block text-sm font-medium mb-1">
                {getClubName(selectedMatch?.club1Id, selectedMatch)} Score
              </label>
              <Input
                type="number"
                value={club1Score}
                onChange={(e) => setClub1Score(Number(e.target.value))}
                min={0}
              />
            </div>
            <div>
              <label className="block text-sm font-medium mb-1">
                {getClubName(selectedMatch?.club2Id, selectedMatch)} Score
              </label>
              <Input
                type="number"
                value={club2Score}
                onChange={(e) => setClub2Score(Number(e.target.value))}
                min={0}
              />
            </div>
            {club1Score == club2Score &&
              <div>
                <label className="block text-sm font-medium mb-1">
                  Select Winning Club
                </label>
                <Select
                    defaultValue={ selectedMatch ? Number(selectedMatch.club1Id).toString() || '' : ''}
                    onValueChange={(e) => setWinningClubId(Number(e))}
                  >
                    <SelectTrigger className="w-full bg-gray-800 border-gray-700">
                      <SelectValue placeholder={ selectedMatch ? getClubName(selectedMatch.club1Id, selectedMatch).toString() || '' : ''} />
                    </SelectTrigger>
                    <SelectContent>
                      {selectedMatch?.club1Id && (
                        <SelectItem key={selectedMatch.club1Id} value={selectedMatch.club1Id.toString()}>
                        {getClubName(selectedMatch.club1Id, selectedMatch)}
                      </SelectItem>
                      )}
                      {selectedMatch?.club2Id && (
                        <SelectItem key={selectedMatch.club2Id} value={selectedMatch.club2Id.toString()}>
                        {getClubName(selectedMatch.club2Id, selectedMatch)}
                      </SelectItem>
                      )}
                      
                    </SelectContent>
                  </Select>
              </div>
            }
            <div className="flex justify-end space-x-2">
              <Button
                variant="secondary"
                onClick={() => setIsScoreDialogOpen(false)}
              >
                Cancel
              </Button>
              <Button onClick={handleScoreSubmit}>
                Update Score
              </Button>
            </div>
          </div>
        </DialogContent>
      </Dialog>
    </div>
  );
};

export default TournamentBracket; 