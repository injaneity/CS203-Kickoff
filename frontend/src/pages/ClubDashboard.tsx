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
import { AiFillWarning } from 'react-icons/ai';
import { Card, CardContent } from '../components/ui/card';
import TournamentCard from '../components/TournamentCard';
import { Dialog, DialogContent, DialogHeader, DialogTitle } from '../components/ui/dialog';

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
    <div className="container mx-auto p-6">
      <img
        src={`https://picsum.photos/seed/club-${club.id}/800/200`}
        alt={`${club.name} banner`}
        className="w-full h-48 object-cover mb-4 rounded"
      />
      <div className="flex items-center space-x-2 mb-4">
        <h1 className="text-3xl font-bold">{club.name}</h1>
        {isBlacklisted && (
          <div className="relative group">
            <AiFillWarning className="text-red-500" style={{ fontSize: '2em' }} />
            <span className="absolute bottom-full mb-2 hidden group-hover:block bg-gray-700 text-white text-xs rounded px-2 py-1">
              This club is blacklisted or contains blacklisted players
            </span>
          </div>
        )}

      </div>
      <p className="text-lg mb-4">{club.clubDescription || 'No description available.'}</p>
      <div className="flex items-center mb-4">
        <div className="mr-4">
          <strong>Captain:</strong> {captain?.username || 'No captain assigned.'}
        </div>
        <div>
          <strong>ELO:</strong> {club.elo ? club.elo.toFixed(2) : 'N/A'}
        </div>
      </div>

      {/* Players List */}
      <div className="mb-4">
        <h2 className="text-2xl font-semibold mb-2">Players in the Club</h2>
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {players ? (
            players.map((player) => (
              <PlayerProfileCard
                key={player.id}
                id={player.id}
                availability={false}
                needAvailability={false}
                onDeleteClick={captain?.id == userId ? handleOpenRemoveDialog : null}
              />
            ))
          ) : (
            <p>Loading player profiles...</p>
          )}
        </div>
      </div>

      {/* Tournament Filter Buttons */}
      <div className="flex justify-center space-x-4 mb-4">
        <Button
          onClick={() => setTournamentFilter(TournamentFilter.UPCOMING)}
          variant={tournamentFilter === TournamentFilter.UPCOMING ? "default" : "secondary"}
        >
          Upcoming Tournaments
        </Button>
        <Button
          onClick={() => setTournamentFilter(TournamentFilter.CURRENT)}
          variant={tournamentFilter === TournamentFilter.CURRENT ? "default" : "secondary"}
        >
          Current Tournaments
        </Button>
        <Button
          onClick={() => setTournamentFilter(TournamentFilter.PAST)}
          variant={tournamentFilter === TournamentFilter.PAST ? "default" : "secondary"}
        >
          Past Tournaments
        </Button>
      </div>

      {/* Tournaments Section */}
      <div className="mb-4">
        <h2 className="text-2xl font-semibold mb-2">
          {tournamentFilter === TournamentFilter.UPCOMING && "Upcoming Tournaments"}
          {tournamentFilter === TournamentFilter.CURRENT && "Current Tournaments"}
          {tournamentFilter === TournamentFilter.PAST && "Past Tournaments"}
        </h2>
        {tournaments.length > 0 ? (
          // <ul>
          //   {tournaments.map((tournament) => (
          //     <li key={tournament.id}>
          //       <strong>{tournament.name}</strong> - Starts: {new Date(tournament.startDateTime).toLocaleString()}, Ends: {new Date(tournament.endDateTime).toLocaleString()}
          //     </li>
          //   ))}
          // </ul>
          <Card className="mt-6">
            <CardContent>
              <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-3">
                {tournaments.map((tournament) => (
                  tournament.id &&
                  <TournamentCard
                    key={tournament.id}
                    tournament={tournament}
                  />
                ))}
              </div>
            </CardContent>
          </Card>


        ) : (
          <p>No tournaments found for the selected filter.</p>
        )}
        {userId && (
          <div className="mt-5 bottom-6 right-6">
            <LeaveClubButton />
          </div>
        )}
      </div>

      {/* Remove Confirmation Dialog */}
      <Dialog open={isRemoveDialogOpen} onOpenChange={setIsRemoveDialogOpen}>
        <DialogContent className="sm:max-w-[425px]">
          <DialogHeader>
            <DialogTitle>Remove {usernameToRemove}</DialogTitle>
          </DialogHeader>
          <div className="mt-4">
            <p> {`Are you sure you want to remove ${usernameToRemove} from your club?`} </p>
          </div>
          <div className="flex flex-col sm:flex-row justify-between mt-4 space-y-2 sm:space-y-0 sm:space-x-2">
            <button 
              className="px-4 py-2 bg-red-500 text-white rounded hover:bg-red-600 w-full" 
              onClick={() => setIsRemoveDialogOpen(false)}
            >
              Cancel
            </button>
            <button 
              className="px-4 py-2 bg-green-500 text-white rounded hover:bg-green-600 w-full"
              onClick={handleConfirmRemove}
            >
              Confirm
            </button>
          </div>
        </DialogContent>
      </Dialog>

    </div>
  );
};

export default ClubDashboard;
