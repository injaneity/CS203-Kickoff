import { useEffect, useState } from 'react';
import { Bell } from 'lucide-react';
import { Link } from 'react-router-dom';
import { Button } from './ui/button';
import { AvatarImage } from './ui/avatar';
import { Toaster, toast } from 'react-hot-toast';
import { useSelector, useDispatch } from 'react-redux'; // Correct hook usage inside functional component
import { selectProfilePictureUrl, selectUserId } from '../store/userSlice';
import { useNavigate } from 'react-router-dom';
import { Club } from '../types/club';
import { selectUserClub, clearUser } from '../store/userSlice';
import { getClubApplication } from '../services/clubService';

export default function Header() {
  const [newApplications, setNewApplications] = useState(false);
  const userClub: Club | null = useSelector(selectUserClub); // Same here
  const userId = useSelector(selectUserId);
  const profilePictureUrl = useSelector(selectProfilePictureUrl);

  const navigate = useNavigate();
  const dispatch = useDispatch(); // Use useDispatch inside the component body
  const clubId = userClub?.id;


  let isCaptain = false;

  if (userClub) {
    isCaptain = userClub?.captainId === userId;
  }

  useEffect(() => {
    const checkForNewApplications = async () => {
      if (!clubId || !isCaptain) return;

      try {
        const response = await getClubApplication(clubId);
        
        if (response.status === 200) {
          const playerIds = response.data;
          const hasPending = playerIds.length > 0;  // If there are any pending applications, set the flag
          setNewApplications(hasPending);
        } else {
          setNewApplications(false);
        }
      } catch (error) {
        console.error('Error fetching applications:', error);
        setNewApplications(false);
      }
    };

    const intervalId = setInterval(checkForNewApplications, 2000);  // Poll every 5 seconds

    return () => clearInterval(intervalId);
  }, [clubId]);

  const handleBellClick = () => {
    if (clubId) {
      navigate(`/clubs/${clubId}/applications`);
    } else {
      console.error('No club selected');
    }
  };

  const handleLogoutClick = () => {
    // Clear the auth token from localStorage
    localStorage.removeItem('authToken');

    // Dispatch a logout action to clear persisted user data
    dispatch(clearUser()); // Use dispatch here safely

    // Optionally, show a toast to confirm the logout action
    toast('You have been logged out.');
    navigate('/profile');
  };

  return (
    <header className="flex justify-between items-center p-4 bg-gray-900">
      <Toaster /> {/* This is needed for toast notifications */}
      <div className="flex items-center">
        <Link to="/" className="text-2xl font-bold ml-2 text-white hover:text-gray-300 transition-colors">
          KICKOFF
        </Link>
      </div>
      {
        userId &&
        <div className="flex items-center space-x-4">
          {
            isCaptain &&
            <Button
              variant="ghost"
              className="relative"
              onClick={handleBellClick}
            >
              <Bell className="h-6 w-6 text-blue-500" />
              {newApplications && (
                <span className="absolute top-0 right-0 block h-3 w-3 rounded-full bg-red-500 ring-2 ring-white" />
              )}
            </Button>
          }

          {/* <Button variant="ghost" size="icon">
            <MessageSquare className="h-5 w-5" />
          </Button> */}
          <Button variant="ghost" onClick={handleLogoutClick}> {/* Logout Button */}
            Logout
          </Button>
          <AvatarImage src={profilePictureUrl || `https://picsum.photos/seed/${userId + 2000}/100/100`}>
          </AvatarImage>
        </div>
      }
    </header>
  );
}
