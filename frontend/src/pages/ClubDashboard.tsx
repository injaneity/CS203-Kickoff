import React, { useEffect, useState } from 'react';
import { toast } from 'react-hot-toast';
import { Button } from '../components/ui/button';
import { ClubProfile } from '../types/club';
import PlayerProfileCard from '../components/PlayerProfileCard';
import { PlayerProfile } from '../types/profile';
import { useSelector } from 'react-redux';
import { selectUserId } from '../store/userSlice';
import { fetchPlayerProfileById } from '../services/userService';
import LeaveClubButton from '../components/LeaveClubButton';
import { getClubProfileById, removePlayerFromClub } from '../services/clubService';
import { Tournament, TournamentFilter } from '../types/tournament';
import { getTournamentsByClubId } from '../services/tournamentService';
import { Badge } from '../components/ui/badge';
import TournamentCard from '../components/TournamentCard';
import { Dialog, DialogContent, DialogHeader, DialogTitle } from '../components/ui/dialog';
import { Trophy, Users, Star, AlertTriangle } from 'lucide-react';

interface ClubDashboardProps {
  id: number;
}

const ClubDashboard: React.FC<ClubDashboardProps> = ({ id }) => {
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [tournaments, setTournaments] = useState<Tournament[]>([]);
  const [tournamentFilter, setTournamentFilter] = useState<TournamentFilter>(TournamentFilter.UPCOMING);
  const [isBlacklisted, setBlacklisted] = useState(false);

  const [club, setClub] = useState<ClubProfile | null>(null);
  const [captain, setCaptain] = useState<PlayerProfile | null>(null);
  const [players, setPlayers] = useState<PlayerProfile[] | null>(null);
  const userId = useSelector(selectUserId);
  const [isRemoveDialogOpen, setIsRemoveDialogOpen] = useState(false);
  const [userIdToRemove, setUserIdToRemove] = useState(0);
  const [usernameToRemove, setUsernameToRemove] = useState('');

  useEffect(() => {
    const fetchClub = async () => {
      try {
        const clubResponse = await getClubProfileById(id);
        setClub(clubResponse);
        if (clubResponse.penaltyStatus) {
          setBlacklisted(clubResponse.penaltyStatus.active);
        }

        const captainResponse = await fetchPlayerProfileById(clubResponse.captainId.toString());
        setCaptain(captainResponse);

        const playerIds = clubResponse.players; // Assuming clubResponse.data.players is an array of player IDs
        const playerProfiles = await Promise.all(
          playerIds.map((player) => fetchPlayerProfileById(player.toString()))
        );
        console.log(playerProfiles);

        // Store the player profiles in state
        setPlayers(playerProfiles);
      } catch (err: any) {
        console.error('Error fetching club info:', err);
        setError('Failed to fetch club information.');
      } finally {
        setLoading(false);
      }
    };

    fetchClub();
  }, [id]);

  useEffect(() => {
    const fetchTournaments = async () => {
      try {
        const response = await getTournamentsByClubId(id, tournamentFilter);
        console.log(response);

        setTournaments(response);
      } catch (err: any) {
        console.error('Error fetching tournaments:', err);
        toast.error('Failed to fetch tournaments.');
      }
    };

    fetchTournaments();
  }, [id, tournamentFilter]);

  const handleOpenRemoveDialog = (playerId: number, playerUsername: string) => {
    setUserIdToRemove(playerId);
    setUsernameToRemove(playerUsername)
    setIsRemoveDialogOpen(true);
  };

  const handleConfirmRemove = async () => {
    if (!club) {
      return;
    }
    try {
      await removePlayerFromClub(club?.id, userIdToRemove);
      toast.success(`Removed ${usernameToRemove}`);
      setPlayers((prevPlayers) => 
        prevPlayers ? prevPlayers.filter(player => player.id !== userIdToRemove) : null
      );
    } catch (err) {
      console.error('Error removing player:', err);
    }
    
    
    setIsRemoveDialogOpen(false);
  };

  if (loading) {
    return <div className="flex justify-center items-center h-screen">Loading...</div>;
  }

  if (error || !club) {
    return (
      <div className="flex justify-center items-center h-screen">
        {error || 'Club not found.'}
      </div>
    );
  }

  return (
    <div className="max-w-7xl mx-auto pb-20">
      {/* Club Header Banner */}
      <div className="relative mb-8">
        <img
          src={`https://picsum.photos/seed/club-${club?.id}/1200/300`}
          alt={`${club?.name} banner`}
          className="w-full h-48 object-cover rounded-lg shadow-lg"
        />
        <div className="absolute inset-0 bg-gradient-to-t from-gray-900 via-gray-900/50 to-transparent rounded-lg" />
        <div className="absolute bottom-0 left-0 right-0 p-6">
          <div className="flex items-center gap-4">
            <div className="bg-gradient-to-br from-blue-500 to-purple-500 p-3 rounded-xl shadow-lg">
              <Trophy className="h-6 w-6 text-white" />
            </div>
            <div className="space-y-2">
              <div className="flex items-center gap-3">
                <h1 className="text-2xl lg:text-3xl font-bold text-white">{club?.name}</h1>
                {isBlacklisted && (
                  <Badge variant="destructive" className="bg-red-500/20 text-red-300 border border-red-500/30">
                    <AlertTriangle className="w-4 h-4 mr-1" />
                    Blacklisted
                  </Badge>
                )}
              </div>
              <p className="text-gray-300">{club?.clubDescription || 'No description available.'}</p>
            </div>
          </div>
        </div>
      </div>

      {/* Club Stats Grid */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6 mb-8">
        {/* Main Info */}
        <div className="lg:col-span-2 bg-gray-800 rounded-lg p-6 shadow-lg border border-gray-700">
          <h3 className="text-xl font-semibold mb-6 flex items-center">
            <Trophy className="w-5 h-5 mr-2 text-blue-400" />
            Club Information
          </h3>
          <div className="grid grid-cols-2 gap-4">
            <div>
              <p className="text-gray-400 text-sm">Captain</p>
              <p className="text-lg font-medium">{captain?.username || 'No captain assigned'}</p>
            </div>
            <div>
              <p className="text-gray-400 text-sm">Total Members</p>
              <p className="text-lg font-medium">{players?.length || 0} players</p>
            </div>
          </div>
        </div>

        {/* Stats Card */}
        <div className="bg-gray-800 rounded-lg p-6 shadow-lg border border-gray-700">
          <h3 className="text-xl font-semibold mb-6 flex items-center">
            <Star className="w-5 h-5 mr-2 text-yellow-500" />
            Club Stats
          </h3>
          <div className="space-y-4">
            <div>
              <p className="text-gray-400 text-sm mb-1">ELO Rating</p>
              <p className="text-2xl font-bold text-yellow-500">
                {club?.elo ? club.elo.toFixed(0) : 'N/A'}
              </p>
            </div>
            <div>
              <p className="text-gray-400 text-sm mb-1">Rating Deviation</p>
              <p className="text-lg">±{club?.ratingDeviation ? club.ratingDeviation.toFixed(0) : 'N/A'}</p>
            </div>
          </div>
        </div>
      </div>

      {/* Team Members Section */}
      <div className="bg-gray-800 rounded-lg p-6 mb-8 shadow-lg border border-gray-700">
        <h3 className="text-xl font-semibold mb-6 flex items-center">
          <Users className="w-5 h-5 mr-2 text-blue-400" />
          Team Members
        </h3>
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {players ? (
            players.map((player) => (
              <div key={player.id} className="transform transition-transform duration-200 hover:scale-[1.02]">
                <PlayerProfileCard
                  id={player.id}
                  availability={false}
                  needAvailability={false}
                  onDeleteClick={captain?.id === userId ? handleOpenRemoveDialog : undefined}
                />
              </div>
            ))
          ) : (
            <p className="text-gray-400">Loading player profiles...</p>
          )}
        </div>
      </div>

      {/* Tournament Filter Section */}
      <div className="bg-gray-800 rounded-lg p-6 mb-8 shadow-lg border border-gray-700">
        <div className="flex flex-col space-y-6">
          <h3 className="text-xl font-semibold flex items-center">
            <Trophy className="w-5 h-5 mr-2 text-blue-400" />
            Tournaments
          </h3>
          
          <div className="flex flex-wrap justify-center gap-3">
            {Object.values(TournamentFilter).map((filter) => (
              <Button
                key={filter}
                onClick={() => setTournamentFilter(filter)}
                variant={tournamentFilter === filter ? "default" : "secondary"}
                className={tournamentFilter === filter ? "bg-blue-600 hover:bg-blue-700" : ""}
              >
                {filter.replace('_', ' ')}
              </Button>
            ))}
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            {tournaments.length > 0 ? (
              tournaments.map((tournament) => (
                tournament.id && (
                  <TournamentCard
                    key={tournament.id}
                    tournament={tournament}
                  />
                )
              ))
            ) : (
              <div className="col-span-full text-center py-8">
                <Trophy className="w-12 h-12 text-gray-500 mx-auto mb-3" />
                <p className="text-gray-400">No tournaments found for the selected filter.</p>
              </div>
            )}
          </div>
        </div>
      </div>

      {/* Leave Club Button */}
      {userId && (
        <div className="flex justify-end">
          <LeaveClubButton />
        </div>
      )}

      {/* Remove Player Dialog */}
      <Dialog open={isRemoveDialogOpen} onOpenChange={setIsRemoveDialogOpen}>
        <DialogContent className="sm:max-w-[425px] bg-gray-800 border border-gray-700">
          <DialogHeader>
            <DialogTitle>Remove {usernameToRemove}</DialogTitle>
          </DialogHeader>
          <div className="mt-4">
            <p className="text-gray-300">Are you sure you want to remove {usernameToRemove} from your club?</p>
          </div>
          <div className="flex justify-end space-x-3 mt-6">
            <Button
              variant="secondary"
              onClick={() => setIsRemoveDialogOpen(false)}
            >
              Cancel
            </Button>
            <Button
              variant="destructive"
              onClick={handleConfirmRemove}
            >
              Remove
            </Button>
          </div>
        </DialogContent>
      </Dialog>
    </div>
  );
};

export default ClubDashboard;
