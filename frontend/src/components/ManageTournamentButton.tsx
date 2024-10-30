import React from 'react';
import { Button } from "./ui/button"; // Adjust the path if necessary
import { useNavigate } from 'react-router-dom';

interface ManageTournamentButtonProps {
  tournamentId: number;
}

const ManageTournamentButton: React.FC<ManageTournamentButtonProps> = ({ tournamentId }) => {
  const navigate = useNavigate();

  const handleManageClick = () => {
    navigate(`/tournaments/manage/${tournamentId}`); // Example path for managing a tournament
  };

  return (
    <Button onClick={handleManageClick} className="bg-blue-500 hover:bg-blue-600">
      Manage Tournament
    </Button>
  );
};

export default ManageTournamentButton;
