import { Card, CardContent, CardFooter } from "./ui/card"
import { useNavigate } from "react-router-dom"
import { useSelector } from 'react-redux'
import { selectIsAdmin } from '../store/userSlice'
import { CheckCircle, MapPin, Calendar, Trophy, Star } from 'lucide-react'
import { Badge } from './ui/badge'
import ManageTournamentButton from './ManageTournamentButton'
import { Tournament } from '../types/tournament'

interface TournamentCardProps {
  tournament: Tournament;
  children?: React.ReactNode;
}

const formatTournamentFormat = (format: string): string => {
  switch (format) {
    case 'FIVE_SIDE':
      return 'Five-a-side'
    case 'SEVEN_SIDE':
      return 'Seven-a-side'
    default:
      return format
  }
}

const formatDate = (dateString: string): string => {
  const date = new Date(dateString)
  return date.toLocaleDateString('en-US', { 
    weekday: 'short',
    month: 'short', 
    day: 'numeric'
  })
}

export default function TournamentCard({ tournament, children }: TournamentCardProps) {
  const navigate = useNavigate()
  const isAdmin = useSelector(selectIsAdmin)

  if (!tournament || !tournament.id) {
    return null
  }

  const handleCardClick = () => {
    navigate(`/tournaments/${tournament.id}`)
  }

  return (
    <Card className="bg-gray-800 rounded-lg overflow-hidden shadow-lg hover:shadow-2xl transition-all duration-300 border border-gray-700 flex flex-col justify-between">
  <CardContent className="p-0" onClick={handleCardClick}>
    <div className="relative">
      <img 
        src={`https://picsum.photos/seed/${tournament.id + 1000}/400/300`} 
        alt={tournament.name} 
        className="w-full h-48 object-cover" 
      />
      {tournament.verificationStatus === 'APPROVED' && (
        <Badge variant="success" className="absolute top-3 right-3 bg-green-600 text-white">
          <CheckCircle className="w-4 h-4 mr-1" />
          Verified
        </Badge>
      )}
    </div>
    
    <div className="p-5 space-y-4">
      <div>
        <h3 className="text-xl font-bold text-white mb-2">{tournament.name}</h3>
        <div className="flex flex-wrap gap-2">
          <Badge className="bg-blue-600/20 text-blue-400 border border-blue-500/30">
            {formatTournamentFormat(tournament.tournamentFormat)}
          </Badge>
          <Badge className="bg-purple-600/20 text-purple-400 border border-purple-500/30 hover:bg-purple-600/40">
            {tournament.knockoutFormat.replace('_', ' ')}
          </Badge>
        </div>
      </div>

      <div className="space-y-2 text-gray-300">
        <div className="flex items-center gap-2">
          <Calendar className="w-4 h-4 text-gray-400" />
          <span className="text-sm">
            {formatDate(tournament.startDateTime)} - {formatDate(tournament.endDateTime)}
          </span>
        </div>

        <div className="flex items-center gap-2">
          <MapPin className="w-4 h-4 text-gray-400" />
          <span className="text-sm">
            {tournament.location?.name || 'Location TBD'}
          </span>
        </div>

        <div className="flex items-center gap-2">
          <Star className="w-4 h-4 text-gray-400" />
          <span className="text-sm">
            Elo Range: {tournament.minRank} - {tournament.maxRank}
          </span>
        </div>

        {tournament.prizePool && tournament.prizePool.length > 0 && (
          <div className="flex items-center gap-2">
            <Trophy className="w-4 h-4 text-yellow-500" />
            <span className="text-sm text-yellow-500">
              Prize pool: ${tournament.prizePool[0]}
            </span>
          </div>
        )}
      </div>
    </div>
  </CardContent>
  
  <CardFooter className="px-5 py-4 border-t border-gray-700 bg-gray-800/50">
    <div className="w-full flex justify-between items-center">
      <div className="flex items-center text-gray-300">
        <svg className="h-5 w-5 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z" />
        </svg>
        <span>{`${tournament.joinedClubIds?.length || 0}/${tournament.maxTeams} Teams`}</span>
      </div>
      {isAdmin ? (
        <ManageTournamentButton 
          tournament={tournament} 
          onActionComplete={() => {}} 
        />
      ) : (
        children
      )}
    </div>
  </CardFooter>
</Card>
  )
}