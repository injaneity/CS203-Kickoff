import PlayerProfileCard from './PlayerProfileCard';
import { PlayerAvailabilityDTO } from '../types/playerAvailability';
import { Users, CheckCircle, XCircle } from 'lucide-react';

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
    <div>
      <div className="flex items-center mb-6">
        <Users className="w-5 h-5 mr-2 text-blue-400" />
        <h3 className="text-xl font-semibold">Team Availability</h3>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-6">
        <div className="bg-gray-700/50 rounded-lg p-4 border border-gray-600">
          <div className="flex items-center justify-between">
            <span className="text-gray-400">Total Players</span>
            <Users className="w-5 h-5 text-blue-400" />
          </div>
          <p className="text-2xl font-bold mt-2">{totalPlayers}</p>
        </div>

        <div className="bg-gray-700/50 rounded-lg p-4 border border-gray-600">
          <div className="flex items-center justify-between">
            <span className="text-gray-400">Available</span>
            <CheckCircle className="w-5 h-5 text-green-400" />
          </div>
          <p className="text-2xl font-bold mt-2 text-green-400">{availablePlayers}</p>
        </div>

        <div className="bg-gray-700/50 rounded-lg p-4 border border-gray-600">
          <div className="flex items-center justify-between">
            <span className="text-gray-400">Unavailable</span>
            <XCircle className="w-5 h-5 text-red-400" />
          </div>
          <p className="text-2xl font-bold mt-2 text-red-400">{unavailablePlayers}</p>
        </div>
      </div>

      {filteredAvailabilities.length === 0 ? (
        <div className="text-center py-8 bg-gray-700/50 rounded-lg border border-gray-600">
          <Users className="w-12 h-12 text-gray-500 mx-auto mb-3" />
          <p className="text-gray-400">No player from your club has indicated availability yet.</p>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {filteredAvailabilities.map((availability) => (
            <div key={availability.playerId} className="transform transition-transform duration-200 hover:scale-[1.02]">
              <PlayerProfileCard
                id={availability.playerId}
                availability={availability.available}
                needAvailability={true}
              />
            </div>
          ))}
        </div>
      )}
    </div>
  );
}