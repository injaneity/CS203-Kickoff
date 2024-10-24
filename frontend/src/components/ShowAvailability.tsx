import PlayerProfileCard from './PlayerProfileCard';
import { PlayerAvailabilityDTO } from '../types/playerAvailability';

interface ShowAvailabilityProps {
  availabilities: PlayerAvailabilityDTO[];
  currentUserId: number;
  currentUserClubId?: number;
}

export default function ShowAvailability({ availabilities, currentUserClubId }: ShowAvailabilityProps) {
  const filteredAvailabilities = availabilities.filter(a => a.clubId === currentUserClubId);
  const totalPlayers = filteredAvailabilities.length;
  const availablePlayers = filteredAvailabilities.filter((a) => a.available).length;
  const unavailablePlayers = totalPlayers - availablePlayers;

  return (
    <div className="bg-gray-800 rounded-lg p-6 mb-6">
      <h3 className="text-2xl font-semibold mb-4">Club Member's Availability</h3>
      <div className="mb-4">
        <p>Total players: {totalPlayers}</p>
        <p>Available: {availablePlayers}</p>
        <p>Not Available: {unavailablePlayers}</p>
      </div>
      {filteredAvailabilities.length === 0 ? (
        <p>No player from your club has indicated availability yet.</p>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {filteredAvailabilities.map((availability) => (
            <PlayerProfileCard
              key={availability.playerId}
              id={availability.playerId}
              availability={availability.available}
              needAvailability={true}
            />
          ))}
        </div>
      )}
    </div>
  );
}