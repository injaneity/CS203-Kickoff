import { useState } from 'react';
import { Button } from "../components/ui/button";
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger } from "../components/ui/dialog";
import { ScrollArea } from "../components/ui/scroll-area";
import { toast } from "react-hot-toast";
import { Crown, UserCheck } from "lucide-react";
import { transferCaptain, getClubProfileById } from '../services/clubService'; // Import the service method
import { fetchPlayerProfileById } from '../services/userService'; // Import the method for fetching player profiles

type PlayerProfile = {
  id: number;
  username: string;
  avatarUrl?: string;
  position?: string;
};

type TransferCaptaincyProps = {
  clubId: number;
  currentCaptainId: number;
};

export default function TransferCaptaincy({ clubId, currentCaptainId }: TransferCaptaincyProps) {
  const [isDialogOpen, setIsDialogOpen] = useState(false);
  const [isTransferring, setIsTransferring] = useState(false);
  const [players, setPlayers] = useState<PlayerProfile[]>([]);

  const fetchPlayers = async () => {
    try {
      console.log('Fetching players for club ID:', clubId);
      const clubResponse = await getClubProfileById(clubId);
      const playerIds = clubResponse.players;

      // Fetch player profiles and filter out the current captain
      const playerProfiles = await Promise.all(
        playerIds.map((playerId) => fetchPlayerProfileById(playerId.toString()))
      );

      const filteredPlayers = playerProfiles.filter(
        (player) => player.id !== currentCaptainId
      );

      setPlayers(filteredPlayers);
    } catch (error) {
      console.error('Error fetching players:', error);
      toast.error("Failed to fetch players. Please try again.");
    }
  };

  const handleTransferCaptaincy = async (newCaptainId: number) => {
    setIsTransferring(true);
    try {
      await transferCaptain(clubId, currentCaptainId, newCaptainId);
      toast.success("Captaincy transferred successfully!");
      setIsDialogOpen(false);
    } catch (error) {
      console.error('Error transferring captaincy:', error);
      toast.error("Failed to transfer captaincy. Please try again.");
    } finally {
      setIsTransferring(false);
    }
  };

  return (
    <Dialog open={isDialogOpen} onOpenChange={setIsDialogOpen}>
      <DialogTrigger asChild>
        <Button
          className="bg-blue-600 hover:bg-blue-700 text-white flex items-center"
          onClick={() => {
            console.log('Transfer Captaincy button clicked');
            setIsDialogOpen(true);
            fetchPlayers();
          }}
        >
          <Crown className="mr-2 h-5 w-5" />
          <span>Transfer Captaincy</span>
        </Button>
      </DialogTrigger>
      <DialogContent className="sm:max-w-[600px]">
        <DialogHeader>
          <DialogTitle>Transfer Captain</DialogTitle>
        </DialogHeader>
        <ScrollArea className="mt-4 max-h-[70vh] pr-4">
          {players.length > 0 ? (
            <div className="space-y-4">
              {players.map(player => (
                <div key={player.id} className="flex flex-col items-center p-4 bg-gray-800 rounded-lg">
                  <div className="flex justify-center items-center mb-2">
                    <img
                      src={player?.avatarUrl || `https://picsum.photos/seed/${player.id + 2000}/200/200`}
                      alt={`${player.username}'s profile`}
                      className="w-24 h-24 rounded-full object-cover border-4 border-gray-700"
                    />
                  </div>
                  <h3 className="text-lg font-semibold">{player.username}</h3>
                  <p className="text-sm text-gray-400 mb-4">{player.position || 'No position'}</p>
                  <Button
                    onClick={() => handleTransferCaptaincy(player.id)}
                    disabled={isTransferring}
                    className="w-full bg-blue-600 hover:bg-blue-700 text-white flex justify-center items-center gap-2 text-sm"
                  >
                    <UserCheck className="h-4 w-4" />
                    Make Captain
                  </Button>
                </div>
              ))}
            </div>
          ) : (
            <p className="text-gray-400">No available players to transfer captaincy.</p>
          )}
        </ScrollArea>
      </DialogContent>
    </Dialog>
  );
}
