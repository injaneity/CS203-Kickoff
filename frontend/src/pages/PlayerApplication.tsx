import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useDispatch, useSelector } from 'react-redux';
import { Dispatch } from 'redux';
import { Button } from '../components/ui/button';
import { toast } from 'react-hot-toast';
import { fetchUserClubAsync, selectUserId, selectUserClub } from '../store/userSlice';
import PlayerProfileCard from '../components/PlayerProfileCard';
import { Club } from '../types/club';
import { getClubApplication, updatePlayerApplication } from '../services/clubService';
import { UserX, Loader2 } from 'lucide-react';
import { Card, CardContent, CardFooter } from '../components/ui/card';

interface Application {
  playerId: number;
  status: 'PENDING' | 'ACCEPTED' | 'REJECTED';
}

export default function ApplicationsPage() {
  const [applications, setApplications] = useState<Application[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isUpdating, setIsUpdating] = useState<number | null>(null);
  const navigate = useNavigate();
  const dispatch: Dispatch = useDispatch();

  const userId = useSelector(selectUserId);
  const userClub: Club | null = useSelector(selectUserClub);
  const clubId = userClub?.id;
  

  // Fetch user club on component mount
  useEffect(() => {
    const fetchData = async () => {
      if (!userId) {
        // toast.error('User not logged in');
        navigate('/profile');
        return;
      }

      try {
        await dispatch(fetchUserClubAsync() as any);
        setIsLoading(false);
      } catch (error) {
        console.error('Error fetching user club:', error);
        toast.error('Failed to fetch user club');
        setIsLoading(false);
      }
    };

    fetchData();
  }, [userId, dispatch, navigate]);

  // Fetch applications when clubId is available
  useEffect(() => {
    const fetchApplications = async () => {
      if (!clubId) return;
      try {
        const response = await getClubApplication(clubId);

        if (response.status === 200) {
          const playerIds: number[] = response.data;
          const newApplications = playerIds.map(playerId => ({
            playerId,
            status: 'PENDING' as const
          }));

          setApplications(newApplications);
        } else {
          throw new Error('Failed to fetch applications');
        }
      } catch (error) {
        console.error('Error fetching applications:', error);
        toast.error('Failed to fetch applications');
      }
    };

    if (clubId) {
      fetchApplications();
    }
  }, [clubId]);

  // Handle accept/reject application status
  const handleApplicationUpdate = async (playerId: number, status: 'ACCEPTED' | 'REJECTED') => {
    if (!clubId) return;

    try {
      setIsUpdating(playerId);
      const updateResponse = await updatePlayerApplication(clubId, playerId, status);

      if (updateResponse.status === 200) {
        toast.success(`Application ${status.toLowerCase()} successfully!`);

        // Update the applications state to reflect the new status
        setApplications(prevApplications =>
          prevApplications.map(app =>
            app.playerId === playerId ? { ...app, status } : app
          )
        );
      } else {
        throw new Error('Failed to update application');
      }
    } catch (error) {
      console.error(`Error ${status.toLowerCase()}ing application:`, error);
      toast.error(`Failed to ${status.toLowerCase()} application`);
    } finally {
      setIsUpdating(null);
    }
  };

  if (isLoading) {
    return <p>Loading...</p>;
  }

  if (!userId) {
    return <p>Please log in to view applications.</p>;
  }

  if (!clubId) {
    return <p>You are not a member of any club.</p>;
  }

  return (
    <div className="container mx-auto p-4">
      <h1 className="text-3xl font-bold mb-6">Player Applications</h1>
      {applications.length === 0 ? (
        <Card className="">
          <CardContent>
            <div className="flex flex-col items-center justify-center text-center">
              <UserX className="w-12 h-12 text-muted-foreground mb-4" />
              <p>There are currently no pending applications for your club.</p>
            </div>
          </CardContent>
        </Card>
      ) : (
        <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-3">
          {applications.map((application) => (
            <Card key={application.playerId} className="flex flex-col">
              <CardContent className="flex-grow p-4">
                <PlayerProfileCard 
                  id={application.playerId} 
                  availability={application.status === 'PENDING'}
                  needAvailability={false}
                />
                <p className="mt-4 text-sm font-medium">Status: 
                  <span className={`ml-2 px-2 py-1 rounded-full text-xs ${
                    application.status === 'PENDING' ? 'bg-yellow-100 text-yellow-800' :
                    application.status === 'ACCEPTED' ? 'bg-green-100 text-green-800' :
                    'bg-red-100 text-red-800'
                  }`}>
                    {application.status}
                  </span>
                </p>
              </CardContent>
              {application.status === 'PENDING' && (
                <CardFooter className="flex justify-between p-4 pt-0">
                  <Button 
                    onClick={() => handleApplicationUpdate(application.playerId, 'ACCEPTED')}
                    disabled={isUpdating === application.playerId}
                  >
                    {isUpdating === application.playerId ? (
                      <Loader2 className="w-4 h-4 mr-2 animate-spin" />
                    ) : null}
                    Accept
                  </Button>
                  <Button 
                    onClick={() => handleApplicationUpdate(application.playerId, 'REJECTED')}
                    variant="destructive"
                    disabled={isUpdating === application.playerId}
                  >
                    {isUpdating === application.playerId ? (
                      <Loader2 className="w-4 h-4 mr-2 animate-spin" />
                    ) : null}
                    Reject
                  </Button>
                </CardFooter>
              )}
            </Card>
          ))}
        </div>
      )}
    </div>
  );
}
