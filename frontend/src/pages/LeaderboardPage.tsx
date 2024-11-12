import { useEffect, useState } from 'react';
import { useSelector } from 'react-redux';
import { useNavigate } from 'react-router-dom';
import { fetchClubs } from '../services/clubService';
import { selectUserClub } from '../store/userSlice';
import { Club } from '../types/club';
import { Card, CardContent } from "../components/ui/card";
import { Progress } from "../components/ui/progress";
import toast from 'react-hot-toast';

export default function Leaderboard() {
    const [clubs, setClubs] = useState<Club[]>([]);
    const userClub = useSelector(selectUserClub);
    const navigate = useNavigate();

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

    const getBorderColor = (club: Club) => {
        if (club.elo >= 1800) return 'border-rainbow animate-rainbow';
        if (club.elo >= 1500) return 'border-yellow-500';
        if (club.elo >= 1200) return 'border-white shadow-xl';
        return 'border-gray-700';
    };

    const handleViewClub = (clubId: string) => {
        if (parseInt(clubId) === userClub?.id) {
            toast.success("That's your club!");
            return;
        }
        navigate(`/clubs/${clubId}`);
    };

    const ClubEloRewards = ({ userClubElo }: { userClubElo: number }) => {
        const maxElo = 3000;
        const progress = (userClubElo / maxElo) * 100;

        return (
            <div className="mt-10 bg-gray-800 p-6 rounded-lg shadow-lg">
                <h2 className="text-2xl font-bold mb-4 text-white">Club ELO Rewards</h2>
                <div className="relative pt-3">
                    <Progress value={progress} className="h-4 relative" />
                    {/* Dividers */}
                    <div className="absolute top-2 left-0 w-full h-4 flex justify-between pointer-events-none">
                        <div className="absolute left-[40%] h-6 w-0.5 bg-gray-300"></div> {/* 1200 ELO */}
                        <div className="absolute left-[50%] h-6 w-0.5 bg-gray-300"></div> {/* 1500 ELO */}
                        <div className="absolute left-[60%] h-6 w-0.5 bg-gray-300"></div> {/* 1800 ELO */}
                    </div>
                    <div className="flex justify-between text-xs text-white mt-2">
                        <span className="absolute left-0">0</span>
                        <span className="absolute left-[40%]">1200</span> {/* 1200 ELO */}
                        <span className="absolute left-[50%]">1500</span> {/* 1500 ELO */}
                        <span className="absolute left-[60%]">1800</span> {/* 1800 ELO */}
                        <span className="absolute right-0">3000</span>
                    </div>
                </div>
                <div className="mt-7 space-y-2">
                    <div className="flex items-center">
                        <div className="w-4 h-4 bg-white shadow-xl mr-2"></div>
                        <span className="text-white"><b>1200 ELO:</b> Silver Border</span>
                    </div>
                    <div className="flex items-center">
                        <div className="w-4 h-4 bg-yellow-400 mr-2"></div>
                        <span className="text-white"><b>1500 ELO:</b> Gold Border</span>
                    </div>
                    <div className="flex items-center">
                        <div className="w-4 h-4 bg-gradient-to-r from-red-500 via-yellow-500 to-blue-500 mr-2 animate-rainbow"></div>
                        <span className="text-white"><b>1800 ELO:</b> Rainbow Animated Border</span>
                    </div>
                </div>
                <div className="mt-4">
                    <span className="text-white font-bold">Your Club's Progress: {userClubElo.toFixed(0)} ELO</span>
                </div>
            </div>
        );
    };

    return (
        <div className="container mx-auto px-4 py-8">
            <h1 className="text-3xl font-bold mb-6 text-center text-white">Club Leaderboard</h1>
            <div className="space-y-4">
                {clubs.map((club, index) => (
                    <Card
                        key={club.id}
                        onClick={() => handleViewClub(club.id.toString())}
                        className={`border-2 p-1 rounded-lg ${getBorderColor(club)} ${club.id === userClub?.id ? 'bg-indigo-600 bg-opacity-90' : 'bg-gray-800'
                            } transition-all hover:bg-gray-700 cursor-pointer shadow-md relative`}
                    >
                        <CardContent className={`flex items-center p-4 ${club.id === userClub?.id ? 'bg-indigo-600' : 'bg-gray-800'} rounded-lg`}>
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
                        </CardContent>
                    </Card>
                ))}
            </div>
            {userClub && <ClubEloRewards userClubElo={userClub.elo} />}
        </div>
    );
}
