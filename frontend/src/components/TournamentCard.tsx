import { Card, CardContent, CardFooter } from "./ui/card"
import { useNavigate } from "react-router-dom"
import { useSelector } from 'react-redux'
import { selectIsAdmin } from '../store/userSlice'
import { Button } from "./ui/button"
import { CheckCircle } from 'lucide-react'
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

export default function TournamentCard({ tournament, children }: TournamentCardProps) {
  const navigate = useNavigate()
  const isAdmin = useSelector(selectIsAdmin)

  // Guard clause to prevent rendering with invalid tournament data
  if (!tournament || !tournament.id) {
    return null
  }

  const handleCardClick = () => {
    navigate(`/tournaments/${tournament.id}`)
  }

  return (
    <Card className="bg-gray-800 rounded-lg overflow-hidden shadow-lg">
      <CardContent className="p-0" onClick={handleCardClick}>
        <img 
          src={`https://picsum.photos/seed/${tournament.id + 1000}/400/300`} 
          alt={tournament.name} 
          className="w-full h-48 object-cover rounded-t-lg" 
        />
        <div className="p-4 space-y-2">
          <h3 className="text-lg font-semibold text-white">{tournament.name}</h3>
          {tournament.verificationStatus === 'APPROVED' && (
            <Badge variant="success" className="bg-green-600 text-white">
              <CheckCircle className="w-4 h-4 mr-1" />
              Verified
            </Badge>
          )}
          <p className="text-sm text-gray-300">
            {new Date(tournament.startDateTime).toLocaleDateString()} - {new Date(tournament.endDateTime).toLocaleDateString()}
          </p>
          <p className="text-sm text-gray-300">
            Format: {formatTournamentFormat(tournament.tournamentFormat)}
          </p>
        </div>
      </CardContent>
      <CardFooter className="flex justify-between items-center p-4 border-t border-gray-700">
        <div className="flex items-center space-x-2 text-gray-300">
          <svg className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z" />
          </svg>
          <span>{`${tournament.joinedClubsIds?.length || 0}/${tournament.maxTeams} Teams`}</span>
        </div>
        {isAdmin ? (
          <ManageTournamentButton 
            tournament={tournament} 
            onActionComplete={() => {}} 
          />
        ) : (
          <div>
            {children}
          </div>
        )}
      </CardFooter>
    </Card>
  )
}