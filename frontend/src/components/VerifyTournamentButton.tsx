import React, { useRef, useState, useEffect } from 'react'
import { Button } from "./ui/button"
import { Dialog, DialogContent, DialogHeader, DialogTitle } from "./ui/dialog"
import { toast } from 'react-hot-toast'
import { checkPaymentStatus, verifyTournamentAsync } from '../services/tournamentService'
import Slider from './ui/slider'
import { Tournament } from '../types/tournament'
import { StripeButton } from './ui/stripe-button'
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
  const [isPaid, setIsPaid] = useState(false)
  const fileInputRef = useRef<HTMLInputElement>(null)

  useEffect(() => {
    let intervalId: ReturnType<typeof setInterval>;

    const checkPayment = async () => {
      try {
        const { paid } = await checkPaymentStatus(tournamentId)
        setIsPaid(paid)
      } catch (error) {
        console.error('Error checking payment status:', error)
      }
    }
    
    if (isDialogOpen) {
      checkPayment()
      intervalId = setInterval(checkPayment, 3000)
    }

    return () => {
      if (intervalId) {
        clearInterval(intervalId)
      }
    }
  }, [tournamentId, isDialogOpen])

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
      const { paid } = await checkPaymentStatus(tournamentId)
      if (!paid) {
        toast.error('Please complete the verification payment first.')
        return
      }

      const base64Image = await fileToBase64(selectedFile)

      const verificationData = {
        venueBooked: venueBooked === 'yes',
        verificationImage: base64Image
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

  const renderPaymentSection = () => {
    if (isPaid) {
      return (
        <div className="flex items-center justify-center space-x-2 text-green-600">
          <svg
            className="w-5 h-5"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={2}
              d="M5 13l4 4L19 7"
            />
          </svg>
          <span>Verification Payment Completed</span>
        </div>
      )
    }

    return (
      <div className="w-full flex justify-center">
        <StripeButton tournamentId={tournamentId} />
      </div>
    )
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
              <p>2. Upload booking confirmation image:</p>
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

            <div className="space-y-2">
              <p>3. Complete the verification payment:</p>
              {renderPaymentSection()}
            </div>
          </div>

          <div className="flex justify-between mt-6">
            <Button onClick={() => setIsDialogOpen(false)} variant="ghost">
              Cancel
            </Button>
            <Button 
              onClick={handleVerifyTournament}
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
