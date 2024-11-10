import React, { useEffect, useState } from 'react';
import { useNavigate, useParams, Link } from 'react-router-dom';
import { toast } from 'react-hot-toast';
import { AppDispatch } from '../store';
import { Button } from "../components/ui/button";
import { Badge } from "../components/ui/badge";
import { Dialog, DialogContent, DialogHeader, DialogTitle } from "../components/ui/dialog";
import UpdateTournament from '../components/UpdateTournament';
import AvailabilityButton from '../components/AvailabilityButton';
import ShowWinners from '../components/ShowWinners';

import { Tournament, TournamentUpdate } from '../types/tournament';
import { useDispatch, useSelector } from 'react-redux';
import { removeClubFromTournamentAsync, updateTournamentAsync } from '../store/tournamentSlice';
import { PlayerAvailabilityDTO } from '../types/playerAvailability';
import ShowAvailability from '../components/ShowAvailability';
import { fetchTournamentById, getPlayerAvailability, updatePlayerAvailability, startTournament } from '../services/tournamentService';
import VerifyTournamentButton from '../components/VerifyTournamentButton';
import { getClubProfileById } from '../services/clubService'
import { fetchUserClubAsync, selectUserClub, selectUserId, selectIsAdmin } from '../store/userSlice'

import { Club, ClubProfile } from '../types/club';
import { fetchUserPublicInfoById } from '../services/userService';
import TournamentBracket from '../components/TournamentBracket';

import { ArrowLeft, Calendar, CheckCircle, MapPin, Trophy, Users } from 'lucide-react'
import ManageTournamentButton from '../components/ManageTournamentButton';

const TournamentPage: React.FC = () => {
  const navigate = useNavigate();
  const dispatch = useDispatch<AppDispatch>();

  const isAdmin = useSelector(selectIsAdmin);

  useEffect(() => {
    dispatch(fetchUserClubAsync());
  }, [dispatch])

  const [isRemoveDialogOpen, setIsRemoveDialogOpen] = useState(false);
  const [clubToRemove, setClubToRemove] = useState<Club | null>(null);

  // State for Update Tournament Dialog
  const [isUpdateDialogOpen, setIsUpdateDialogOpen] = useState(false);
  const [initialUpdateData, setInitialUpdateData] = useState<TournamentUpdate | null>(null);
  const [availabilities, setAvailabilities] = useState<PlayerAvailabilityDTO[]>([]);
  const [isAvailabilityDialogOpen, setIsAvailabilityDialogOpen] = useState(false);
  const [joinedClubsProfiles, setJoinedClubsProfiles] = useState<ClubProfile[] | null>(null);
  const [hostUsername, setHostUsername] = useState('');
  const [isWinnersModalOpen, setIsWinnersModalOpen] = useState(false);
  const [winningClub, setWinningClub] = useState<Club | null>(null);


  const { id } = useParams<{ id: string }>();
  const tournamentId = id ? parseInt(id, 10) : null;
  const userId = useSelector(selectUserId);
  const userClub: Club | null = useSelector(selectUserClub);

  const [selectedTournament, setSelectedTournament] = useState<Tournament | null>(null);
  const [status, setStatus] = useState<'idle' | 'loading' | 'succeeded' | 'failed'>('idle');
  const [error, setError] = useState<string | null>(null);

  const isHost = selectedTournament ? selectedTournament.host === userId : false;

  let isCaptain = false;

  if (userClub) {
    isCaptain = userClub?.captainId === userId;
  }

  const tournamentFormatMap: { [key: string]: string } = {
    FIVE_SIDE: 'Five-a-side',
    SEVEN_SIDE: 'Seven-a-side'
  };

  const knockoutFormatMap: { [key: string]: string } = {
    SINGLE_ELIM: 'Single Elimination',
    DOUBLE_ELIM: 'Double Elimination'
  };


  const handleOpenRemoveDialog = (club: Club) => {
    setClubToRemove(club);
    setIsRemoveDialogOpen(true);
  };

  const handleConfirmRemove = async () => {
    if (clubToRemove && selectedTournament && selectedTournament.id) {
      await dispatch(removeClubFromTournamentAsync({
        tournamentId: selectedTournament.id,
        clubId: clubToRemove.id
      })).unwrap();
      const updatedTournamentData = await fetchTournamentById(selectedTournament.id);
      setSelectedTournament(updatedTournamentData);
      setJoinedClubsProfiles(prevProfiles => prevProfiles ?
        prevProfiles.filter(club => club.id !== clubToRemove.id)
        : prevProfiles
      );
      toast.success('Club removed successfully');
    }
    setIsRemoveDialogOpen(false);
  };

  useEffect(() => {
    if (tournamentId === null || isNaN(tournamentId)) {
      setError('Invalid tournament ID.');
      setStatus('failed');
      return;
    }

    const fetchData = async () => {
      try {
        setStatus('loading');
        const tournament = await fetchTournamentById(tournamentId);
        console.log(tournament);
        setSelectedTournament(tournament);
        if (tournament.host) {
          const hostId = tournament.host;
          const hostProfile = await fetchUserPublicInfoById(hostId.toString());
          setHostUsername(hostProfile.username);
        }


        if (tournament.joinedClubIds) {
          const clubProfilesPromises = tournament.joinedClubIds.map((id) => getClubProfileById(id));

          // Wait for all promises to resolve
          const clubProfiles = await Promise.all(clubProfilesPromises);

          setJoinedClubsProfiles(clubProfiles);
        }


        const availabilities = await getPlayerAvailability(tournamentId);
        setAvailabilities(availabilities);
        setStatus('succeeded');
      } catch (err) {
        console.error('Error fetching tournament data:', err);
        toast.error('Failed to load tournament data.');
        setStatus('failed');
        setError('Failed to load tournament data.');
      }
    };

    fetchData();
  }, [tournamentId]);

  useEffect(() => {
    if (selectedTournament?.bracket) {
      const isTournamentOver = selectedTournament.bracket.rounds.every((round) =>
        round.matches.every((match) => match.over)
      );

      if (isTournamentOver) {
        const highestRound = selectedTournament.bracket.rounds.reduce((max, round) =>
          round.roundNumber > max.roundNumber ? round : max
        );

        const finalMatch = highestRound.matches.find(match => match.over && match.winningClubId);

        if (finalMatch && joinedClubsProfiles) {
          const winner = joinedClubsProfiles.find(club => club.id === finalMatch.winningClubId);
          if (winner) {
            setWinningClub(winner);
            setIsWinnersModalOpen(true);
          }
        }
      }
    }
  }, [selectedTournament, joinedClubsProfiles]);


  const handleAvailabilityUpdate = async (availability: boolean) => {
    if (tournamentId === null || isNaN(tournamentId)) {
      toast.error('Invalid tournament ID.');
      return;
    }

    if (!userClub) {
      toast.error("You must be part of a club to mark availability.");
      return;
    }

    try {
      const payload = {
        tournamentId: tournamentId,
        playerId: userId,
        clubId: userClub.id,  // Use the fetched clubId
        available: availability
      };

      console.log('Updating availability: ', payload);
      await updatePlayerAvailability(payload);

      // Refetch or update availabilities after the change
      const updatedAvailabilities = await getPlayerAvailability(tournamentId);
      setAvailabilities(updatedAvailabilities);

      toast.success(`You have marked yourself as ${availability ? 'available' : 'not available'}.`);
    } catch (err) {
      console.error('Error updating availability:', err);
      toast.error('Failed to update your availability.');
    }
  };


  const formatDate = (dateString: string) => {
    const options: Intl.DateTimeFormatOptions = {
      year: 'numeric', month: 'long', day: 'numeric',
      hour: '2-digit', minute: '2-digit'
    };
    return new Date(dateString).toLocaleDateString(undefined, options);
  };

  const handleUpdateClick = () => {
    if (selectedTournament) {
      const initialData: TournamentUpdate = {
        name: selectedTournament.name,
        startDateTime: selectedTournament.startDateTime,
        endDateTime: selectedTournament.endDateTime,
        location: selectedTournament.location || null,
        prizePool: selectedTournament.prizePool || [],
        minRank: selectedTournament.minRank || 0,
        maxRank: selectedTournament.maxRank || 0,
      };
      setInitialUpdateData(initialData);
      setIsUpdateDialogOpen(true);
    }
  };

  const handleUpdateTournament = async (data: TournamentUpdate) => {
    if (selectedTournament === null || tournamentId === null) {
      throw new Error('Invalid tournament data.');
    }
    if (!selectedTournament.id) return;
    await dispatch(updateTournamentAsync({
      tournamentId: selectedTournament.id,
      tournamentData: data
    })).unwrap();

    const updatedTournamentData = await fetchTournamentById(tournamentId);
    setSelectedTournament(updatedTournamentData);
  };

  const handleStartTournament = async () => {
    if (!selectedTournament?.id) return;

    try {
      const updatedTournament = await startTournament(selectedTournament.id);
      setSelectedTournament(updatedTournament);
      toast.success('Tournament started successfully');
    } catch (error: any) {
      console.error('Error starting tournament:', error);
      // Log more detailed error information
      if (error.response) {
        console.error('Error response:', error.response.data);
        console.error('Error status:', error.response.status);
        toast.error(`Failed to start tournament: ${error.response.data}`);
      } else {
        toast.error('Failed to start tournament: Network error');
      }
    }
  };

  if (status === 'loading') return <div className="text-center mt-10">Loading tournament details...</div>;
  if (status === 'failed') return <div className="text-center mt-10 text-red-500">Error: {error}</div>;
  if (!selectedTournament) return <div className="text-center mt-10">No tournament found.</div>;

  return (
    <div className="max-w-7xl mx-auto pb-20">
      <div className='flex items-center mb-6'>
        <Button variant="ghost" size="sm" onClick={() => navigate(-1)} className="mr-2">
          <ArrowLeft className="h-4 w-4 mr-2" />
          Back
        </Button>
      </div>

      {/* Tournament Header Banner */}
      <div className="relative mb-8">
        <div className="absolute inset-0 bg-gradient-to-r from-blue-600/20 to-purple-600/20 rounded-lg backdrop-blur-sm" />
        <div className="relative bg-gray-800/40 rounded-lg border border-gray-700/50 backdrop-blur-sm">
          <div className="px-6 py-8">
            <div className="flex items-center gap-6">
              <div className="bg-gradient-to-br from-blue-500 to-purple-500 p-4 rounded-xl shadow-lg">
                <Trophy className="h-8 w-8 text-white" />
              </div>
              <div className="space-y-2">
                <h1 className="text-2xl lg:text-3xl font-bold text-white">{selectedTournament.name}</h1>
                <div className="flex items-center gap-3">
                  {selectedTournament.verificationStatus === 'APPROVED' && (
                    <Badge className="bg-green-500/20 text-green-300 border border-green-500/30">
                      <CheckCircle className="w-4 h-4 mr-1" />
                      Verified
                    </Badge>
                  )}
                  <Badge className="bg-blue-500/20 text-blue-300 border border-blue-500/30">
                    {tournamentFormatMap[selectedTournament.tournamentFormat]}
                  </Badge>
                  <Badge className="bg-purple-500/20 text-purple-300 border border-purple-500/30">
                    {knockoutFormatMap[selectedTournament.knockoutFormat]}
                  </Badge>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Tournament Details Card */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6 mb-8">
        <div className="lg:col-span-2 bg-gray-800 rounded-lg p-6 shadow-lg border border-gray-700">
          <h3 className="text-xl font-semibold mb-6 flex items-center">
            <Calendar className="w-5 h-5 mr-2 text-blue-400" />
            Tournament Schedule
          </h3>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div className="space-y-4">
              <div>
                <p className="text-gray-400 text-sm mb-1">Start Date & Time</p>
                <p className="text-lg">{formatDate(selectedTournament.startDateTime)}</p>
              </div>
              <div>
                <p className="text-gray-400 text-sm mb-1">Location</p>
                <p className="text-lg flex items-center">
                  <MapPin className="w-4 h-4 mr-2 text-gray-400" />
                  {selectedTournament.location?.name || 'No location specified'}
                </p>
              </div>
            </div>
            <div className="space-y-4">
              <div>
                <p className="text-gray-400 text-sm mb-1">End Date & Time</p>
                <p className="text-lg">{formatDate(selectedTournament.endDateTime)}</p>
              </div>
              <div>
                <p className="text-gray-400 text-sm mb-1">Host</p>
                <Link 
                  to={`/player/${selectedTournament.host}`}
                  className="text-lg text-blue-400 hover:text-blue-300 transition-colors duration-200 flex items-center gap-2"
                >
                  {hostUsername}
                </Link>
              </div>
            </div>
          </div>
        </div>

        <div className="bg-gray-800 rounded-lg p-6 shadow-lg border border-gray-700">
          <h3 className="text-xl font-semibold mb-6 flex items-center">
            <Trophy className="w-5 h-5 mr-2 text-yellow-500" />
            Tournament Details
          </h3>
          <div className="space-y-4">
            <div>
              <p className="text-gray-400 text-sm mb-1">Prize Pool</p>
              <p className="text-lg text-yellow-500">
                {selectedTournament.prizePool && selectedTournament.prizePool.length > 0 
                  ? `$${selectedTournament.prizePool.join(', ')}` 
                  : 'N/A'}
              </p>
            </div>
            <div>
              <p className="text-gray-400 text-sm mb-1">Elo Range</p>
              <p className="text-lg">{selectedTournament.minRank} - {selectedTournament.maxRank}</p>
            </div>
            <div>
              <p className="text-gray-400 text-sm mb-1">Team Capacity</p>
              <p className="text-lg">{`${selectedTournament.joinedClubIds?.length || 0}/${selectedTournament.maxTeams} Teams`}</p>
            </div>
          </div>
        </div>
      </div>

      {/* Joined Clubs Section */}
      <div className="bg-gray-800 rounded-lg p-6 mb-8 shadow-lg border border-gray-700">
        <h3 className="text-xl font-semibold mb-6 flex items-center">
          <Users className="w-5 h-5 mr-2 text-blue-400" />
          Participating Teams
        </h3>
        {joinedClubsProfiles && joinedClubsProfiles.length === 0 ? (
          <p className="text-gray-400">No clubs have joined this tournament yet.</p>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            {joinedClubsProfiles?.map((club: ClubProfile) => {
              const isUserClub = club.id === userClub?.id;
              return (
                <div 
                  key={club.id} 
                  onClick={() => navigate(`/clubs/${club.id}`)}
                  className="bg-gray-700/50 hover:bg-gray-700 transition-colors duration-200 rounded-lg p-4 flex items-center justify-between cursor-pointer border border-gray-600"
                >
                  <div className="flex items-center space-x-4">
                    <img
                      src={`https://picsum.photos/seed/${club.id}/100/100`}
                      alt={club.name}
                      className="w-12 h-12 rounded-full object-cover border-2 border-gray-600"
                    />
                    <div>
                      <h4 className="font-semibold">{club.name}</h4>
                    </div>
                  </div>
                  {(isHost || (isCaptain && isUserClub)) && (
                    <Button
                      onClick={(event) => {
                        event.stopPropagation();
                        handleOpenRemoveDialog(club);
                      }}
                      variant="destructive"
                      size="sm"
                    >
                      {isUserClub ? 'Leave' : 'Remove'}
                    </Button>
                  )}
                </div>
              );
            })}
          </div>
        )}
      </div>

      {/* Show Availability Section */}
      {userClub && (
        <div className="bg-gray-800 rounded-lg p-6 mb-8 shadow-lg border border-gray-700">
          <ShowAvailability
            availabilities={availabilities}
            currentUserId={userId}
            currentUserClubId={userClub.id !== null ? userClub.id : undefined}
          />
        </div>
      )}

      {/* Action Buttons */}
      <div className="flex flex-wrap items-center justify-between gap-4 mb-8">
        <div className="flex flex-wrap gap-3">
          {isHost && (
            <Button
              onClick={handleUpdateClick}
              className="bg-blue-600 hover:bg-blue-700"
            >
              Update Tournament
            </Button>
          )}

          {userClub && (
            <Button
              onClick={() => setIsAvailabilityDialogOpen(true)}
              className="bg-blue-600 hover:bg-blue-700"
            >
              Indicate Availability
            </Button>
          )}
        </div>

        <div className="flex flex-wrap gap-3 items-center">
          {isHost && (
            <>
              <VerifyTournamentButton
                tournamentId={tournamentId!}
                tournament={selectedTournament}
                onVerifySuccess={() => {
                  toast.success("Tournament verification initiated.");
                }}
              />

              {selectedTournament.joinedClubIds &&
                selectedTournament.joinedClubIds.length >= 2 &&
                !selectedTournament.bracket &&
                selectedTournament.verificationStatus === 'APPROVED' && (
                  <Button
                    onClick={handleStartTournament}
                    className="bg-green-600 hover:bg-green-700"
                  >
                    Start Tournament
                  </Button>
                )}
            </>
          )}
        </div>
      </div>

      {/* Tournament Bracket */}
      {selectedTournament.bracket && (
        <div className="bg-gray-800 rounded-lg p-6 mb-8 shadow-lg border border-gray-700">
          <h3 className="text-xl font-semibold mb-6 flex items-center">
            <Trophy className="w-5 h-5 mr-2 text-yellow-500" />
            Tournament Bracket
          </h3>
          <TournamentBracket
            tournament={selectedTournament}
            isHost={isHost}
            onMatchUpdate={() => {
              if (tournamentId) {
                fetchTournamentById(tournamentId).then(setSelectedTournament);
              }
            }}
          />
        </div>
      )}

      {/* Admin Section */}
      {isAdmin && (
        <div className="mt-4">
          <ManageTournamentButton
            tournament={selectedTournament}
            onActionComplete={() => {
              fetchTournamentById(tournamentId!).then(setSelectedTournament);
              toast.success("Tournament management actions completed.");
            }}
          />
        </div>
      )}

      {/* Remove Confirmation Dialog */}
      <Dialog open={isRemoveDialogOpen} onOpenChange={setIsRemoveDialogOpen}>
        <DialogContent className="sm:max-w-[425px]">
          <DialogHeader>
            <DialogTitle>Remove {clubToRemove?.name}</DialogTitle>
          </DialogHeader>
          <div className="mt-4">
            <p> {clubToRemove?.id === userClub?.id ? `Are you sure you want to leave this tournament?` : `Are you sure you want to remove ${clubToRemove?.name} from this tournament?`}</p>
          </div>
          <div className="flex flex-col sm:flex-row justify-between mt-4 space-y-2 sm:space-y-0 sm:space-x-2">
            <Button
              variant="secondary"
              onClick={() => setIsRemoveDialogOpen(false)}
              className="w-full"
            >
              Cancel
            </Button>
            <Button
              onClick={handleConfirmRemove}
              className="w-full"
            >
              Confirm
            </Button>
          </div>
        </DialogContent>
      </Dialog>

      {/* Update Tournament Dialog */}
      <UpdateTournament
        isOpen={isUpdateDialogOpen}
        onClose={() => setIsUpdateDialogOpen(false)}
        initialData={initialUpdateData!}
        onUpdate={handleUpdateTournament}
      />

      {/* Availability Button Dialog */}
      <Dialog open={isAvailabilityDialogOpen} onOpenChange={setIsAvailabilityDialogOpen}>
        <DialogContent>
          <div>
            <AvailabilityButton
              onAvailabilitySelect={(availability: boolean) => {
                handleAvailabilityUpdate(availability);
                setIsAvailabilityDialogOpen(false);
              }}
            />
          </div>
        </DialogContent>
      </Dialog>

      {/* Show winners if the tournament has ended */}
      {!isAdmin && isWinnersModalOpen && winningClub && (
        <ShowWinners
          winningClub={winningClub}
          onClose={() => setIsWinnersModalOpen(false)}
        />
      )}
    </div>
  );
};

export default TournamentPage;
