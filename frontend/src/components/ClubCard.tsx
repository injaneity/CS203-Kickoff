import React from 'react';
import ManageClubButton from "./ManageClubButton";
import { useSelector } from 'react-redux';
import { selectIsAdmin, selectUserClub } from '../store/userSlice';
import { AiFillWarning } from 'react-icons/ai';
import { Club } from '../types/club';

interface ClubCardProps {
  club: Club;
  image: string;
  onClick: () => void;
}

const ClubCard: React.FC<ClubCardProps> = ({
  club,
  image,
  onClick,
}) => {
  const isAdmin = useSelector(selectIsAdmin);
  const userClub = useSelector(selectUserClub); 

  const isUserClub = userClub && userClub.id === club.id;

  return (
    <div
      className={`cursor-pointer ${
        isUserClub ? 'bg-gray-700' : 'bg-gray-800'
      } rounded-lg overflow-hidden shadow-md flex flex-col ${isAdmin ? 'h-74' : 'h-70'}`}
    >
      <div onClick={onClick} className="flex-grow">
        <img src={image} alt={club.name} className="w-full h-48 object-cover" />

        <div className="p-4">
          <div className="relative flex items-center space-x-2">
            {(club.penaltyStatus.hasPenalisedPlayer || club.penaltyStatus.active) && (
              <div className="relative group">
                <AiFillWarning className="text-red-500" style={{ fontSize: '2em' }} />
                <span className="absolute bottom-full mb-2 hidden group-hover:block bg-gray-700 text-white text-xs rounded px-2 py-1">
                  This club is blacklisted or contains blacklisted players
                </span>
              </div>
            )}
            <h3 className="text-xl font-bold mb-2 text-white">{club.name}</h3>
          </div>
          <p className="text-gray-400">{club.clubDescription || 'No description available.'}</p>
          <p className="text-gray-400 mt-2">{`ELO: ${club.elo.toFixed(0)}, RD: ${club.ratingDeviation.toFixed(0)}`}</p>
        </div>
      </div>

      {isAdmin && (
        <div className="p-4">
          <ManageClubButton clubId={club.id} currentPenaltyStatus={club.penaltyStatus} />
        </div>
      )}
    </div>
  );
};

export default ClubCard;