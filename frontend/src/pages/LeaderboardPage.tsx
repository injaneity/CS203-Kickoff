import React, { useEffect, useState } from 'react';
import { useSelector } from 'react-redux';
import { fetchClubs } from '../services/clubService';
import { fetchUserClubAsync, selectUserClub } from '../store/userSlice'; // Make sure you import fetchUserClubAsync if you need to dispatch it
import { Club } from '../types/club';
import { Card, CardContent } from "../components/ui/card";
import ClubCard from '../components/ClubCard';
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from '../components/ui/dialog';

export default function Leaderboard() {
    const [clubs, setClubs] = useState<Club[]>([]);
    const [selectedClub, setSelectedClub] = useState<Club | null>(null);
    const [isDialogOpen, setIsDialogOpen] = useState(false);
    const userClub = useSelector(selectUserClub);

    useEffect(() => {
        const loadClubs = async () => {
            try {
                const fetchedClubs = await fetchClubs();
                const sortedClubs = fetchedClubs.sort((a, b) => b.elo - a.elo);
                setClubs(sortedClubs);
            } catch (error) {
                console.error('Error fetching clubs:', error);
            }
        };

        loadClubs();
    }, []);

    const getBorderColor = (index: number) => {
        switch (index) {
            case 0: return 'border-yellow-400'; // Gold
            case 1: return 'border-gray-100';   // Silver
            case 2: return 'border-amber-700';  // Bronze
            default: return 'border-gray-700';  
        }
    };

    const handleViewClub = (club: Club) => {
        setSelectedClub(club);
        setIsDialogOpen(true);
    };

    return (
        <div className="container mx-auto px-4 py-8">
            <h1 className="text-3xl font-bold mb-6 text-center text-white">Club Leaderboard</h1>
            <div className="space-y-4">
                {clubs.map((club, index) => (
                    <Card
                        key={club.id}
                        className={`border-2 ${getBorderColor(index)} ${
                            club.id === userClub?.id ? 'bg-gray-500' : 'bg-gray-800'
                        } transition-all hover:bg-gray-700 cursor-pointer shadow-md`}
                    >
                        <CardContent className="flex items-center p-4">
                            <div className="flex-shrink-0 w-12 h-12 flex items-center justify-center font-bold text-2xl mr-4 text-white">
                                {index + 1}
                            </div>
                            <div className="flex-shrink-0 w-16 h-16 mr-4">
                                <img
                                    src={`https://picsum.photos/seed/club-${club.id}/800/800`}
                                    alt={`${club.name} profile`}
                                    className="w-full h-full object-cover rounded-full"
                                />
                            </div>
                            <div className="flex-grow">
                                <div className="flex items-center">
                                    <h3 className="text-xl font-bold text-white mr-2">{club.name}</h3>
                                    {index < 3 && (
                                        <span className="text-3xl">
                                            {index === 0 && '🥇'}
                                            {index === 1 && '🥈'}
                                            {index === 2 && '🥉'}
                                        </span>
                                    )}
                                </div>
                                <p className="text-sm text-gray-400">ELO: {club.elo.toFixed(0)}</p>
                            </div>
                            <button
                                onClick={(e) => {
                                    e.stopPropagation(); 
                                    handleViewClub(club);
                                }}
                                className="px-3 py-1 bg-blue-600 text-white rounded-md hover:bg-blue-700"
                            >
                                View Club
                            </button>
                        </CardContent>
                    </Card>
                ))}
            </div>

            {/* Club Info Dialog */}
            {selectedClub && (
                <Dialog open={isDialogOpen} onOpenChange={setIsDialogOpen}>
                    <DialogContent className="sm:max-w-[600px]">
                        <DialogHeader>
                            <DialogTitle>{selectedClub.name}</DialogTitle>
                        </DialogHeader>
                        <ClubCard
                            club={selectedClub}
                            image={`https://picsum.photos/seed/club-${selectedClub.id}/800/200`}
                            onClick={() => setIsDialogOpen(false)} // Close the dialog when clicking on the card
                        />
                        <button
                            onClick={() => setIsDialogOpen(false)}
                            className="mt-4 px-4 py-2 bg-red-600 text-white rounded-md hover:bg-red-700"
                        >
                            Close
                        </button>
                    </DialogContent>
                </Dialog>
            )}
        </div>
    );
}
