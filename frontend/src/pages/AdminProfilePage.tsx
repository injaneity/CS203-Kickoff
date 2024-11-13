import { useState, useEffect } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { useNavigate } from 'react-router-dom';
import { fetchAllPlayersAsync, selectPlayers } from '../store/userSlice';
import { PlayerProfile, PlayerStatus } from '../types/profile';
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
  const [filteredPlayers, setFilteredPlayers] = useState<PlayerProfile[]>([]);
  const [loading, setLoading] = useState(true);

  // Redirect if not an admin
  useEffect(() => {
    if (!isAdmin) {
      navigate('/not-authorized'); // Replace with your chosen path for unauthorized access
    }
  }, [isAdmin, navigate]);

  useEffect(() => {
    if (isAdmin) {
      setLoading(true)
      dispatch(fetchAllPlayersAsync()).then(() => {
        setLoading(false); // Set loading to false once the fetching is complete
      });
    }
  }, [dispatch, isAdmin]);

  useEffect(() => {
    // Compute filteredPlayers based on players, searchTerm, and playerFilter
    const filterPlayers = () => {
      return players.filter((player: PlayerProfile) => {
        const matchesSearch = player.username.toLowerCase().includes(searchTerm.toLowerCase());
        switch (playerFilter) {
          case PlayerFilter.ALL:
            return matchesSearch;
          case PlayerFilter.BLACKLISTED:
            return matchesSearch && player.status === PlayerStatus.STATUS_BLACKLISTED;
          default:
            return false;
        }
      });
    };

    setFilteredPlayers(filterPlayers());
  }, [players, searchTerm, playerFilter]);

  const handleStatusUpdate = (updatedPlayer: PlayerProfile) => {
    setFilteredPlayers((prevPlayers) =>
      prevPlayers.map((player) =>
        player.id === updatedPlayer.id ? updatedPlayer : player
      )
    );
  };

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
      {loading ? (
        <p>Loading...</p>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {filteredPlayers.length > 0 ? (
            filteredPlayers.map((player: PlayerProfile) => (
              <div key={player.id} className="space-y-2">
                <PlayerProfileCard
                  id={player.id}
                  availability={true}
                  needAvailability={false}
                  player={player}
                />
                {isAdmin && (
                  <ManagePlayerButton
                    playerProfile={player}
                    onStatusChange={handleStatusUpdate}
                  />
                )}
              </div>
            ))
          ) : (
            <p>No players available</p>
          )}
        </div>
      )}
    </div>
  );
};

export default AdminProfilePage;
