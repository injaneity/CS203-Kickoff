import React, { useState } from 'react';
import { Match, Round, Bracket } from '../types/bracket';
import { Button } from "./ui/button";
import { Dialog, DialogContent, DialogHeader, DialogTitle } from "./ui/dialog";
import { Input } from "./ui/input";
import { Tournament } from '../types/tournament';
import { updateMatchInTournament } from '../services/tournamentService';
import { toast } from 'react-hot-toast';

interface TournamentBracketProps {
  tournament: Tournament;
  isHost: boolean;
  onMatchUpdate: () => void;
}

const TournamentBracket: React.FC<TournamentBracketProps> = ({ tournament, isHost, onMatchUpdate }) => {
  const [selectedMatch, setSelectedMatch] = useState<Match | null>(null);
  const [isScoreDialogOpen, setIsScoreDialogOpen] = useState(false);
  const [club1Score, setClub1Score] = useState<number>(0);
  const [club2Score, setClub2Score] = useState<number>(0);

  if (!tournament.bracket) {
    return <div>Tournament bracket not available</div>;
  }

  const getClubName = (clubId: number | null) => {
    if (!clubId) return 'TBD';
    const club = tournament.joinedClubsIds?.includes(clubId) ? `Team ${clubId}` : 'TBD';
    return club;
  };

  const handleMatchClick = (match: Match) => {
    if (isHost && !match.over && match.club1Id && match.club2Id) {
      setSelectedMatch(match);
      setClub1Score(match.club1Score);
      setClub2Score(match.club2Score);
      setIsScoreDialogOpen(true);
    }
  };

  const handleScoreSubmit = async () => {
    if (!selectedMatch || !tournament.id) return;

    try {
      await updateMatchInTournament(tournament.id, selectedMatch.id, {
        club1Score,
        club2Score
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
            <div className={`${match.winningClubId === match.club1Id ? 'font-bold text-green-500' : ''} py-1`}>
              {getClubName(match.club1Id)} {match.club1Score > 0 && `(${match.club1Score})`}
            </div>
            <div className="border-t border-gray-600 my-2" />
            <div className={`${match.winningClubId === match.club2Id ? 'font-bold text-green-500' : ''} py-1`}>
              {getClubName(match.club2Id)} {match.club2Score > 0 && `(${match.club2Score})`}
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

  const renderRound = (round: Round, index: number, totalRounds: number) => {
    const matches = round.matches;
    const baseSpacing = 8;
    
    // For first round (Quarter-Finals or equivalent)
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
                    marginTop: `${(firstMatchOffset + secondMatchOffset) / 2.75}rem`
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
        {sortedRounds.map((round, index) => renderRound(round, index, totalRounds))}
      </div>

      <Dialog open={isScoreDialogOpen} onOpenChange={setIsScoreDialogOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Update Match Score</DialogTitle>
          </DialogHeader>
          <div className="space-y-4">
            <div>
              <label className="block text-sm font-medium mb-1">
                {getClubName(selectedMatch?.club1Id)} Score
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
                {getClubName(selectedMatch?.club2Id)} Score
              </label>
              <Input
                type="number"
                value={club2Score}
                onChange={(e) => setClub2Score(Number(e.target.value))}
                min={0}
              />
            </div>
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