import React, { useState, useEffect } from 'react';
import { Badge } from './ui/badge';
import { Button } from './ui/button';
import { fetchPlayerProfileById } from '../services/userService';
import { PlayerProfile, PlayerPosition, PlayerStatus } from '../types/profile';
import { useNavigate } from 'react-router-dom';
import { useSelector } from 'react-redux';
import { selectUserId, selectIsAdmin, selectPlayers } from '../store/userSlice';
import toast from 'react-hot-toast';
import { AiFillWarning } from 'react-icons/ai';

interface PlayerProfileCardProps {
  id: number;
  availability: boolean;
  needAvailability: boolean;
  player?: PlayerProfile;
  onDeleteClick?: ((playerId: number, playerUsername: string) => void) | null;
}

const PlayerProfileCard: React.FC<PlayerProfileCardProps> = ({ id, availability, needAvailability, player, onDeleteClick }) => {
  const navigate = useNavigate();
  const userId = useSelector(selectUserId);
  const isAdmin = useSelector(selectIsAdmin);
  const players = useSelector(selectPlayers);
  const [playerProfile, setPlayerProfile] = useState<PlayerProfile | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  

  // Fetch the player profile data on component mount
  useEffect(() => {
    const fetchProfile = async () => {
      try {
        const profile = player || await fetchPlayerProfileById(String(id));
        setPlayerProfile(profile);
      } catch (err) {
        setError('Failed to load player profile');
      } finally {
        setLoading(false);
      }
    };
    fetchProfile();
  }, [id, players]);

  // Helper to format the position string
  const formatPosition = (position?: PlayerPosition) => {
    if (!position) return 'No position specified';
    return position.replace('POSITION_', '').charAt(0) + position.replace('POSITION_', '').slice(1).toLowerCase();
  };

  const navigateToProfile = () => {
    if (userId === playerProfile?.id) {
      toast.success("That's your profile!");
      return;
    }
    navigate(`/player/${playerProfile?.id}`);
  };

  const isBlacklisted = playerProfile?.status === PlayerStatus.STATUS_BLACKLISTED;

  // Conditional rendering for loading, error, and profile display
  if (loading) return <div>Loading...</div>;
  if (error) return <div>{error}</div>;
  if (!playerProfile) return <div>No profile data available</div>;

  return (
    <div className="bg-gray-800 rounded-lg p-4 flex flex-col items-center space-y-4" onClick={navigateToProfile}>
      {/* Profile Image */}
      <img
        src={playerProfile?.profilePictureUrl || `https://picsum.photos/seed/${playerProfile.id + 2000}/100/100`}
        alt={`${playerProfile.username}'s profile`}
        className="w-16 h-16 rounded-full object-cover"
      />

      {/* Profile Info */}
      <div className="relative flex items-center space-x-2">
        {isBlacklisted && (
          <div className="relative group">
            <AiFillWarning className="text-red-500" style={{ fontSize: '1.5em' }} />
            <span className="absolute bottom-full mb-2 hidden group-hover:block bg-gray-700 text-white text-xs rounded px-2 py-1">
              This player is blacklisted
            </span>
          </div>
        )}
        <div className="flex flex-col items-center space-y-1">
          <h3 className="text-lg font-semibold">{playerProfile.username}</h3>
          <p className="text-sm text-gray-400">
            {playerProfile.preferredPositions.length > 0
              ? playerProfile.preferredPositions.map(formatPosition).join(', ')
              : 'No position specified'}
          </p>
        </div>
        {isBlacklisted && (
          <div className="relative group">
            <AiFillWarning className="text-red-500" style={{ fontSize: '1.5em' }} />
            <span className="absolute bottom-full mb-2 hidden group-hover:block bg-gray-700 text-white text-xs rounded px-2 py-1">
              This player is blacklisted
            </span>
          </div>
        )}
      </div>

      {/* Availability or Manage Player Button */}
      {needAvailability && (
        isAdmin ? (
          <Button
            className="w-full h-10 bg-blue-500 hover:bg-blue-600"
            onClick={(e) => {
              e.stopPropagation(); // Prevents triggering the navigateToProfile
              console.log('Manage Player clicked');
            }}
          >
            Manage Player
          </Button>
        ) : (
          <Badge
            variant={availability ? 'success' : 'destructive'}
            className="w-20 h-6 flex items-center justify-center whitespace-nowrap"
          >
            {availability ? 'Available' : 'Not Available'}
          </Badge>
        )
      )}
      {onDeleteClick && !(userId == playerProfile.id) && (
        <button
          onClick={(event) => {
            event.stopPropagation(); // Prevents the click event from bubbling up to the parent div
            onDeleteClick(playerProfile.id, playerProfile.username);
          }}
          className="bg-red-500 text-white px-4 py-2 rounded-lg hover:bg-red-600"
        >
          Remove
        </button>
      )}
    </div>
  );
};

export default PlayerProfileCard;
