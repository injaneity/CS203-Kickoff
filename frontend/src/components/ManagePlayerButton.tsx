import React, { useState } from 'react';
import toast from 'react-hot-toast';
import { updatePlayerStatus } from '../services/userService';
import { PlayerProfile, PlayerStatus } from '../types/profile';
import { AppDispatch } from '../store';
import { useDispatch } from 'react-redux';
import { fetchAllPlayersAsync } from '../store/userSlice';
import { Button } from './ui/button';
import { Dialog, DialogContent, DialogHeader, DialogTitle } from './ui/dialog';

interface ManagePlayerButtonProps {
    playerProfile: PlayerProfile;
}

const ManagePlayerButton: React.FC<ManagePlayerButtonProps> = ({ playerProfile }) => {
    const dispatch = useDispatch<AppDispatch>();
    const [isManageModalOpen, setIsManageModalOpen] = useState(false);

    // Open the Manage Player modal
    const openManageModal = (e: React.MouseEvent) => {
        e.preventDefault();
        e.stopPropagation();
        setIsManageModalOpen(true);
    };

    const closeModal = () => {
        setIsManageModalOpen(false);
    };

    // Toggle Blacklist Status
    const handleToggleBlacklist = async (e: React.MouseEvent) => {
        e.preventDefault();
        const newStatus =
            playerProfile.status === PlayerStatus.STATUS_BLACKLISTED
                ? null
                : PlayerStatus.STATUS_BLACKLISTED;

        try {
            await updatePlayerStatus(playerProfile.id, newStatus);

            toast.success(
                newStatus === PlayerStatus.STATUS_BLACKLISTED
                    ? `${playerProfile.username} has been blacklisted.`
                    : `${playerProfile.username} has been unblacklisted.`
            );
            closeModal(); // Close modal after action
            dispatch(fetchAllPlayersAsync());
        } catch (error) {
            console.error('Failed to update player status:', error);
            toast.error('Failed to update player status. Please try again.');
        }
    };

    return (
        <>
            {/* Button to open modal */}
            <Button className="w-full h-10 bg-blue-500 hover:bg-blue-600" onClick={openManageModal}>
                Manage Player
            </Button>

            {/* Manage Player Modal */}
            {isManageModalOpen && (
                <Dialog open={isManageModalOpen} onOpenChange={setIsManageModalOpen}>
                    <DialogContent>
                        <DialogHeader>
                            <DialogTitle>Manage Player: {playerProfile.username}</DialogTitle>
                        </DialogHeader>
                        <div className="space-y-4">
                            {/* Blacklist/Unblacklist Player Button */}
                            <Button onClick={handleToggleBlacklist} className="w-full bg-red-600 hover:bg-red-700">
                                {playerProfile.status === PlayerStatus.STATUS_BLACKLISTED ? 'Unblacklist Player' : 'Blacklist Player'}
                            </Button>
                        </div>
                    </DialogContent>
                </Dialog>
            )}
        </>
    );
};

export default ManagePlayerButton;
