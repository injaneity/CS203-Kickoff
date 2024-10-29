import React, { useState } from 'react'
import { Button } from "./ui/button"
import { Dialog, DialogContent, DialogHeader, DialogTitle} from "./ui/dialog"
import { Input } from "./ui/input"
import { toast } from 'react-hot-toast'
import { verifyTournamentAsync } from '../services/tournamentService'

interface VerifyTournamentButtonProps {
  tournamentId: number
  onVerifySuccess: () => void
}

const VerifyTournamentButton: React.FC<VerifyTournamentButtonProps> = ({ tournamentId, onVerifySuccess }) => {
  const [isDialogOpen, setIsDialogOpen] = useState(false)
  const [image, setImage] = useState<File | null>(null)

  const handleFileChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    if (event.target.files && event.target.files[0]) {
      setImage(event.target.files[0])
    }
  }

  const handleVerifyTournament = async () => {
    if (!image) {
      toast.error('Please upload an image of the venue booking.')
      return
    }

    try {
      const formData = new FormData()
      formData.append('image', image)
      formData.append('tournamentId', tournamentId.toString())

      await verifyTournamentAsync(formData)
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
            <p>Please upload an image of the venue booking to verify this tournament.</p>
            <Input
              type="file"
              accept="image/*"
              onChange={handleFileChange}
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