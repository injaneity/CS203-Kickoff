import { useState, useEffect } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { useNavigate } from 'react-router-dom';
import { fetchAllPlayersAsync, selectPlayers } from '../store/userSlice';
import { PlayerProfile } from '../types/profile';
import PlayerProfileCard from '../components/PlayerProfileCard';
import { Input } from "../components/ui/input";
import ManagePlayerButton from "../components/ManagePlayerButton";
import { Search } from 'lucide-react';
import { AppDispatch } from '../store';
import { selectIsAdmin } from '../store/userSlice';
import { Button } from "../components/ui/button";

enum PlayerFilter {
  ALL = 'All Players',
  REPORTED = 'Reported',
  BLACKLISTED = 'Blacklisted',
}

const AdminProfilePage = () => {
  const dispatch = useDispatch<AppDispatch>();
  const navigate = useNavigate();
  const isAdmin = useSelector(selectIsAdmin);
  const players = useSelector(selectPlayers);
  const [searchTerm, setSearchTerm] = useState('');
  const [playerFilter, setPlayerFilter] = useState<PlayerFilter>(PlayerFilter.ALL);

  // Redirect if not an admin
  useEffect(() => {
    if (!isAdmin) {
      navigate('/not-authorized'); // Replace with your chosen path for unauthorized access
    }
  }, [isAdmin, navigate]);

  useEffect(() => {
    if (isAdmin) {
      dispatch(fetchAllPlayersAsync());
    }
  }, [dispatch, isAdmin]);

  const filteredPlayers = players.filter((player: PlayerProfile) => {
    const matchesSearch = player.username.toLowerCase().includes(searchTerm.toLowerCase());

    // Apply player filter logic
    if (playerFilter === PlayerFilter.ALL) return matchesSearch;
    // if (playerFilter === PlayerFilter.REPORTED) return matchesSearch && player.isReported; 
    // if (playerFilter === PlayerFilter.BLACKLISTED) return matchesSearch && player.isBlacklisted; 
    return false;
  });
  
  return (
    <div>
      <h2 className="text-2xl font-bold mb-4">Manage Players</h2>
      <div className="relative w-full mb-4">
        <Search className="absolute left-2 top-2.5 h-4 w-4 text-gray-500" />
        <Input
          type="search"
          placeholder="Search Players"
          className="pl-8 bg-gray-800 border-gray-700 w-full"
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
        />
      </div>
      {/* Filter Buttons */}
      <div className="flex justify-center space-x-4 mb-4">
        {Object.values(PlayerFilter).map((filter) => (
          <Button
            key={filter}
            onClick={() => setPlayerFilter(filter)}
            variant={playerFilter === filter ? "default" : "secondary"}
          >
            {filter}
          </Button>
        ))}
      </div>
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
        {filteredPlayers.length > 0 ? (
          filteredPlayers.map((player: PlayerProfile) => (
            <div key={player.id} className="space-y-2">
              <PlayerProfileCard
                id={player.id}
                availability={true}
                needAvailability={false}
              />
              {isAdmin && (
                <ManagePlayerButton
                  playerProfile={player}
                  onStatusChange={(newStatus) => {
                    // Update player's status locally if necessary
                    player.playerStatus = newStatus;
                    // dispatch(fetchAllPlayersAsync()); // Optionally refresh the list if necessary
                  }}
                />
              )}
            </div>
          ))
        ) : (
          <p>No players available</p>
        )}
      </div>
    </div>
  );
};

export default AdminProfilePage;
