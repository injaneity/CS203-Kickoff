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
      p-4 rounded-lg mb-4
    `;

    return (
      <div 
        key={match.id} 
        className={matchStyle}
        onClick={() => handleMatchClick(match)}
      >
        <div className="flex justify-between items-center">
          <div className="flex-1">
            <div className={`${match.winningClubId === match.club1Id ? 'font-bold text-green-500' : ''}`}>
              {getClubName(match.club1Id)} {match.club1Score > 0 && `(${match.club1Score})`}
            </div>
            <div className={`${match.winningClubId === match.club2Id ? 'font-bold text-green-500' : ''}`}>
              {getClubName(match.club2Id)} {match.club2Score > 0 && `(${match.club2Score})`}
            </div>
          </div>
          <div className="text-sm text-gray-400">
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

  const renderRound = (round: Round) => (
    <div key={round.id} className="flex-1">
      <h3 className="text-lg font-semibold mb-4">{getRoundName(round.roundNumber)}</h3>
      <div className="space-y-4">
        {round.matches.map(renderMatch)}
      </div>
    </div>
  );

  return (
    <div className="mt-8">
      <div className="flex gap-8 overflow-x-auto pb-4">
        {tournament.bracket.rounds
          .sort((a: Round, b: Round) => b.roundNumber - a.roundNumber)
          .map(renderRound)}
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