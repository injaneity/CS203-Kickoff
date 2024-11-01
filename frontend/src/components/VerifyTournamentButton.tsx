import React, { useState } from 'react'
import { Button } from "./ui/button"
import { Dialog, DialogContent, DialogHeader, DialogTitle} from "./ui/dialog"
import { Input } from "./ui/input"
import { toast } from 'react-hot-toast'
import { verifyTournamentAsync } from '../services/tournamentService'
import Slider from './ui/slider'
import { Tournament } from '../types/tournament'

interface VerifyTournamentButtonProps {
  tournamentId: number;
  tournament: Tournament | null;
  onVerifySuccess: () => void;
}

const VerifyTournamentButton: React.FC<VerifyTournamentButtonProps> = ({ tournamentId, tournament, onVerifySuccess }) => {
  const [isDialogOpen, setIsDialogOpen] = useState(false)
  const [venueBooked, setVenueBooked] = useState('no') 
  const [confirmationUrl, setConfirmationUrl] = useState('') 

  const handleVerifyTournament = async () => {
    if (!confirmationUrl) {
      toast.error('Please provide a URL for the booking confirmation image.')
      return
    }

    try {
      const verificationData = {
        venueBooked: venueBooked === 'yes',
        confirmationUrl: confirmationUrl,
      }
      await verifyTournamentAsync(tournamentId, verificationData)
      toast.success('Verification request submitted successfully!')
      onVerifySuccess()
      setIsDialogOpen(false)
    } catch (error) {
      console.error('Error verifying tournament:', error)
      toast.error('Failed to submit verification request.')
    }
  }

  const getVerificationButton = (status?: string) => {
    switch (status) {
      case 'PENDING':
        return (
          <Button
            disabled
            className="bg-yellow-600 cursor-not-allowed"
          >
            Verification Pending
          </Button>
        )
      case 'APPROVED':
        return (
          <Button
            disabled
            className="bg-green-600 cursor-not-allowed"
          >
            Verified
          </Button>
        )
      default:
        return (
          <Button
            onClick={() => setIsDialogOpen(true)}
            className="bg-blue-600 hover:bg-blue-700"
          >
            Verify Tournament
          </Button>
        )
    }
  }

  return (
    <>
      {getVerificationButton(tournament?.verificationStatus)}

      <Dialog open={isDialogOpen} onOpenChange={setIsDialogOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Verify Tournament</DialogTitle>
          </DialogHeader>
          <div className="mt-4 space-y-4">
            <p>Have you booked the venue for the tournament duration?</p>
            <Slider selected={venueBooked} onChange={setVenueBooked} />
            
            <p>Please provide a link with the uploaded booking confirmation picture:</p>
            <Input
              type="text"
              placeholder="Confirmation URL"
              value={confirmationUrl}
              onChange={(e) => setConfirmationUrl(e.target.value)}
            />           
          </div>
          <div className="flex justify-end mt-6 space-x-4">
            <Button onClick={() => setIsDialogOpen(false)} variant="ghost">
              Cancel
            </Button>
            <Button onClick={handleVerifyTournament} className="bg-green-600 hover:bg-green-700">
              Submit for Verification
            </Button>
          </div>
        </DialogContent>
      </Dialog>
    </>
  )
}

export default VerifyTournamentButton
