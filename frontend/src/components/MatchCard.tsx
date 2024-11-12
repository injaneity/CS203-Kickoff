import React from 'react';
import { useNavigate } from 'react-router-dom';
import { Match } from '../types/bracket';
import { ClubProfile } from '../types/club';

interface MatchCardProps {
  match: Match;
  club1: ClubProfile;
  club2: ClubProfile;
  tournamentId: number; // Pass the tournament ID as a prop
}

const MatchCard: React.FC<MatchCardProps> = ({ match, club1, club2, tournamentId }) => {
  const navigate = useNavigate();
  const scoreDifference = Math.abs(match.club1Score - match.club2Score);
  const winningClub = match.winningClubId === club1.id ? club1 : club2;
  const losingClub = match.winningClubId === club1.id ? club2 : club1;

  const getResultVerb = () => {
    if (scoreDifference === 0) return 'draws with';
    if (scoreDifference <= 2) return ['defeats', 'overcomes', 'edges out'][Math.floor(Math.random() * 3)];
    if (scoreDifference <= 3) return ['beats', 'triumphs over', 'outplays'][Math.floor(Math.random() * 3)];
    return ['thrashes', 'steamrolls', 'demolishes'][Math.floor(Math.random() * 3)];
  };

  const getClubImage = (clubId: number) => `https://picsum.photos/seed/club-${clubId}/100/100`;

  const handleCardClick = () => {
    navigate(`/tournaments/${tournamentId}`);
  };

  return (
    <div
      className="bg-gray-800 rounded-lg p-4 mb-4 shadow-md border border-gray-400 cursor-pointer hover:bg-gray-700 transition"
      onClick={handleCardClick}
    >
      <div className="flex items-center justify-between mb-2">
        <div className="flex items-center">
          <img src={getClubImage(winningClub.id)} alt={winningClub.name} className="w-10 h-10 rounded-full mr-2" />
          <span className="font-bold text-green-500">{winningClub.name}</span>
        </div>
        <div className="text-xl font-bold">
          {match.club1Id === winningClub.id ? match.club1Score : match.club2Score} - {match.club1Id === losingClub.id ? match.club1Score : match.club2Score}
        </div>
        <div className="flex items-center">
          <span className="font-bold text-red-500">{losingClub.name}</span>
          <img src={getClubImage(losingClub.id)} alt={losingClub.name} className="w-10 h-10 rounded-full ml-2" />
        </div>
      </div>
      <p className="text-center text-sm text-gray-400">
        {winningClub.name} {getResultVerb()} {losingClub.name}
      </p>
    </div>
  );
};

export default MatchCard;
