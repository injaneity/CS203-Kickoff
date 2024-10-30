import { Card, CardContent, CardFooter } from "./ui/card";
import { useNavigate } from "react-router-dom";
import { useSelector } from 'react-redux';
import { selectIsAdmin } from '../store/userSlice'; 
import { Button } from "./ui/button"; 
import { CheckCircle } from 'lucide-react';
import { Badge } from './ui/badge';

interface TournamentCardProps {
  id: number;
  name: string;
  startDate: string;
  endDate: string;
  format: string;
  teams: string;
  image: string;
  isVerified: boolean; // Ensure isVerified is included in the props
  children?: React.ReactNode;
}

const formatTournamentFormat = (format: string): string => {
  switch (format) {
    case 'FIVE_SIDE':
      return 'Five-a-side';
    case 'SEVEN_SIDE':
      return 'Seven-a-side';
    default:
      return format;
  }
};

export default function TournamentCard({ id, name, startDate, endDate, format, teams, image, isVerified, children }: TournamentCardProps) { // Add isVerified here
  const navigate = useNavigate();
  const isAdmin = useSelector(selectIsAdmin);

  const handleCardClick = () => {
    navigate(`/tournaments/${id}`);
  }

  return (
    <Card className="bg-gray-800 rounded-lg overflow-hidden shadow-lg">
      <CardContent className="p-0" onClick={handleCardClick}>
        <img src={image} alt={name} className="w-full h-48 object-cover rounded-t-lg" />
        <div className="p-4 space-y-2">
          <h3 className="text-lg font-semibold text-white">{name}</h3>
          {isVerified && ( // Conditionally render the badge if verified
            <Badge variant="success" className="bg-green-600 text-white">
              <CheckCircle className="w-4 h-4 mr-1" />
              Verified
            </Badge>
          )}
          <p className="text-sm text-gray-300">{startDate} - {endDate}</p>
          <p className="text-sm text-gray-300">Format: {formatTournamentFormat(format)}</p>
        </div>
      </CardContent>
      <CardFooter className="flex justify-between items-center p-4 border-t border-gray-700">
        <div className="flex items-center space-x-2 text-gray-300">
          <svg className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z" />
          </svg>
          <span>{teams} Teams</span>
        </div>
        {isAdmin ? (
          <Button className="bg-blue-500 hover:bg-blue-600">
            Manage Tournament
          </Button>
        ) : (
          <div>
            {children}
          </div>
        )}
      </CardFooter>
    </Card>
  );
}
