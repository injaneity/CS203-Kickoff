import React, { useState } from 'react'
import { Button } from "./ui/button"
import { Dialog, DialogContent, DialogHeader, DialogTitle} from "./ui/dialog"
import { Input } from "./ui/input"
import { toast } from 'react-hot-toast'
import { checkPaymentStatus, verifyTournamentAsync } from '../services/tournamentService'
import Slider from './ui/slider'
import { Tournament } from '../types/tournament'
import { StripeButton } from './ui/stripe-button'

interface VerifyTournamentButtonProps {
  tournamentId: number;
  tournament: Tournament | null;
  onVerifySuccess: () => void;
}

const VerifyTournamentButton: React.FC<VerifyTournamentButtonProps> = ({ tournamentId, tournament, onVerifySuccess }) => {
  const [isDialogOpen, setIsDialogOpen] = useState(false)
  const [venueBooked, setVenueBooked] = useState('no')
  const [confirmationUrl, setConfirmationUrl] = useState('')
  const [isCheckingPayment, setIsCheckingPayment] = useState(false)

  const handleCheckPaymentStatus = async () => {
    setIsCheckingPayment(true)
    try {
      const { paid, status } = await checkPaymentStatus(tournamentId)
      if (paid) {
        toast.success('Payment verified! You can now submit verification details.')
        return true
      } else {
        toast.error('Payment not yet received. Please complete payment first.')
        return false
      }
    } catch (error) {
      console.error('Error checking payment status:', error)
      toast.error('Error checking payment status')
      return false
    } finally {
      setIsCheckingPayment(false)
    }
  }

  const handleVerifyTournament = async () => {
    if (!confirmationUrl) {
      toast.error('Please provide a URL for the booking confirmation image.')
      return
    }

    const isPaid = await handleCheckPaymentStatus()
    if (!isPaid) {
      return
    }

    try {
      const verificationData = {
        venueBooked: venueBooked === 'yes',
        confirmationUrl,
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
            <div className="space-y-2">
              <p>1. Have you booked the venue for the tournament duration?</p>
              <Slider selected={venueBooked} onChange={setVenueBooked} />
            </div>
            
            <div className="space-y-2">
              <p>2. Provide a link with the uploaded booking confirmation picture:</p>
              <Input
                type="text"
                placeholder="Confirmation URL"
                value={confirmationUrl}
                onChange={(e) => setConfirmationUrl(e.target.value)}
              />
            </div>

            <div className="space-y-2">
              <p>3. Complete the verification payment:</p>
              <div className="w-full flex justify-center">
                <StripeButton tournamentId={tournamentId} />
              </div>
              <Button 
                onClick={handleCheckPaymentStatus}
                disabled={isCheckingPayment}
                variant="outline"
                className="w-full mt-2"
              >
                {isCheckingPayment ? 'Checking...' : 'Check Payment Status'}
              </Button>
            </div>
          </div>

          <div className="flex justify-end mt-6 space-x-4">
            <Button onClick={() => setIsDialogOpen(false)} variant="ghost">
              Cancel
            </Button>
            <Button 
              onClick={handleVerifyTournament}
              disabled={tournament?.verificationStatus !== 'PAYMENT_COMPLETED'}
              className="bg-green-600 hover:bg-green-700"
            >
              Submit for Verification
            </Button>
          </div>
        </DialogContent>
      </Dialog>
    </>
  )
}

export default VerifyTournamentButton
