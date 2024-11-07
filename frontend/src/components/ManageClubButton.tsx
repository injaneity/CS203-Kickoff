import React, { useState } from 'react';
import { useDispatch } from 'react-redux';
import { updateClubPenaltyStatus } from '../services/clubService';
import { ClubPenaltyStatus, PenaltyType } from '../types/club';
import { Button } from './ui/button';
import { Dialog, DialogContent, DialogHeader, DialogTitle } from './ui/dialog';
import toast from 'react-hot-toast';
import { AppDispatch } from '../store';
import { fetchClubsAsync } from '../store/clubSlice';


interface ManageClubButtonProps {
    clubId: number;
    currentPenaltyStatus: ClubPenaltyStatus;
}

const ManageClubButton: React.FC<ManageClubButtonProps> = ({ clubId, currentPenaltyStatus }) => {
    const dispatch = useDispatch<AppDispatch>();
    const [isManageModalOpen, setIsManageModalOpen] = useState(false);
    const [penaltyType, setPenaltyType] = useState<PenaltyType>(currentPenaltyStatus.penaltyType);
    const [banUntil, setBanUntil] = useState<string>('');

    const openManageModal = () => setIsManageModalOpen(true);
    const closeModal = () => setIsManageModalOpen(false);

    const handleUpdatePenalty = async (e: React.MouseEvent) => {
        e.preventDefault();
        try {
            // await dispatch(
            //     updateClubPenaltyAsync({
            //         clubId,
            //         penaltyStatus,
            //         banUntil: banUntil + "Z",
            //     })
            // ).unwrap();
            console.log(clubId,
                penaltyType.toString(),
                banUntil + ":00",
            );
            await updateClubPenaltyStatus(
                clubId,
                penaltyType.toString(),
                banUntil + ":00",
            )
            toast.success('Club penalty status updated successfully');
            closeModal();
            dispatch(fetchClubsAsync());
        } catch {
            toast.error('Failed to update club penalty status');
        }
    };

    return (
        <>
            <Button className="w-full h-10 bg-blue-500 hover:bg-blue-600" onClick={openManageModal}>
                Manage Club
            </Button>

            {isManageModalOpen && (
                <Dialog open={isManageModalOpen} onOpenChange={setIsManageModalOpen}>
                    <DialogContent>
                        <DialogHeader>
                            <DialogTitle>Manage Club Penalty</DialogTitle>
                        </DialogHeader>
                        <div className="space-y-4">
                            <div>
                                <label className="block text-sm font-medium text-gray-300">Penalty Status</label>
                                <select
                                    className="mt-1 block w-full border-gray-700 rounded bg-gray-800 text-white"
                                    value={penaltyType}
                                    onChange={(e) => setPenaltyType(e.target.value as PenaltyType)}
                                >
                                    <option value={PenaltyType.NONE}>None</option>
                                    <option value={PenaltyType.BLACKLISTED}>Blacklist</option>
                                </select>
                            </div>
                            {penaltyType === PenaltyType.BLACKLISTED && (
                                <div>
                                    <label className="block text-sm font-medium text-gray-300">Ban Until</label>
                                    <input
                                        type="datetime-local"
                                        className="mt-1 block w-full border-gray-700 rounded bg-gray-800 text-white"
                                        value={banUntil}
                                        onChange={(e) => setBanUntil(e.target.value)}
                                    />
                                </div>
                            )}
                            <Button onClick={handleUpdatePenalty} className="w-full bg-red-600 hover:bg-red-700">
                                Update Penalty
                            </Button>
                        </div>
                    </DialogContent>
                </Dialog>
            )}
        </>
    );
};

export default ManageClubButton;