import React, { useState, useEffect } from 'react';
import { Button } from "./ui/button";
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger } from "./ui/dialog";
import { ScrollArea } from "./ui/scroll-area";
import { Tournament } from '../types/tournament';
import { ClubProfile } from '../types/club';
import { fetchTournaments } from '../services/tournamentService';
import { getClubProfileById } from '../services/clubService';
import MatchCard from './MatchCard';
import { Match } from '../types/bracket';

const LiveUpdatesButton: React.FC = () => {
  const [isOpen, setIsOpen] = useState(false);
  const [tournaments, setTournaments] = useState<Tournament[]>([]);
  const [clubProfiles, setClubProfiles] = useState<{ [key: number]: ClubProfile }>({});
  const [matches, setMatches] = useState<Match[]>([]);

  useEffect(() => {
    if (isOpen) {
      const fetchAllTournaments = async () => {
        try {
          const fetchedTournaments = await fetchTournaments();
          console.log('Fetched tournaments:', fetchedTournaments);
          if (fetchTournaments != null && fetchTournaments.length != 0) {            
            setTournaments(fetchedTournaments);
          }
          
          

          const clubIds = new Set<number>();
          fetchedTournaments.forEach(tournament => 
            tournament.bracket?.rounds.forEach(round => 
              round.matches.forEach(match => {
                if (match.club1Id) clubIds.add(match.club1Id);
                if (match.club2Id) clubIds.add(match.club2Id);
              })
            )
          );

          const clubProfilesMap: { [key: number]: ClubProfile } = {};
          await Promise.all(Array.from(clubIds).map(async clubId => {
            try {
              const profile = await getClubProfileById(clubId);
              console.log(`Fetched club profile for clubId ${clubId}:`, profile);
              clubProfilesMap[clubId] = profile;
            } catch (error) {
              console.error(`Error fetching club profile for clubId ${clubId}:`, error);
            }
          }));

          setClubProfiles(clubProfilesMap);

          const matches = fetchedTournaments.flatMap(tournament =>
            tournament.bracket?.rounds.flatMap(round =>
              round.matches.filter(match => match.over)
            ) || []
          );
          setMatches(matches);
        } catch (error) {
          console.error('Error fetching tournaments:', error);
        }
      };

      fetchAllTournaments();
    }
  }, [isOpen]);

  return (
    <Dialog open={isOpen} onOpenChange={setIsOpen}>
      <DialogTrigger asChild>
        {/* Updated Button Styling */}
        <Button variant="ghost" onClick={() => setIsOpen(true)} className="text-white hover:text-gray-300">
          Recent Matches
        </Button>
      </DialogTrigger>
      <DialogContent className="sm:max-w-[425px] relative">
        <DialogHeader>
          <DialogTitle>Latest Match Results</DialogTitle>
          {/* Close button */}
          <Button 
            variant="ghost"
            className="absolute top-2 right-2 bg-blue-600 text-white-500 hover:text-blue-400"
            onClick={() => setIsOpen(false)}
          >
            Close
          </Button>
        </DialogHeader>
        <ScrollArea className="h-[300px] w-full rounded-md border p-4">
          {matches.length > 0 ? (
            matches
              .sort((a, b) => b.matchNumber - a.matchNumber)
              .map(match => {

                const tournament = tournaments.find(t => 
                  t.bracket?.rounds.some(r => r.matches.some(m => m.id === match.id))
                );
                const club1 = clubProfiles[match.club1Id!];
                const club2 = clubProfiles[match.club2Id!];

                if (!club1 || !club2 || !tournament || tournament.id === undefined) return null; // Ensure tournament and club data exist

                return (
                  <div key={match.id}>
                    <h3 className="text-sm font-semibold mb-2">{tournament.name || 'Unknown Tournament'}</h3>
                    <MatchCard match={match} club1={club1} club2={club2} tournamentId={tournament.id} />
                  </div>
                );
              })
          ) : (
            <div className="flex justify-center items-center h-full text-center text-gray-400">
              No matches yet, come back later!
            </div>
          )}
        </ScrollArea>
      </DialogContent>
    </Dialog>
  );
};

export default LiveUpdatesButton;
