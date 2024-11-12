import { useEffect, useState } from 'react'
import { useDispatch, useSelector } from 'react-redux'
import { fetchTournamentsAsync, removeClubFromTournamentAsync } from '../store/tournamentSlice'
import { AppDispatch, RootState } from '../store'
import { Search, Trophy, TriangleAlert } from 'lucide-react'
import { Input } from "../components/ui/input"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "../components/ui/select"
import { Button } from "../components/ui/button"
import { Dialog, DialogContent, DialogHeader, DialogTitle } from "../components/ui/dialog"
import { toast } from 'react-hot-toast'
import TournamentCard from '../components/TournamentCard'
import CreateTournament from '../components/CreateTournament'
import { Tournament } from '../types/tournament'
import { PlayerAvailabilityDTO } from '../types/playerAvailability'
import { getPlayerAvailability, joinTournament } from '../services/tournamentService'
import { fetchUserClubAsync, selectUserId } from '../store/userSlice'
import { PlayerProfile } from '../types/profile'
import { fetchPlayerProfileById } from '../services/userService'
import axios from 'axios'
import { useNavigate } from 'react-router-dom'

export default function Component() {
  const dispatch = useDispatch<AppDispatch>()
  const { userClub } = useSelector((state: RootState) => state.user)
  const userId = useSelector(selectUserId)
  const [playerProfile, setPlayerProfile] = useState<PlayerProfile | null>(null);
  const { tournaments, status, error } = useSelector((state: RootState) => state.tournaments)
  const [filteredTournaments, setFilteredTournaments] = useState<Tournament[]>([])
  const [searchTerm, setSearchTerm] = useState('')
  const [teamSizeFilter, setTeamSizeFilter] = useState<string | null>(null)
  const [knockoutFormatFilter, setKnockoutFormatFilter] = useState<string | null>(null)
  const [isDialogOpen, setIsDialogOpen] = useState(false)
  const [isLeaveDialogOpen, setIsLeaveDialogOpen] = useState(false)
  const [selectedTournament, setSelectedTournament] = useState<Tournament | null>(null)
  const [isCreateDialogOpen, setIsCreateDialogOpen] = useState(false)
  const [isCaptainAlertOpen, setIsCaptainAlertOpen] = useState(false)
  const [isAvailabilityDialogOpen, setIsAvailabilityDialogOpen] = useState(false)
  const [availabilityAlertMessage, setAvailabilityAlertMessage] = useState('')
  const [availabilities, setAvailabilities] = useState<PlayerAvailabilityDTO[]>([])
  const [statusFilter, setStatusFilter] = useState<'all' | 'upcoming' | 'past'>('all');

  const navigate = useNavigate();

  let isCaptain = false

  if (userClub) {
    isCaptain = userClub?.captainId === userId
  }

  useEffect(() => {
    const fetchPlayerProfile = async () => {
      try {
        const playerProfile = await fetchPlayerProfileById(userId);
        setPlayerProfile(playerProfile);
      } catch (err: unknown) {
        // Enables loading of page even if PlayerProfile does not exist
        if (axios.isAxiosError(err) && err.response?.status === 404) {
          setPlayerProfile(null);
        } else {
          console.error('Error fetching player profile:', err);

        }
      }
    }
    fetchPlayerProfile()
    dispatch(fetchTournamentsAsync())
    dispatch(fetchUserClubAsync())
  }, [dispatch])

  useEffect(() => {
    if (!tournaments) {
      setFilteredTournaments([])
      console.log('No tournaments available to filter.');
      return
    }
    let results = tournaments.filter(tournament =>
      tournament.name.toLowerCase().includes(searchTerm.toLowerCase())
    )

    if (teamSizeFilter) {
      results = results.filter(tournament => tournament.tournamentFormat === teamSizeFilter)
    }

    if (knockoutFormatFilter) {
      results = results.filter(tournament => tournament.knockoutFormat === knockoutFormatFilter)
    }

    // Set currentDate to the start of the day (12:00 AM)
    const currentDate = new Date();
    currentDate.setHours(0, 0, 0, 0);
    console.log('Current Date at 12:00 AM:', currentDate);

    if (statusFilter === 'upcoming') {
      results = results.filter(tournament =>
        new Date(tournament.startDateTime) >= currentDate
      );
      console.log('After upcoming status filter:', results);
    } else if (statusFilter === 'past') {
      results = results.filter(tournament =>
        new Date(tournament.startDateTime) < currentDate
      );
      console.log('After past status filter:', results);
    }


    setFilteredTournaments(results)
  }, [searchTerm, teamSizeFilter, knockoutFormatFilter, statusFilter, tournaments]);

  const handleSearch = (event: React.ChangeEvent<HTMLInputElement>) => {
    setSearchTerm(event.target.value)
  }

  const handleTeamSizeFilter = (value: string) => {
    setTeamSizeFilter(value === 'ALL' ? null : value)
  }

  const handleKnockoutFormatFilter = (value: string) => {
    setKnockoutFormatFilter(value === 'ALL' ? null : value)
  }

  const handleStatusFilter = (value: 'all' | 'upcoming' | 'past') => {
    setStatusFilter(value);
  };
  

  const handleJoin = async (tournament: Tournament) => {
    setSelectedTournament(tournament)
    if (isCaptain) {
      try {
        if (tournament.id === undefined) {
          throw new Error("Undefined tournament ID");
        }
        const availabilityData = await getPlayerAvailability(tournament.id)
        setAvailabilities(availabilityData)
        const requiredPlayers = tournament.tournamentFormat === "FIVE_SIDE" ? 5 : 7
        const availablePlayerCount = availabilityData.filter((player: PlayerAvailabilityDTO) => player.available).length

        if (availablePlayerCount < requiredPlayers) {
          setAvailabilityAlertMessage(
            `${requiredPlayers} available players are needed per team. Currently, you have ${availablePlayerCount} available players. Join anyway?`
          )
          setIsAvailabilityDialogOpen(true)
        } else {
          setIsDialogOpen(true)
        }
      } catch (error) {
        console.error('Error fetching player availability:', error)
        toast.error("Failed to fetch player availability. Please try again.")
      }
    } else {
      setIsCaptainAlertOpen(true)
    }
  }

  const handleLeave = (tournament: Tournament) => {
    setSelectedTournament(tournament)
    setIsLeaveDialogOpen(true)
  }

  const handleConfirmJoin = async () => {
    if (!selectedTournament) return

    if (!userClub) {
      toast.error("User club information is missing.", {
        duration: 4000,
        position: 'top-center',
      })
      return
    }

    try {
      if (!selectedTournament.id) return

      await joinTournament(userClub.id, selectedTournament.id)

      setIsDialogOpen(false)
      setSelectedTournament(null)
      setIsAvailabilityDialogOpen(false)

      toast.success(`Successfully joined ${selectedTournament.name}`, {
        duration: 3000,
        position: 'top-center',
      })

      const updatedTournaments = tournaments.map(t =>
        t.id === selectedTournament.id ? { ...t, joinedClubIds: [...(t.joinedClubIds || []), userClub.id] } : t
      );
      dispatch({ type: 'tournaments/updateTournaments', payload: updatedTournaments });


    } catch (err: any) {
      toast.error(`${err.response.data}`, {
        duration: 4000,
        position: 'top-center',
      })
    }
  }

  const handleConfirmLeave = async () => {
    if (!selectedTournament || !userClub || !selectedTournament.id) return
    try {
      await dispatch(removeClubFromTournamentAsync({
        tournamentId: selectedTournament.id,
        clubId: userClub.id
      })).unwrap()

      setIsLeaveDialogOpen(false)
      setSelectedTournament(null)

      toast.success(`Successfully left ${selectedTournament.name}`, {
        duration: 3000,
        position: 'top-center',
      })

      const updatedTournaments = tournaments.map(t =>
        t.id === selectedTournament.id
          ? {
            ...t,
            joinedClubIds: (t.joinedClubIds || []).filter(club => club !== userClub.id)
          }
          : t
      )
      dispatch({ type: 'tournaments/updateTournaments', payload: updatedTournaments })

    } catch (err: any) {
      console.error('Error leaving tournament:', err)
      toast.error(`${err.message}`, {
        duration: 4000,
        position: 'top-center',
      })
    }
  }

  const isTournamentStarted = (tournament: Tournament) => {
    return tournament.bracket !== undefined && tournament.bracket !== null
  }

  if (status === 'loading') return <div>Loading...</div>
  if (status === 'failed') return <div>Error: {error}</div>

  return (
    <>
      {/* Notification Card for Users Without a Club */}
      {playerProfile && !userClub && (
        <div className="relative mb-8">
          <div className="absolute inset-0 bg-gradient-to-r from-red-600/20 to-orange-600/20 rounded-lg backdrop-blur-sm" />
          <div className="relative bg-gray-800/40 rounded-lg border border-gray-700/50 backdrop-blur-sm">
            <div className="px-6 py-8">
              <div className="flex items-center gap-6">
                <div className="bg-gradient-to-br from-red-500 to-orange-500 p-4 rounded-xl shadow-lg">
                <TriangleAlert className="h-8 w-8 text-white" />
                </div>
                <div className="space-y-2">
                  <h2 className="text-2xl lg:text-3xl font-bold text-white">Join a Club to Participate</h2>
                  <p className="text-gray-300">You must join a club before you can join a tournament.</p>
                  <a
                    onClick={() => navigate("/clubs")}
                    className="inline-block bg-gradient-to-r from-red-500 to-orange-500 text-white px-4 py-2 rounded-lg font-medium hover:from-red-600 hover:to-orange-600 hover:text-white transition-colors"
                  >
                    Browse Clubs
                  </a>
                </div>
              </div>
            </div>
          </div>
        </div>
      )}
      <div className="relative mb-8">
        <div className="absolute inset-0 bg-gradient-to-r from-blue-600/20 to-purple-600/20 rounded-lg backdrop-blur-sm" />
        <div className="relative bg-gray-800/40 rounded-lg border border-gray-700/50 backdrop-blur-sm">
          <div className="px-6 py-8">
            <div className="flex items-center gap-6">
              <div className="bg-gradient-to-br from-blue-500 to-purple-500 p-4 rounded-xl shadow-lg">
                <Trophy className="h-8 w-8 text-white" />
              </div>
              <div className="space-y-1">
                <h1 className="text-2xl lg:text-3xl font-bold text-white">Soccer Tournaments</h1>
                <p className="text-gray-300">{filteredTournaments.length} tournaments available</p>
              </div>
            </div>
          </div>
        </div>
      </div>
      <div className="flex flex-col lg:flex-row justify-between items-start lg:items-center space-y-2 lg:space-y-0 lg:space-x-4 mb-6">
        <div className="flex flex-col lg:flex-row space-y-2 lg:space-y-0 lg:space-x-4 w-full lg:w-auto">
          <div className="relative w-full lg:w-[300px]">
            <Search className="absolute left-2 top-2.5 h-4 w-4 text-gray-500" />
            <Input
              type="search"
              placeholder="Search tournaments"
              className="pl-8 bg-gray-800 border-gray-700 w-full h-10"
              value={searchTerm}
              onChange={handleSearch}
            />
          </div>
            <Select onValueChange={(value) => handleStatusFilter(value as 'all' | 'upcoming' | 'past')}>
              <SelectTrigger className="w-full lg:w-[180px] bg-gray-800 border-gray-700 text-white">
                <SelectValue placeholder="Upcoming / Past" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="all">All Statuses</SelectItem>
                <SelectItem value="upcoming">Upcoming</SelectItem>
                <SelectItem value="past">Past</SelectItem>
              </SelectContent>
            </Select>
            <Select onValueChange={handleTeamSizeFilter}>
              <SelectTrigger className="w-full lg:w-[180px] bg-gray-800 border-gray-700 text-white">
                <SelectValue placeholder="Team Size" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="ALL">All Sizes</SelectItem>
                <SelectItem value="FIVE_SIDE">Five-a-side</SelectItem>
                <SelectItem value="SEVEN_SIDE">Seven-a-side</SelectItem>
              </SelectContent>
            </Select>
            <Select onValueChange={handleKnockoutFormatFilter}>
              <SelectTrigger className="w-full lg:w-[180px] bg-gray-800 border-gray-700 text-white">
                <SelectValue placeholder="Knockout Format" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="ALL">All Formats</SelectItem>
                <SelectItem value="SINGLE_ELIM">Single Elimination</SelectItem>
                {/* <SelectItem value="DOUBLE_ELIM">Double Elimination</SelectItem> */}
              </SelectContent>
            </Select>
          </div>

        {userId && (
          <Button onClick={() => setIsCreateDialogOpen(true)} className="bg-blue-600 hover:bg-blue-700 w-full lg:w-auto">
            Create Tournament
          </Button>
        )}
      </div>
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4 lg:gap-6">
        {filteredTournaments.map((tournament) => {
          const isUserClubInTournament = userClub?.id !== undefined && tournament.joinedClubIds?.includes(userClub?.id);
          const hasStarted = isTournamentStarted(tournament);

          const meetsEloRequirement = userClub ? tournament.maxRank > userClub?.elo && tournament.minRank < userClub?.elo : false;

          return (
            tournament?.id && (
              <TournamentCard key={tournament.id} tournament={tournament}>
                {playerProfile && (
                  !tournament.verificationStatus || tournament.verificationStatus !== 'APPROVED' ? (
                    <Button
                      disabled
                      className="bg-gray-600 text-gray-300 cursor-not-allowed hover:bg-gray-600"
                    >
                      Unverified
                    </Button>
                  ) : userClub && isCaptain && (
                    <>
                      {hasStarted ? (
                        <Button
                          disabled
                          className="bg-gray-600 text-gray-300 cursor-not-allowed hover:bg-gray-600"
                        >
                          Started
                        </Button>
                      ) : isUserClubInTournament ? (
                        <Button
                          onClick={() => handleLeave(tournament)}
                          className="bg-red-500 hover:bg-red-600 text-white"
                        >
                          Leave
                        </Button>
                      ) : !meetsEloRequirement ? (
                        <Button
                          disabled
                          className="bg-gray-600 text-gray-300 cursor-not-allowed hover:bg-gray-600"
                        >
                          Ineligible
                        </Button>
                      ) : (userClub.penaltyStatus.active || userClub.penaltyStatus.hasPenalisedPlayer) ? (
                        <div className="relative group">
                          <Button
                            disabled
                            className="bg-gray-400 text-gray-300 cursor-not-allowed hover:bg-gray-400"
                          >
                            Join
                          </Button>
                          <div className="absolute bottom-full mb-2 hidden group-hover:block bg-gray-700 text-white text-xs rounded px-2 py-1">
                            Unable to join tournament due to blacklisted club or players
                          </div>
                        </div>
                      ) : tournament.joinedClubIds && tournament.joinedClubIds.length >= tournament.maxTeams ? (
                        <Button
                          disabled
                          className="bg-gray-600 text-gray-300 cursor-not-allowed hover:bg-gray-600"
                        >
                          Full
                        </Button>
                      ) : (
                        <Button
                          onClick={() => handleJoin(tournament)}
                          className="bg-blue-600 hover:bg-blue-700 text-white"
                        >
                          Join
                        </Button>
                      )}
                    </>
                  )
                )}
              </TournamentCard>
            )
          )
        })}
      </div>
      <Dialog open={isDialogOpen} onOpenChange={setIsDialogOpen}>
        <DialogContent className="sm:max-w-[425px]">
          <DialogHeader>
            <DialogTitle>Join {selectedTournament?.name}</DialogTitle>
          </DialogHeader>
          <div className="mt-4">
            <p>Are you sure you want to join this tournament?</p>
            <p className="mt-2">Available players: {availabilities.filter(player => player.available).length}</p>
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
              onClick={handleConfirmJoin}
              className="w-full"
            >
              Confirm
            </Button>
          </div>
        </DialogContent>
      </Dialog>
      <Dialog open={isCaptainAlertOpen} onOpenChange={setIsCaptainAlertOpen}>
        <DialogContent className="sm:max-w-[425px]">
          <DialogHeader>
            <DialogTitle>Only Captains Can Join Tournaments</DialogTitle>
          </DialogHeader>
          <div className="mt-4">
            <p>Only a captain can join the tournament on behalf of the club. Please inform your captain if you wish to participate.</p>
          </div>
          <div className="flex justify-end mt-4">
            <Button onClick={() => setIsCaptainAlertOpen(false)} className="bg-blue-500 text-white">
              Okay
            </Button>
          </div>
        </DialogContent>
      </Dialog>
      <Dialog open={isAvailabilityDialogOpen} onOpenChange={setIsAvailabilityDialogOpen}>
        <DialogContent className="sm:max-w-[425px]">
          <DialogHeader>
            <DialogTitle>Insufficient Available Players</DialogTitle>
          </DialogHeader>
          <div className="mt-4">
            <p>{availabilityAlertMessage}</p>
          </div>
          <div className="flex justify-between mt-4">
            <Button onClick={() => setIsAvailabilityDialogOpen(false)} variant="secondary">
              Back
            </Button>
            <Button onClick={handleConfirmJoin}>
              Confirm
            </Button>
          </div>
        </DialogContent>
      </Dialog>
      <Dialog open={isLeaveDialogOpen} onOpenChange={setIsLeaveDialogOpen}>
        <DialogContent className="sm:max-w-[425px]">

          <DialogHeader>
            <DialogTitle>Leave {selectedTournament?.name}</DialogTitle>
          </DialogHeader>
          <div className="mt-4">
            <p>Are you sure you want to leave this tournament?</p>
          </div>
          <div className="flex flex-col sm:flex-row justify-between mt-4 space-y-2 sm:space-y-0 sm:space-x-2">
            <Button
              variant="secondary"
              onClick={() => setIsLeaveDialogOpen(false)}
              className="w-full"
            >
              Cancel
            </Button>
            <Button
              onClick={handleConfirmLeave}
              className="w-full"
            >
              Confirm
            </Button>
          </div>
        </DialogContent>
      </Dialog>
      <CreateTournament isOpen={isCreateDialogOpen} onClose={setIsCreateDialogOpen} />
    </>
  )
}