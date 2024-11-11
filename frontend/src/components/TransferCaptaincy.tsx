import { useEffect, useState } from 'react';
import { Button } from "../components/ui/button";
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger } from "../components/ui/dialog";
import { ScrollArea } from "../components/ui/scroll-area";
import { toast } from "react-hot-toast";
import { Crown, Loader2, UserCheck } from "lucide-react";
import { transferCaptain, getClubProfileById } from '../services/clubService'; // Import the service method
import { fetchPlayerProfileById } from '../services/userService'; // Import the method for fetching player profiles
import { PlayerProfile } from '../types/profile';
import { Card, CardContent } from './ui/card';
import { Avatar, AvatarFallback, AvatarImage } from './ui/avatar';

type TransferCaptaincyProps = {
  clubId: number
  currentCaptainId: number
  setCaptain: React.Dispatch<React.SetStateAction<PlayerProfile | null>>
}

export default function TransferCaptaincy({ clubId, currentCaptainId, setCaptain }: TransferCaptaincyProps) {
  const [isDialogOpen, setIsDialogOpen] = useState(false)
  const [isLoading, setIsLoading] = useState(false)
  const [isTransferring, setIsTransferring] = useState(false)
  const [players, setPlayers] = useState<PlayerProfile[]>([])
  const [selectedPlayer, setSelectedPlayer] = useState<PlayerProfile | null>(null)

  useEffect(() => {
    if (isDialogOpen) {
      fetchPlayers()
    }
  }, [isDialogOpen])

  const fetchPlayers = async () => {
    setIsLoading(true)
    try {
      const clubResponse = await getClubProfileById(clubId)
      const playerIds = clubResponse.players

      const playerProfiles = await Promise.all(
        playerIds.map((playerId) => fetchPlayerProfileById(playerId.toString()))
      )

      const filteredPlayers = playerProfiles.filter(
        (player) => player.id !== currentCaptainId
      )

      setPlayers(filteredPlayers)
    } catch (error) {
      console.error('Error fetching players:', error)
      toast.error("Failed to fetch players. Please try again.")
    } finally {
      setIsLoading(false)
    }
  }

  const handleTransferCaptaincy = async () => {
    if (!selectedPlayer) return

    setIsTransferring(true)
    try {
      await transferCaptain(clubId, currentCaptainId, selectedPlayer.id)
      setCaptain(selectedPlayer)
      toast.success("Captaincy transferred successfully!")
      setIsDialogOpen(false)
    } catch (error) {
      console.error('Error transferring captaincy:', error)
      toast.error("Failed to transfer captaincy. Please try again.")
    } finally {
      setIsTransferring(false)
    }
  }

  return (
    <Dialog open={isDialogOpen} onOpenChange={setIsDialogOpen}>
      <DialogTrigger asChild>
        <Button
          variant="outline"
          className="bg-primary text-primary-foreground hover:bg-primary/90"
          onClick={() => setIsDialogOpen(true)}
        >
          <Crown className="mr-2 h-5 w-5" />
          <span>Transfer Captaincy</span>
        </Button>
      </DialogTrigger>
      <DialogContent className="sm:max-w-[600px] bg-[#1a1d24]">
        <DialogHeader>
          <DialogTitle>Transfer Captain</DialogTitle>
        </DialogHeader>
        <ScrollArea className="mt-6 max-h-[65vh] p-5">
          {isLoading ? (
            <div className="flex justify-center items-center h-40">
              <Loader2 className="h-8 w-8 animate-spin text-primary" />
            </div>
          ) : players.length > 0 ? (
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              {players.map(player => (
                <Card
                  key={player.id}
                  className={`cursor-pointer transition-all bg-[#1e2128] border-0 hover:bg-[#262931] ${selectedPlayer?.id === player.id ? 'ring-2 ring-primary' : ''
                    }`}
                  onClick={() => setSelectedPlayer(player)}
                >
                  <CardContent className="flex flex-col items-center p-6 gap-3">
                    <Avatar>
                      <AvatarImage
                        src={player.profilePictureUrl || `https://picsum.photos/seed/${player.id + 2000}/200/200`}
                        alt={`${player.username}'s profile`}
                        className="object-cover"
                      />
                    </Avatar>
                    <h3 className="text-lg font-medium text-white">{player.username}</h3>
                  </CardContent>
                </Card>
              ))}
            </div>
          ) : (
            <p className="text-muted-foreground text-center py-8">No available players to transfer captaincy.</p>
          )}
        </ScrollArea>
        <div className="mt-4">
          <Button
            onClick={handleTransferCaptaincy}
            disabled={isTransferring || !selectedPlayer}
            className="w-full flex items-center justify-center"
          >
            {isTransferring ? (
              <>
                <Loader2 className="h-4 w-4 animate-spin mr-2" />
                Transferring...
              </>
            ) : (
              <>
                <Crown className="h-4 w-4 mr-2" />
                Confirm Transfer
              </>
            )}
          </Button>
        </div>
      </DialogContent>
    </Dialog>
  )
}