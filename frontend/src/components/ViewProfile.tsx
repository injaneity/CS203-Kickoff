import React, { useState, useEffect } from 'react'
import { PlayerPosition, PlayerProfile, UserPublicDetails } from '../types/profile'
import { fetchPlayerProfileById, fetchUserPublicInfoById, updatePlayerProfile } from '../services/userService'
import { getClubByPlayerId } from '../services/clubService'
import { Club } from '../types/club'
import { useNavigate, useParams } from 'react-router-dom'
import { Button } from './ui/button'
import { Card, CardContent, CardHeader, CardTitle } from './ui/card'
import { ArrowLeft, Calendar, Pencil, Trophy, User } from 'lucide-react'
import { getTournamentsHosted } from '../services/tournamentService'
import { Tournament } from '../types/tournament'
import TournamentCard from './TournamentCard'
import { selectUserId } from '../store/userSlice'
import { useSelector } from 'react-redux'
import axios from 'axios'
import NewUserGuide from './NewUserGuide'

export default function Component() {
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

  useEffect(() => {
    if (!userId) {
      setError('User not found')
      setLoading(false)
      return
    }

    const fetchUserProfile = async () => {
      try {
        const viewedUser = await fetchUserPublicInfoById(userId)
        setViewedUser(viewedUser)

        const playerProfile = await fetchPlayerProfileById(userId)
        setPlayerProfile(playerProfile)
        setPreferredPositions(playerProfile.preferredPositions || [])
        setProfileDescription(playerProfile.profileDescription || '')

        if (playerProfile.profileDescription === null || playerProfile.profileDescription === '') {
          setShowNewUserGuide(true)
        }

        const hostResponse = await getTournamentsHosted(parseInt(userId))
        setTournamentsHosted(hostResponse)

        const clubResponse = await getClubByPlayerId(parseInt(userId))
        setClub(clubResponse)
      } catch (err) {
        if (axios.isAxiosError(err) && err.response?.status === 404) {
          setShowNewUserGuide(true)
        } else {
          console.error('Error fetching user data:', err)
          setError('Failed to load user data')
        }
      } finally {
        setLoading(false)
      }
    }

    fetchUserProfile()
  }, [userId])

  const handleNewUserGuideComplete = async (description: string) => {
    try {
      await updatePlayerProfile(parseInt(userId), preferredPositions, description)
      setProfileDescription(description)
      setShowNewUserGuide(false)
    } catch (err) {
      console.error('Error updating profile:', err)
      setError('Failed to update profile')
    }
  }

  const handleNewUserGuideSkip = async () => {
    try {
      await updatePlayerProfile(parseInt(userId), preferredPositions, '')
      setProfileDescription('')
      setShowNewUserGuide(false)
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
    <div className="container mx-auto p-6 max-w-4xl">
      {showNewUserGuide ? (
        <NewUserGuide
          onComplete={handleNewUserGuideComplete}
          onSkip={handleNewUserGuideSkip}
        />
      ) : (
        <>
          {id && (
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <Button variant="ghost" size="icon" onClick={() => navigate(-1)}>
                <ArrowLeft className="h-4 w-4" />
              </Button>
            </CardHeader>
          )}
          <Card className="mb-6">
            <CardContent>
              <div className="flex flex-col md:flex-row items-center md:items-start gap-6">
                <img
                  src={viewedUser?.profilePictureUrl || `/placeholder.svg?height=200&width=200`}
                  alt={`${playerProfile ? playerProfile.username : 'User'}'s profile`}
                  className="w-32 h-32 rounded-full object-cover"
                />
                <div className="text-center md:text-left">
                  <div className="flex items-center gap-2">
                    <h1 className="text-3xl font-bold">{viewedUser ? viewedUser.username : 'User'}</h1>
                    {!id && (
                      <Button
                        variant="ghost"
                        size="icon"
                        onClick={() => navigate('/profile/edit')}
                        className="p-0"
                      >
                        <Pencil className="h-5 w-5 text-muted-foreground" />
                        <span className="sr-only">Edit Profile</span>
                      </Button>
                    )}
                  </div>
                  <p className="text-muted-foreground">ID: {viewedUser ? viewedUser.id : 'N/A'}</p>
                  <div className="mt-4 flex flex-wrap gap-2 justify-center md:justify-start">
                    <p className="text-muted-foreground">
                      {profileDescription || 'No user description provided.'}
                    </p>
                  </div>
                </div>
              </div>
            </CardContent>
          </Card>
  
          {playerProfile && (
            <div className="grid gap-6 md:grid-cols-2">
              <Card>
                <CardHeader>
                  <CardTitle className="text-xl font-semibold flex items-center gap-2">
                    <Trophy className="h-5 w-5" />
                    Club Information
                  </CardTitle>
                </CardHeader>
                <CardContent>
                  {club ? (
                    <div
                      className="flex items-center gap-4 cursor-pointer"
                      onClick={() => navigate(`/clubs/${club.id}`)}
                    >
                      <img
                        src={`/placeholder.svg?height=200&width=200`}
                        alt={`${club.name} logo`}
                        className="w-16 h-16 rounded-full object-cover"
                      />
                      <div>
                        <p className="font-semibold">{club.name}</p>
                        <p className="text-sm text-muted-foreground">ELO: {club.elo.toFixed(2)}</p>
                      </div>
                    </div>
                  ) : (
                    <div className="flex flex-col items-center">
                      <p className="text-muted-foreground">Not associated with a club.</p>
                      <Button className="mt-4" onClick={() => navigate('/clubs')}>
                        Find or Create a Club
                      </Button>
                    </div>
                  )}
                </CardContent>
              </Card>
  
              <Card>
                <CardHeader>
                  <CardTitle className="text-xl font-semibold flex items-center gap-2">
                    <User className="h-5 w-5" />
                    Player Positions
                  </CardTitle>
                </CardHeader>
                <CardContent className="grid grid-cols-2 gap-2">
                  {preferredPositions.map((position) => (
                    <div
                      key={position}
                      className="bg-primary text-primary-foreground rounded-full py-1 px-3 text-sm font-medium text-center"
                    >
                      {formatPosition(position)}
                    </div>
                  ))}
                </CardContent>
              </Card>
            </div>
          )}
  
          {tournamentsHosted && tournamentsHosted.length > 0 && (
            <Card className="mt-6">
              <CardHeader>
                <CardTitle className="text-xl font-semibold flex items-center gap-2">
                  <Calendar className="h-5 w-5" />
                  Hosted Tournaments
                </CardTitle>
              </CardHeader>
              <CardContent>
                <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-3">
                  {tournamentsHosted.map((tournament) => (
                    tournament.id && (
                      <TournamentCard key={tournament.id} tournament={tournament} />
                    )
                  ))}
                </div>
              </CardContent>
            </Card>
          )}
        </>
      )}
    </div>
  )
}