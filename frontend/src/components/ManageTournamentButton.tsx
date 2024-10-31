import React, { useState } from 'react'
import { Button } from "../components/ui/button"
import { Dialog, DialogContent, DialogHeader, DialogTitle } from "../components/ui/dialog"
import { Card, CardContent } from "../components/ui/card"
import { toast } from 'react-hot-toast'
import { approveVerification, rejectVerification } from '../services/tournamentService'
import { Tournament } from '../types/tournament'

interface ManageTournamentButtonProps {
    tournament: Tournament;
    onActionComplete: () => void;
}

export default function ManageTournamentButton({ tournament, onActionComplete }: ManageTournamentButtonProps) {
    const [isDialogOpen, setIsDialogOpen] = useState(false)

    const handleApprove = async () => {
        try {
            await approveVerification(tournament.id!)
            toast.success('Tournament verification approved successfully!')
            onActionComplete()
            setIsDialogOpen(false)
        } catch (error) {
            console.error('Error approving verification:', error)
            toast.error('Failed to approve verification')
        }
    }

    const handleReject = async () => {
        try {
            await rejectVerification(tournament.id!)
            toast.success('Tournament verification rejected successfully!')
            onActionComplete()
            setIsDialogOpen(false)
        } catch (error) {
            console.error('Error rejecting verification:', error)
            toast.error('Failed to reject verification')
        }
    }

    return (
        <>
            <Button
                onClick={() => setIsDialogOpen(true)}
                className="bg-blue-500 hover:bg-blue-600 w-40 h-10" // Increased width from w-32 to w-40
            >
                Manage Tournament
            </Button>

            <Dialog open={isDialogOpen} onOpenChange={setIsDialogOpen}>
                <DialogContent className="sm:max-w-[600px]">
                    <DialogHeader>
                        <DialogTitle>Manage Tournament: {tournament.name}</DialogTitle>
                    </DialogHeader>
                    <div className="mt-4 space-y-4">
                        <Card>
                            <CardContent className="pt-6">
                                <div className="space-y-4">
                                    <div>
                                        <h3 className="font-semibold mb-2">Verification Status</h3>
                                        <p className="text-sm">{tournament.verificationStatus || 'Not submitted'}</p>
                                    </div>
                                    <div>
                                        <h3 className="font-semibold mb-2">Venue Booked</h3>
                                        <p className="text-sm">{tournament.venueBooked ? 'Yes' : 'No'}</p>
                                    </div>
                                    {tournament.verificationImageUrl && (
                                        <div>
                                            <h3 className="font-semibold mb-2">Verification Image</h3>
                                            <a 
                                                href={tournament.verificationImageUrl} 
                                                target="_blank" 
                                                rel="noopener noreferrer"
                                                className="text-blue-500 hover:text-blue-600 underline text-sm"
                                            >
                                                View Image
                                            </a>
                                            <img 
                                                src={tournament.verificationImageUrl} 
                                                alt="Verification" 
                                                className="mt-2 w-full max-h-[300px] object-cover rounded-md"
                                            />
                                        </div>
                                    )}
                                </div>
                            </CardContent>
                        </Card>
                    </div>
                    <div className="flex justify-end space-x-2 mt-4">
                        <Button
                            variant="ghost"
                            onClick={() => setIsDialogOpen(false)}
                        >
                            Close
                        </Button>
                        {tournament.verificationStatus === 'PENDING' && (
                            <>
                                <Button
                                    variant="ghost"
                                    onClick={handleReject}
                                    className="hover:bg-red-700"
                                >
                                    Reject
                                </Button>
                                <Button
                                    variant="ghost"
                                    onClick={handleApprove}
                                    className="bg-green-600 hover:bg-green-700"
                                >
                                    Approve
                                </Button>
                            </>
                        )}
                    </div>
                </DialogContent>
            </Dialog>
        </>
    )
}