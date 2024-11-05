import React, { useRef, useState } from 'react'
import { Button } from "./ui/button"
import { Dialog, DialogContent, DialogHeader, DialogTitle } from "./ui/dialog"
import { toast } from 'react-hot-toast'
import { verifyTournamentAsync } from '../services/tournamentService'
import Slider from './ui/slider'
import { Tournament } from '../types/tournament'
import { fileToBase64 } from '../services/image'

interface VerifyTournamentButtonProps {
  tournamentId: number;
  tournament: Tournament | null;
  onVerifySuccess: () => void;
}

const VerifyTournamentButton: React.FC<VerifyTournamentButtonProps> = ({ tournamentId, tournament, onVerifySuccess }) => {
  const [isDialogOpen, setIsDialogOpen] = useState(false)
  const [venueBooked, setVenueBooked] = useState('no')
  const [selectedFile, setSelectedFile] = useState<File | null>(null)
  const fileInputRef = useRef<HTMLInputElement>(null)

  const handleFileChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    if (event.target.files && event.target.files[0]) {
      setSelectedFile(event.target.files[0])
    }
  }

  const handleVerifyTournament = async () => {
    if (!selectedFile) {
      toast.error('Please upload a booking confirmation image.')
      return
    }

    try {
      const base64Image = await fileToBase64(selectedFile);

      const verificationData = {
        venueBooked: venueBooked === 'yes',  // assuming `venueBooked` is either 'yes' or 'no'
        verificationImage: base64Image
      };

      await verifyTournamentAsync(tournamentId, verificationData);
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
            Verification Submitted
          </Button>
        )
      case 'APPROVED':
        return (
          <Button
            disabled
            variant="outline"
            className="text-green-500 border-green-500 cursor-not-allowed hover:bg-transparent"
          >
            Verified
          </Button>
        )
      case 'REJECTED':
        return (
          <Button
            onClick={() => setIsDialogOpen(true)}
            className="bg-red-600 hover:bg-red-700"
          >
            Resubmit Verification
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

            <p>Please upload the booking confirmation image:</p>
            <input
              type="file"
              accept="image/*"
              onChange={handleFileChange}
              className="hidden"
              ref={fileInputRef}
            />
            <Button
              onClick={() => fileInputRef.current?.click()}
              variant="outline"
              className="w-full"
            >
              {selectedFile ? selectedFile.name : 'Choose File'}
            </Button>
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
