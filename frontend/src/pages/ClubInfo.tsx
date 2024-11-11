import React, { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { toast } from 'react-hot-toast';
import { Button } from '../components/ui/button';
import { ClubProfile } from '../types/club';
import { PlayerProfile } from '../types/profile';
import { selectIsAdmin, selectUserClub, selectUserId } from '../store/userSlice';
import { useSelector } from 'react-redux';
import { Dialog, DialogContent, DialogHeader, DialogTitle } from '../components/ui/dialog';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '../components/ui/select';
import { fetchPlayerProfileById } from '../services/userService';
import PlayerProfileCard from '../components/PlayerProfileCard';
import { applyToClub, getClubApplication, getClubProfileById } from '../services/clubService';
import { ArrowLeft, Trophy, Users, Star } from 'lucide-react';
import { Badge } from '../components/ui/badge';

enum PlayerPosition {
  POSITION_FORWARD = 'POSITION_FORWARD',
  POSITION_MIDFIELDER = 'POSITION_MIDFIELDER',
  POSITION_DEFENDER = 'POSITION_DEFENDER',
  POSITION_GOALKEEPER = 'POSITION_GOALKEEPER',
}

const ClubInfo: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const isAdmin = useSelector(selectIsAdmin);
  const [club, setClub] = useState<ClubProfile | null>(null);
  const [captain, setCaptain] = useState<PlayerProfile | null>(null);
  const [players, setPlayers] = useState<PlayerProfile[] | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [hasApplied, setHasApplied] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const [isDialogOpen, setIsDialogOpen] = useState<boolean>(false);
  const [selectedPosition, setSelectedPosition] = useState<PlayerPosition | null>(null);
  const userId = useSelector(selectUserId);
  const userClub = useSelector(selectUserClub);

  const navigate = useNavigate();

  useEffect(() => {
    if (userClub?.id === parseInt(id)) {
      navigate("/clubs");
    }
    const fetchClub = async () => {
      try {
        if (!id) {
          return;
        }
        const clubResponse = await getClubProfileById(parseInt(id));
        setClub(clubResponse);

        const applicantsResponse = await getClubApplication(parseInt(id));
        setHasApplied(applicantsResponse.data.includes(userId));

        const captainResponse = await fetchPlayerProfileById(clubResponse.captainId.toString());
        setCaptain(captainResponse);

        const playerIds = clubResponse.players; // Assuming clubResponse.data.players is an array of player IDs
        const playerProfiles = await Promise.all(
  playerIds.map((player) => fetchPlayerProfileById(player.toString()))
);
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

  const handleApply = async () => {
    if (!selectedPosition) {
      toast.error('Please select a position.');
      return;
    }

    if (!userId) {
      toast.error('You need to log in to apply.');
      return;
    }

    try {
      if (!id) {
        return;
      }
      await applyToClub(parseInt(id), userId, selectedPosition);
      toast.success('Application sent successfully!');
      setHasApplied(true);
      setIsDialogOpen(false);
      setSelectedPosition(null);
    } catch (err: any) {
      console.error('Error applying to club:', err);
      toast.error('Failed to apply to club.');
    }
  };

  const handlePositionChange = (position: string) => {
    setSelectedPosition(position as PlayerPosition);
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
      <div className='flex items-center mb-6'>
        <Button variant="ghost" onClick={() => navigate(-1)} className="mr-2">
          <ArrowLeft className="h-4 w-4 mr-2" />
          Back
        </Button>
      </div>

      {/* Club Header Banner */}
      <div className="relative mb-8">
        <img
          src={`https://picsum.photos/seed/club-${club?.id}/1200/300`}
          alt={`${club?.name} banner`}
          className="w-full h-48 object-cover rounded-lg"
        />
        <div className="absolute inset-0 bg-gradient-to-t from-gray-900 via-gray-900/50 to-transparent rounded-lg" />
        <div className="absolute bottom-0 left-0 right-0 p-6">
          <div className="flex items-center gap-4">
            <div className="bg-gradient-to-br from-blue-500 to-purple-500 p-3 rounded-xl shadow-lg">
              <Users className="h-6 w-6 text-white" />
            </div>
            <div className="space-y-2">
              <h1 className="text-2xl lg:text-3xl font-bold text-white">{club?.name}</h1>
              {club?.penaltyStatus.active && (
                <Badge variant="destructive" className="bg-red-500/20 text-red-300 border border-red-500/30">
                  Blacklisted Club
                </Badge>
              )}
            </div>
          </div>
        </div>
      </div>

      {/* Club Details Grid */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6 mb-8">
        {/* Main Info */}
        <div className="lg:col-span-2 bg-gray-800 rounded-lg p-6 shadow-lg border border-gray-700">
          <h3 className="text-xl font-semibold mb-6 flex items-center">
            <Trophy className="w-5 h-5 mr-2 text-blue-400" />
            Club Information
          </h3>
          <div className="space-y-4">
            <p className="text-gray-300">{club?.clubDescription || 'No description available.'}</p>
            <div className="grid grid-cols-2 gap-4 mt-4">
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
              <p className="text-lg">
                {club?.ratingDeviation ? club.ratingDeviation.toFixed(0) : 'N/A'}
              </p>
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
                  player={player}
                />
              </div>
            ))
          ) : (
            <p className="text-gray-400">Loading player profiles...</p>
          )}
        </div>
      </div>

      {/* Apply Button */}
      {!isAdmin && !userClub && userId && (
        <div className="fixed bottom-8 right-8">
          {hasApplied ? (
            <Button disabled className="bg-green-500 hover:bg-green-600">
              Application Sent
            </Button>
          ) : (
            <Button 
              onClick={() => setIsDialogOpen(true)}
              className="bg-blue-600 hover:bg-blue-700 shadow-lg"
            >
              Apply to Join
            </Button>
          )}
        </div>
      )}

      {/* Position Selection Dialog */}
      <Dialog open={isDialogOpen} onOpenChange={setIsDialogOpen}>
        <DialogContent className="sm:max-w-[425px]">
          <DialogHeader>
            <DialogTitle>Apply to {club?.name}</DialogTitle>
          </DialogHeader>
          <div className="grid gap-4 py-4">
            <div className="flex flex-col justify-between">
              <Select onValueChange={handlePositionChange}>
                <SelectTrigger className="w-full">
                  <SelectValue placeholder="Select your preferred position" />
                </SelectTrigger>
                <SelectContent>
                  {Object.values(PlayerPosition).map((position) => (
                    <SelectItem key={position} value={position}>
                      {position.replace('POSITION_', '').charAt(0) +
                        position.replace('POSITION_', '').slice(1).toLowerCase()}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
          </div>
          <div className="flex flex-col sm:flex-row justify-between mt-4 space-y-2 sm:space-y-0 sm:space-x-2">
            <Button
              variant="secondary"
              onClick={() => setIsDialogOpen(false)}
              className="w-full"
            >
              Cancel
            </Button>
            <Button
              onClick={handleApply}
              className="w-full"
              disabled={!selectedPosition}
            >
              Apply
            </Button>
          </div>
        </DialogContent>
      </Dialog>
    </div>
  );
};

export default ClubInfo;