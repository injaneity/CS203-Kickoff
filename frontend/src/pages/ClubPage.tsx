import React, { useEffect, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { fetchClubsAsync, applyToClubAsync } from '../store/clubSlice';
import { AppDispatch, RootState } from '../store';
import { Search, Trophy, Star } from 'lucide-react';
import { Input } from '../components/ui/input';
import { Dialog, DialogContent, DialogHeader, DialogTitle } from '../components/ui/dialog';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '../components/ui/select';
import { Button } from '../components/ui/button';
import { toast } from 'react-hot-toast';
import { Club } from '../types/club';
import CreateClub from '../components/CreateClub';
import { fetchUserClubAsync, selectUserId } from '../store/userSlice';
import { EloRangeSlider } from '../components/EloRangeSlider';
import { Badge } from '../components/ui/badge';
import { useNavigate } from 'react-router-dom';

enum PlayerPosition {
  POSITION_FORWARD = 'POSITION_FORWARD',
  POSITION_MIDFIELDER = 'POSITION_MIDFIELDER',
  POSITION_DEFENDER = 'POSITION_DEFENDER',
  POSITION_GOALKEEPER = 'POSITION_GOALKEEPER',
}

export default function ClubPage() {
  const dispatch = useDispatch<AppDispatch>();
  const userId = useSelector(selectUserId);
  const { clubs, status, error } = useSelector(
    (state: RootState) => state.clubs
  );
  const { userClub } = useSelector((state: RootState) => state.user);
  const [filteredClubs, setFilteredClubs] = useState<Club[]>([]);
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedClub, setSelectedClub] = useState<Club | null>(null);
  const [selectedPosition, setSelectedPosition] =
    useState<PlayerPosition | null>(null);
  const [isDialogOpen, setIsDialogOpen] = useState(false);
  const [isInfoDialogOpen, setIsInfoDialogOpen] = useState(false);
  const [isCreateDialogOpen, setIsCreateDialogOpen] = useState(false);
  const [eloRange, setEloRange] = useState<[number, number]>([0, 3000]); // Default ELO range
  const navigate = useNavigate();

  useEffect(() => {
    dispatch(fetchClubsAsync());
    dispatch(fetchUserClubAsync());
  }, [dispatch]);

  useEffect(() => {
    const results = clubs.filter((club) => {
      const matchesSearch =
        club.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
        (club.clubDescription &&
          club.clubDescription.toLowerCase().includes(searchTerm.toLowerCase()));

      const matchesElo = club.elo >= eloRange[0] && club.elo <= eloRange[1];

      return matchesSearch && matchesElo;
    });

    setFilteredClubs(results);
  }, [searchTerm, clubs, eloRange]);

  const handleSearch = (event: React.ChangeEvent<HTMLInputElement>) => {
    setSearchTerm(event.target.value);
  };

  const handleCreateClub = (newClub: Club) => {
    setFilteredClubs(prevClubs => [...prevClubs, newClub]); // Add the new club to the list
  };

  const formatPosition = (position: string) => {
    return (
      position.replace('POSITION_', '').charAt(0) +
      position.replace('POSITION_', '').slice(1).toLowerCase()
    );
  };

  const handleCardClick = (club: Club) => {
    setSelectedClub(club);
    setIsInfoDialogOpen(true);
  };

  const handleApplyClick = () => {
    setIsInfoDialogOpen(false); // Close the club info dialog
    setIsDialogOpen(true); // Open the position selection dialog
  };

  const handleApply = async () => {
    if (!selectedClub || !selectedPosition) return;

    try {
      await dispatch(
        applyToClubAsync({
          clubId: selectedClub.id,
          playerProfileId: 1, // Replace with actual player ID
          desiredPosition: selectedPosition,
        })
      ).unwrap();
      toast.success(
        `Successfully applied to ${selectedClub.name} as ${formatPosition(
          selectedPosition
        )}`,
        {
          duration: 3000,
          position: 'top-center',
        }
      );
      setIsDialogOpen(false);
      setSelectedClub(null);
      setSelectedPosition(null);
    } catch (err) {
      console.error('Error applying to club:', err);
      toast.error(`Error applying to club: ${(err as any).message}`, {
        duration: 4000,
        position: 'top-center',
      });
    }
  };

  const handlePositionChange = (position: string) => {
    setSelectedPosition(position as PlayerPosition);
  };

  const handleCreateClubClick = () => {
    setIsCreateDialogOpen(true);  // Open the CreateClub modal
  };

  if (status === 'loading') return <div>Loading...</div>;
  if (status === 'failed') return <div>Error: {error}</div>;

  return (
    <div className="max-w-7xl mx-auto pb-20">
      {/* Banner */}
      <div className="relative mb-8">
        <div className="absolute inset-0 bg-gradient-to-r from-blue-600/20 to-purple-600/20 rounded-lg backdrop-blur-sm" />
        <div className="relative bg-gray-800/40 rounded-lg border border-gray-700/50 backdrop-blur-sm">
          <div className="px-6 py-8">
            <div className="flex items-center gap-6">
              <div className="bg-gradient-to-br from-blue-500 to-purple-500 p-4 rounded-xl shadow-lg">
                <Trophy className="h-8 w-8 text-white" />
              </div>
              <div className="space-y-1">
                <h1 className="text-2xl lg:text-3xl font-bold text-white">Soccer Clubs</h1>
                <p className="text-gray-300">{filteredClubs.length} clubs available</p>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Search and Filters */}
      <div className="bg-gray-800 rounded-lg p-6 mb-8 shadow-lg border border-gray-700">
        <div className="flex flex-col lg:flex-row justify-between items-start lg:items-center space-y-4 lg:space-y-0">
          <div className="flex flex-col lg:flex-row w-full space-y-4 lg:space-y-0 lg:space-x-8">
            {/* Search Input */}
            <div className="relative w-full lg:w-[300px]">
              <Search className="absolute left-2 top-4 h-4 w-4 text-gray-500" />
              <Input
                type="search"
                placeholder="Search clubs"
                className="pl-8 bg-gray-700/50 border-gray-600 w-full"
                value={searchTerm}
                onChange={handleSearch}
              />
            </div>

            {/* ELO Range Slider */}
            <div className="w-full lg:w-[300px]">
              <EloRangeSlider
                value={eloRange}
                onValueChange={setEloRange}
              />
            </div>
          </div>

          {/* Create Club Button */}
          {userId && !userClub && (
            <Button
              onClick={handleCreateClubClick}
              className="bg-blue-600 hover:bg-blue-700 w-full lg:w-auto whitespace-nowrap"
            >
              Create Club
            </Button>
          )}
        </div>
      </div>

      {/* Club Cards Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        {filteredClubs.map((club) => (
          <div
            key={club.id}
            className="bg-gray-800 rounded-lg overflow-hidden shadow-lg hover:shadow-xl transition-all duration-300 border border-gray-700"
            onClick={() => handleCardClick(club)}
          >
            <div className="relative">
              <img
                src={`https://picsum.photos/seed/${club.id}/400/200`}
                alt={club.name}
                className="w-full h-48 object-cover"
              />
              {club.penaltyStatus.active && (
                <Badge variant="destructive" className="absolute top-3 right-3">
                  Blacklisted
                </Badge>
              )}
            </div>
            
            <div className="p-5 space-y-4">
              <div>
                <h3 className="text-xl font-bold text-white mb-2">{club.name}</h3>
                <p className="text-gray-400 text-sm line-clamp-2">
                  {club.clubDescription || 'No description available.'}
                </p>
              </div>

              <div className="space-y-2">
                <div className="flex items-center justify-between text-sm">
                  <div className="flex items-center text-gray-400">
                    <Trophy className="w-4 h-4 mr-2" />
                    <span>Members</span>
                  </div>
                  <span>{club.players.length}</span>
                </div>

                <div className="flex items-center justify-between text-sm">
                  <div className="flex items-center text-gray-400">
                    <Star className="w-4 h-4 mr-2" />
                    <span>Rating</span>
                  </div>
                  <span>
                    <span className="font-semibold text-yellow-500">{club.elo.toFixed(0)}</span>
                    <span className="text-gray-400 mx-1">±</span>
                    <span className="text-gray-300">{club.ratingDeviation.toFixed(0)}</span>
                  </span>
                </div>
              </div>
            </div>
          </div>
        ))}
      </div>

      {/* Club Info Dialog */}
      <Dialog open={isInfoDialogOpen} onOpenChange={setIsInfoDialogOpen}>
        <DialogContent className="sm:max-w-[600px] bg-gray-800 border border-gray-700">
          <DialogHeader>
            <DialogTitle className="text-xl font-bold">{selectedClub?.name}</DialogTitle>
          </DialogHeader>
          <div className="mt-4">
            {selectedClub && (
              <div className="space-y-6">
                <img
                  src={`https://picsum.photos/seed/${selectedClub.id}/600/200`}
                  alt={selectedClub.name}
                  className="w-full h-48 object-cover rounded-lg"
                />
                <div className="space-y-4">
                  <p className="text-gray-300">{selectedClub.clubDescription || 'No description available.'}</p>
                  
                  <div className="grid grid-cols-2 gap-4">
                    <div className="bg-gray-700/50 p-4 rounded-lg">
                      <p className="text-gray-400 text-sm">ELO Rating</p>
                      <p className="text-2xl font-bold text-yellow-500">{selectedClub.elo.toFixed(0)}</p>
                    </div>
                    <div className="bg-gray-700/50 p-4 rounded-lg">
                      <p className="text-gray-400 text-sm">Members</p>
                      <p className="text-2xl font-bold">{selectedClub.players.length}</p>
                    </div>
                  </div>
                </div>

                <div className="flex flex-wrap justify-between gap-3">
                  <Button 
                    variant="secondary" 
                    onClick={() => setIsInfoDialogOpen(false)}
                  >
                    Close
                  </Button>
                  <div className="flex gap-3">
                    <Button 
                      variant="default"
                      onClick={() => {
                        setIsInfoDialogOpen(false);
                        navigate(`/clubs/${selectedClub.id}`);
                      }}
                      className="bg-blue-600 hover:bg-blue-700"
                    >
                      View More Information
                    </Button>
                    {userId && !userClub && (
                      <Button 
                        onClick={handleApplyClick}
                        className="bg-green-600 hover:bg-green-700"
                      >
                        Apply to Join
                      </Button>
                    )}
                  </div>
                </div>
              </div>
            )}
          </div>
        </DialogContent>
      </Dialog>

      {/* Position Selection Dialog */}
      <Dialog open={isDialogOpen} onOpenChange={setIsDialogOpen}>
        <DialogContent className="sm:max-w-[425px] bg-gray-800 border border-gray-700">
          <DialogHeader>
            <DialogTitle>Apply to {selectedClub?.name}</DialogTitle>
          </DialogHeader>
          <div className="grid gap-4 py-4">
            <div className="flex flex-col justify-between">
              <p className="text-sm text-gray-400 mb-2">Select your preferred position</p>
              <Select onValueChange={handlePositionChange}>
                <SelectTrigger className="w-full bg-gray-700 border-gray-600">
                  <SelectValue placeholder="Choose position" />
                </SelectTrigger>
                <SelectContent>
                  {Object.values(PlayerPosition).map((position) => (
                    <SelectItem key={position} value={position}>
                      {formatPosition(position)}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
          </div>
          <div className="flex justify-end space-x-3">
            <Button variant="secondary" onClick={() => setIsDialogOpen(false)}>
              Cancel
            </Button>
            <Button
              onClick={handleApply}
              disabled={!selectedPosition}
            >
              Apply
            </Button>
          </div>
        </DialogContent>
      </Dialog>

      {/* Create Club Dialog */}
      <CreateClub
            isCreateDialogOpen={isCreateDialogOpen}
            setIsCreateDialogOpen={setIsCreateDialogOpen}
            handleClubCreated={handleCreateClub}
          />
    </div>
  );
}
