import { useState, useEffect } from 'react'
import { PlayerPosition, PlayerProfile, UserPublicDetails } from '../types/profile'
import { fetchPlayerProfileById, fetchUserPublicInfoById, updatePlayerProfile } from '../services/userService'
import { getClubByPlayerId } from '../services/clubService'
import { Club } from '../types/club'
import { useNavigate, useParams } from 'react-router-dom'
import { Button } from './ui/button'
import { Card, CardContent, CardHeader, CardTitle } from './ui/card'
import { ArrowLeft, Pencil, Trophy, User, Star } from 'lucide-react'
import { getTournamentsHosted } from '../services/tournamentService'
import { Tournament } from '../types/tournament'
import TournamentCard from './TournamentCard'
import { selectUserId } from '../store/userSlice'
import { useSelector } from 'react-redux'
import axios from 'axios'
import NewUserGuide from './NewUserGuide'
import { Badge } from './ui/badge'

export default function ViewProfile() {
  const navigate = useNavigate()
  let userId = useSelector(selectUserId)

  const { id } = useParams<{ id: string }>()

  userId = id ? id : userId

  const [playerProfile, setPlayerProfile] = useState<PlayerProfile | null>(null)
  const [viewedUser, setViewedUser] = useState<UserPublicDetails | null>(null)
  const [club, setClub] = useState<Club | null>(null)
  const [preferredPositions, setPreferredPositions] = useState<PlayerPosition[]>([])
  const [profileDescription, setProfileDescription] = useState('')
  const [loading, setLoading] = useState(true)
  const [tournamentsHosted, setTournamentsHosted] = useState<Tournament[] | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [showNewUserGuide, setShowNewUserGuide] = useState(false)
  const loggedInUserId = useSelector(selectUserId);

  useEffect(() => {
    if (!userId) {
      setError('User not found');
      setLoading(false);
      return;
    }

    const fetchUserProfile = async () => {
      try {
        const viewedUser = await fetchUserPublicInfoById(userId);
        setViewedUser(viewedUser);

        try {
          const playerProfile = await fetchPlayerProfileById(userId);
          setPlayerProfile(playerProfile);
          console.log('Set Player Profile:', playerProfile);

          setPreferredPositions(playerProfile.preferredPositions || []);
          setProfileDescription(playerProfile.profileDescription || '');

          // Only show NewUserGuide for users with a PlayerProfile and no description
          if (playerProfile && !playerProfile.profileDescription && parseInt(userId) === loggedInUserId) {
            setShowNewUserGuide(true);
          }

          console.log('Profile Description:', playerProfile?.profileDescription);
        } catch (err) {
          if (axios.isAxiosError(err) && err.response?.status === 404) {
            console.warn('No PlayerProfile found. This user might be a host.');
            setPlayerProfile(null); // Handle as a host without a PlayerProfile
          } else {
            throw err;
          }
        }

        const hostResponse = await getTournamentsHosted(parseInt(userId));
        setTournamentsHosted(hostResponse);

        try {
          const clubResponse = await getClubByPlayerId(parseInt(userId));
          setClub(clubResponse);
        } catch (err) {
          if (axios.isAxiosError(err) && err.response?.status === 404) {
            console.warn('Club not found for the player, which is expected for some users.');
            setClub(null);
          } else {
            console.error('Error fetching club data:', err);
            setError('Failed to load club data');
          }
        }

      } catch (err) {
        if (axios.isAxiosError(err)) {
          console.error('Error fetching user data:', err);
          setError('Failed to load user data');
        }
      } finally {
        setLoading(false);
      }
    };

    fetchUserProfile();
  }, [userId]);


  const handleNewUserGuideComplete = async (description: string, positions: PlayerPosition[]) => {
    try {
      await updatePlayerProfile(parseInt(userId), positions, description)
      setProfileDescription(description)
      setPreferredPositions(positions)
      setShowNewUserGuide(false)
      navigate('/clubs')
    } catch (err) {
      console.error('Error updating profile:', err)
      setError('Failed to update profile')
    }
  }

  const handleNewUserGuideSkip = async () => {
    try {
      await updatePlayerProfile(parseInt(userId), [], 'Just a soccer player')
      setProfileDescription('Just a soccer player')
      setPreferredPositions([])
      setShowNewUserGuide(false)
      navigate('/clubs')
    } catch (err) {
      console.error('Error updating profile:', err)
      setError('Failed to update profile')
    }
  }

  const formatPosition = (position: string) => {
    return position.replace('POSITION_', '').charAt(0) + position.replace('POSITION_', '').slice(1).toLowerCase()
  }

  if (loading) return <div>Loading...</div>

  if (error) return <div>Error: {error || 'Profile not found'}</div>

  return (
    <div className="max-w-7xl mx-auto pb-20">
      {showNewUserGuide && (
        <NewUserGuide
          onComplete={handleNewUserGuideComplete}
          onSkip={handleNewUserGuideSkip}
        />
      )}

      {id && (
        <div className="mb-6">
          <Button variant="ghost" onClick={() => navigate(-1)} className="mr-2">
            <ArrowLeft className="h-4 w-4 mr-2" />
            Back
          </Button>
        </div>
      )}

      {/* Profile Header */}
      <div className="bg-gray-800 rounded-lg p-6 shadow-lg border border-gray-700 mb-8">
        <div className="flex flex-col md:flex-row items-center md:items-start gap-6">
          <div className="relative">
            <img
              src={viewedUser?.profilePictureUrl || `https://picsum.photos/seed/${userId + 2000}/200/200`}
              alt={`${playerProfile ? playerProfile.username : 'User'}'s profile`}
              className="w-32 h-32 rounded-full object-cover border-4 border-gray-700"
            />

          </div>

          <div className="flex-1 text-center md:text-left">
            <div className="flex items-center justify-center md:justify-start gap-3 mb-2">
              <h1 className="text-3xl font-bold">{viewedUser ? viewedUser.username : 'User'}</h1>
              {playerProfile?.status && (
                <Badge
                  variant="destructive"
                  className="bg-red-500/20 text-red-300 border border-red-500/30"
                >
                  {playerProfile.status.replace('STATUS_', '')}
                </Badge>
              )}
              {!id && (
                <Button
                  variant="ghost"
                  size="icon"
                  onClick={() => navigate('/profile/edit')}
                  className="hover:bg-gray-700"
                >
                  <Pencil className="h-5 w-5 text-gray-400" />
                  <span className="sr-only">Edit Profile</span>
                </Button>
              )}
            </div>
            <p className="text-gray-400 mb-4">ID: {viewedUser ? viewedUser.id : 'N/A'}</p>
            <p className="text-gray-300">
              {profileDescription || 'No user description provided.'}
            </p>
          </div>
        </div>
      </div>

      {playerProfile && (
        <div className="grid gap-6 md:grid-cols-2 mb-8">
          {/* Club Information Card */}
          <Card className="bg-gray-800 border-gray-700">
            <CardHeader>
              <CardTitle className="text-xl font-semibold flex items-center gap-2">
                <Trophy className="h-5 w-5 text-blue-400" />
                Club Information
              </CardTitle>
            </CardHeader>
            <CardContent>
              {club ? (
                <div
                  className="flex items-center gap-4 p-4 bg-gray-700/50 rounded-lg cursor-pointer hover:bg-gray-700 transition-colors duration-200"
                  onClick={() => navigate(`/clubs/${club.id}`)}
                >
                  <img
                    src={`https://picsum.photos/seed/club-${club.id}/800/200`}
                    alt={`${club.name} logo`}
                    className="w-16 h-16 rounded-full object-cover border-2 border-gray-600"
                  />
                  <div>
                    <p className="font-semibold text-lg">{club.name}</p>
                    <div className="flex items-center gap-2 text-gray-300">
                      <Star className="w-4 h-4 text-yellow-500" />
                      <span>ELO: {club.elo.toFixed(0)}</span>
                    </div>
                  </div>
                </div>
              ) : (
                <div className="text-center py-8 bg-gray-700/50 rounded-lg">
                  <Trophy className="h-12 w-12 text-gray-500 mx-auto mb-3" />
                  <p className="text-gray-400 mb-4">Not associated with a club.</p>
                  <Button
                    className="bg-blue-600 hover:bg-blue-700"
                    onClick={() => navigate('/clubs')}
                  >
                    Find or Create a Club
                  </Button>
                </div>
              )}
            </CardContent>
          </Card>

          {/* Player Positions Card */}
          <Card className="bg-gray-800 border-gray-700">
            <CardHeader>
              <CardTitle className="text-xl font-semibold flex items-center gap-2">
                <User className="h-5 w-5 text-blue-400" />
                Preferred Positions
              </CardTitle>
            </CardHeader>
            <CardContent>
              {preferredPositions.length > 0 ? (
                <div className="grid grid-cols-2 gap-3">
                  {preferredPositions.map((position) => (
                    <div
                      key={position}
                      className="bg-gray-700/50 text-gray-200 rounded-lg py-2 px-4 text-center font-medium"
                    >
                      {formatPosition(position)}
                    </div>
                  ))}
                </div>
              ) : (
                <div className="text-center py-8 bg-gray-700/50 rounded-lg">
                  <User className="h-12 w-12 text-gray-500 mx-auto mb-3" />
                  <p className="text-gray-400">No preferred positions set.</p>

                </div>
              )}
            </CardContent>
          </Card>
        </div>
      )}

      {/* Hosted Tournaments Section */}
      {tournamentsHosted && tournamentsHosted.length > 0 && (
        <Card className="bg-gray-800 border-gray-700">
          <CardHeader>
            <CardTitle className="text-xl font-semibold flex items-center gap-2">
              <Trophy className="h-5 w-5 text-blue-400" />
              Hosted Tournaments
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-3">
              {tournamentsHosted.map((tournament) => (
                tournament.id && (
                  <div key={tournament.id} className="transform transition-transform duration-200 hover:scale-[1.02]">
                    <TournamentCard
                      tournament={tournament}
                    />
                  </div>
                )
              ))}
            </div>
          </CardContent>
        </Card>
      )}
    </div>
  )
}