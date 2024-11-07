import React from 'react';
import ManageClubButton from "./ManageClubButton";
import { useSelector } from 'react-redux';
import { selectIsAdmin } from '../store/userSlice';
import { ClubPenaltyStatus } from '../types/club';

interface ClubCardProps {
  id: number;
  name: string;
  description: string;
  ratings: string;
  image: string;
  applied: boolean;
  onClick: () => void;
  penaltyStatus: ClubPenaltyStatus;
}

const ClubCard: React.FC<ClubCardProps> = ({
  id,
  name,
  description,
  ratings,
  image,
  onClick,
  penaltyStatus
}) => {
  const isAdmin = useSelector(selectIsAdmin);

  return (
    <div
      onClick={onClick}
      className={`cursor-pointer bg-gray-800 rounded-lg overflow-hidden shadow-md flex flex-col ${isAdmin ? 'h-74' : 'h-70'}`} // Adjust height based on admin status
    >
      {/* Card Content */}
      <img src={image} alt={name} className="w-full h-48 object-cover" />
      <div className="p-4 flex-grow">
        <h3 className="text-xl font-bold mb-2">{name}</h3>
        <p className="text-gray-400">{description}</p>
        <p className="text-gray-400 mt-2">{ratings}</p>
      </div>
      {isAdmin && (
        <div className="p-4">
          <ManageClubButton clubId={id} currentPenaltyStatus={penaltyStatus} />
        </div>
      )}
    </div>
  );
};

export default ClubCard;
