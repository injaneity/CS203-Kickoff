import React, { useState } from 'react'
import { Button } from "./ui/button"
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter } from "./ui/dialog"
import { Input } from "./ui/input"
import { toast } from 'react-hot-toast'
import { verifyTournamentAsync } from '../services/tournamentService'

interface VerifyTournamentButtonProps {
  tournamentId: number
  onVerifySuccess: () => void
}

const VerifyTournamentButton: React.FC<VerifyTournamentButtonProps> = ({ tournamentId, onVerifySuccess }) => {
  const [isDialogOpen, setIsDialogOpen] = useState(false)
  const [imageUrl, setImageUrl] = useState('')

  const handleVerifyTournament = async () => {
    if (!imageUrl) {
      toast.error('Please provide a URL for the venue booking image.')
      return
    }

    try {
      await verifyTournamentAsync(tournamentId, imageUrl)
      toast.success('Verification request submitted successfully!')
      onVerifySuccess()
      setIsDialogOpen(false)
    } catch (error) {
      console.error('Error verifying tournament:', error)
      toast.error('Failed to submit verification request.')
    }
  }

  return (
    <>
      <Button
        onClick={() => setIsDialogOpen(true)}
        className="bg-green-600 hover:bg-green-700"
      >
        Verify Tournament
      </Button>

      <Dialog open={isDialogOpen} onOpenChange={setIsDialogOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Verify Tournament</DialogTitle>
          </DialogHeader>
          <div className="mt-4">
            <p>Please provide a URL for the image of the venue booking to verify this tournament.</p>
            <Input
              type="text"
              placeholder="Image URL"
              value={imageUrl}
              onChange={(e) => setImageUrl(e.target.value)}
              className="mt-2"
            />
          </div>
          <DialogFooter>
            <Button onClick={() => setIsDialogOpen(false)} variant="outline">Cancel</Button>
            <Button onClick={handleVerifyTournament} className="bg-green-600 hover:bg-green-700">
              Submit for Verification
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </>
  )
}

export default VerifyTournamentButton