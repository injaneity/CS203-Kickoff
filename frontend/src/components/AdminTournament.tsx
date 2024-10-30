'use client'

import React, { useState, useEffect } from 'react'
import { useDispatch, useSelector } from 'react-redux'
import { fetchTournamentsAsync } from '../store/tournamentSlice'
import { Tournament } from '../types/tournament'
import TournamentCard from '../components/TournamentCard'
import { Input } from "../components/ui/input"
import { Search } from 'lucide-react'
import { Button } from "../components/ui/button"
import { AppDispatch, RootState } from '../store'
import { Card, CardContent, CardFooter } from "../components/ui/card"
import { toast } from 'react-hot-toast'
import { fetchPendingVerifications, approveVerification, rejectVerification } from '../services/userService'

enum TournamentFilter {
  UPCOMING = 'All Tournaments',
  CURRENT = 'Pending',
  PAST = 'Verified',
  REJECTED = 'Rejected',
}

interface Verification {
  id: number
  tournamentId: number
  tournamentName: string
  imageUrl: string
}

const AdminTournament = () => {
  const dispatch = useDispatch<AppDispatch>()
  const { tournaments } = useSelector((state: RootState) => state.tournaments)
  const [searchTerm, setSearchTerm] = useState('')
  const [tournamentFilter, setTournamentFilter] = useState<TournamentFilter>(TournamentFilter.UPCOMING)
  const [verifications, setVerifications] = useState<Verification[]>([])

  useEffect(() => {
    dispatch(fetchTournamentsAsync())
    loadVerifications()
  }, [dispatch])

  const loadVerifications = async () => {
    try {
      const pendingVerifications = await fetchPendingVerifications()
      setVerifications(pendingVerifications)
    } catch (error) {
      console.error('Error fetching verifications:', error)
      toast.error('Failed to load pending verifications.')
    }
  }

  const handleApprove = async (verificationId: number) => {
    try {
      await approveVerification(verificationId)
      toast.success('Verification approved successfully!')
      loadVerifications()
      dispatch(fetchTournamentsAsync())
    } catch (error) {
      console.error('Error approving verification:', error)
      toast.error('Failed to approve verification.')
    }
  }

  const handleReject = async (verificationId: number) => {
    try {
      await rejectVerification(verificationId)
      toast.success('Verification rejected successfully!')
      loadVerifications()
      dispatch(fetchTournamentsAsync())
    } catch (error) {
      console.error('Error rejecting verification:', error)
      toast.error('Failed to reject verification.')
    }
  }

  const filteredTournaments = tournaments.filter((tournament: Tournament) => {
    const matchesSearch = tournament.name.toLowerCase().includes(searchTerm.toLowerCase())

    switch (tournamentFilter) {
      case TournamentFilter.UPCOMING:
        return matchesSearch
      case TournamentFilter.CURRENT:
        return matchesSearch && tournament.verificationStatus === 'PENDING'
      case TournamentFilter.PAST:
        return matchesSearch && tournament.verificationStatus === 'APPROVED'
      case TournamentFilter.REJECTED:
        return matchesSearch && tournament.verificationStatus === 'REJECTED'
      default:
        return false
    }
  })

  return (
    <div>
      <h2 className="text-2xl font-bold mb-4">Manage Tournaments</h2>
      <div className="relative w-full mb-4">
        <Search className="absolute left-2 top-2.5 h-4 w-4 text-gray-500" />
        <Input
          type="search"
          placeholder="Search Tournaments"
          className="pl-8 bg-gray-800 border-gray-700 w-full"
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
        />
      </div>

      {/* Filter Buttons */}
      <div className="flex justify-center space-x-4 mb-4">
        {Object.values(TournamentFilter).map((filter) => (
          <Button
            key={filter}
            onClick={() => setTournamentFilter(filter)}
            variant={tournamentFilter === filter ? "default" : "secondary"}
          >
            {filter}
          </Button>
        ))}
      </div>

      {/* Pending Verifications */}
      {tournamentFilter === TournamentFilter.CURRENT && (
        <div className="mb-8">
          <h3 className="text-xl font-bold mb-4">Pending Verifications</h3>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            {verifications.map((verification) => (
              <Card key={verification.id} className="bg-gray-800">
                <CardContent className="p-4">
                  <h4 className="text-lg font-semibold">{verification.tournamentName}</h4>
                  <img src={verification.imageUrl} alt="Venue Booking" className="mt-2 max-w-full h-auto" />
                </CardContent>
                <CardFooter className="flex justify-end space-x-2">
                  <Button onClick={() => handleReject(verification.id)} variant="ghost">
                    Reject
                  </Button>
                  <Button onClick={() => handleApprove(verification.id)} className="bg-green-600 hover:bg-green-700">
                    Approve
                  </Button>
                </CardFooter>
              </Card>
            ))}
          </div>
        </div>
      )}

      {/* Tournament Cards */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
        {filteredTournaments.length > 0 ? (
          filteredTournaments.map((tournament: Tournament) => (
            tournament.id && 
            <TournamentCard
              key={tournament.id}
              id={tournament.id || 0}
              name={tournament.name}
              startDate={new Date(tournament.startDateTime).toLocaleDateString()}
              endDate={new Date(tournament.endDateTime).toLocaleDateString()}
              format={tournament.tournamentFormat}
              teams={`${tournament.joinedClubsIds?.length || 0}/${tournament.maxTeams}`}
              image={`https://picsum.photos/seed/${tournament.id + 1000}/400/300`}
              isVerified={tournament.verificationStatus === 'APPROVED'}
            >
              <Button className="bg-blue-500 hover:bg-blue-600 w-32 h-10">
                Manage Tournament
              </Button>
            </TournamentCard>
          ))
        ) : (
          <p>No tournaments available</p>
        )}
      </div>
    </div>
  )
}

export default AdminTournament