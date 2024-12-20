'use client';

import { useState, useEffect } from 'react';
import { useDispatch } from 'react-redux';
import { fetchTournamentsAsync } from '../store/tournamentSlice';
import { Tournament } from '../types/tournament';
import TournamentCard from '../components/TournamentCard';
import { Input } from "../components/ui/input";
import { Search } from 'lucide-react';
import { Button } from "../components/ui/button";
import { AppDispatch } from '../store';
import { toast } from 'react-hot-toast';
import { fetchPendingVerifications, fetchApprovedVerifications, fetchRejectedVerifications } from '../services/tournamentService';

enum TournamentFilter {
  ALL = 'All Tournaments',
  PENDING = 'Pending',
  VERIFIED = 'Verified',
  REJECTED = 'Rejected',
}

const AdminTournamentPage = () => {
  const dispatch = useDispatch<AppDispatch>();
  const [filteredTournaments, setFilteredTournaments] = useState<Tournament[]>([]);
  const [searchTerm, setSearchTerm] = useState('');
  const [tournamentFilter, setTournamentFilter] = useState<TournamentFilter>(TournamentFilter.ALL);

  // Initial fetch
  useEffect(() => {
    dispatch(fetchTournamentsAsync());
  }, []); // Only fetch on mount

  // Handle filtering when filter, search, or tournaments change
  useEffect(() => {
    loadFilteredTournaments();
  }, [tournamentFilter, searchTerm]); // Remove tournaments from dependency array

  const loadFilteredTournaments = async () => {
    try {
      let filtered: Tournament[] = [];
      
      switch (tournamentFilter) {
        case TournamentFilter.PENDING:
          filtered = await fetchPendingVerifications();
          break;
        case TournamentFilter.VERIFIED:
          filtered = await fetchApprovedVerifications();
          break;
        case TournamentFilter.REJECTED:
          filtered = await fetchRejectedVerifications();
          break;
        default:
          // For ALL tournaments, fetch fresh data
          const response = await dispatch(fetchTournamentsAsync()).unwrap();
          filtered = response;
      }

      console.log(filtered);
      
      if (filtered == null || filtered.length === 0) {
        filtered = [];
      }

      // Apply search filter if needed
      if (searchTerm) {
        filtered = filtered.filter(tournament =>
          tournament?.name?.toLowerCase().includes(searchTerm.toLowerCase())
        );
      }

      // Normalize the data structure to ensure consistency
      filtered = filtered.map(tournament => ({
        ...tournament,
        // Handle both possible property names
        joinedClubsIds: tournament.joinedClubIds || tournament.joinedClubIds || []
      }));

      setFilteredTournaments(filtered);
    } catch (error) {
      console.error(`Error loading ${tournamentFilter} tournaments:`, error);
      toast.error(`Failed to load ${tournamentFilter.toLowerCase()} tournaments.`);
    }
  };

  return (
    <div>
      <h2 className="text-2xl font-bold mb-4">Manage Tournaments</h2>
      <div className="relative w-full mb-4">
        <Search className="absolute left-2 top-2.5 h-4 w-4 text-gray-500" />
        <Input
          type="search"
          placeholder="Search Tournaments"
          className="pl-8 bg-gray-800 border-gray-700 w-full"
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
        />
      </div>

      {/* Filter Buttons */}
      <div className="flex justify-center space-x-4 mb-4">
        {Object.values(TournamentFilter).map((filter) => (
          <Button
            key={filter}
            onClick={() => setTournamentFilter(filter)}
            variant={tournamentFilter === filter ? "default" : "secondary"}
          >
            {filter}
          </Button>
        ))}
      </div>

      {/* Tournament Cards */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
        {filteredTournaments && filteredTournaments.length > 0 ? (
          filteredTournaments.map((tournament: Tournament) => (
            tournament && tournament.id && (
              <TournamentCard
                key={tournament.id}
                tournament={tournament}
                onActionComplete={loadFilteredTournaments}
              />
            )
          ))
        ) : (
          <p className="col-span-3 text-center text-gray-500">No tournaments available</p>
        )}
      </div>
    </div>
  );
};

export default AdminTournamentPage;
