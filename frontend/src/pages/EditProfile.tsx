import { useState, useEffect, useRef } from 'react';
import { Input } from '../components/ui/input';
import { Button } from '../components/ui/button';
import { PlayerPosition, PlayerProfile, UserPublicDetails } from '../types/profile';
import { fetchPlayerProfileById, updatePlayerProfile, fetchUserPublicInfoById, uploadProfilePicture, updateProfilePicture } from '../services/userService';
import { useDispatch, useSelector } from 'react-redux';
import { setUser, selectUserId, selectUsername, selectIsAdmin } from '../store/userSlice';
import { toast } from 'react-hot-toast';
import { ArrowLeft, Upload } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { AppDispatch } from '../store';
import { fileToBase64 } from '../services/image';

export default function PlayerProfilePage() {
  const userId = useSelector(selectUserId);
  const username = useSelector(selectUsername);
  const isAdmin = useSelector(selectIsAdmin);
  const navigate = useNavigate();
  const dispatch = useDispatch<AppDispatch>();

  const [playerProfile, setPlayerProfile] = useState<PlayerProfile | null>(null);
  const [userDetails, setUserDetails] = useState<UserPublicDetails | null>(null);
  const [preferredPositions, setPreferredPositions] = useState<PlayerPosition[]>([]);
  const [profileDescription, setProfileDescription] = useState('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [profilePictureUrl, setProfilePictureUrl] = useState<string | null>(null);
  const fileInputRef = useRef<HTMLInputElement>(null);

  // Fetch player profile when logged in
  useEffect(() => {
    if (!userId) {
      setError('User not logged in');
      setLoading(false);
      return;
    }

    const fetchPlayerProfile = async () => {
      try {
        const viewedUser = await fetchUserPublicInfoById(userId);
        setUserDetails(viewedUser);
        setProfilePictureUrl(viewedUser.profilePictureUrl || null)
      } catch (err) {
        console.error('Error fetching user:', err);
        setLoading(false);
      }

      try {
        const response = await fetchPlayerProfileById(userId);
        setPlayerProfile(response);
        setPreferredPositions(response.preferredPositions || []);
        setProfileDescription(response.profileDescription || '');
        setLoading(false);
      } catch (err) {
        console.error('Error fetching player profile:', err);
        setLoading(false);
      }
    };

    fetchPlayerProfile();
  }, [userId]);

  const handlePreferredPositionsChange = (position: PlayerPosition) => {
    setPreferredPositions((prevPositions) =>
      prevPositions.includes(position)
        ? prevPositions.filter((pos) => pos !== position)
        : [...prevPositions, position]
    );
  };

  const handleSubmit = async () => {
    if (!userDetails) return;

    if (!playerProfile) {
      if (profilePictureUrl) {
        await updateProfilePicture(userDetails.id, profilePictureUrl);
        dispatch(setUser({ userId: userId, username: username, isAdmin: isAdmin, profilePictureUrl: profilePictureUrl }));
        toast.success('Profile updated successfully', {
          duration: 3000,
          position: 'top-center',
        });
        navigate("/profile");
      }
      return
    }

    try {
      await updatePlayerProfile(playerProfile.id, preferredPositions, profileDescription);
      if (profilePictureUrl) {
        await updateProfilePicture(userDetails.id, profilePictureUrl);
        dispatch(setUser({ userId: userId, username: username, isAdmin: isAdmin, profilePictureUrl: profilePictureUrl }));
      }
      toast.success('Profile updated successfully', {
        duration: 3000,
        position: 'top-center',
      });
      navigate("/profile");
    } catch (err) {
      console.error('Error updating profile:', err);
      toast.error('Failed to update profile', {
        duration: 4000,
        position: 'top-center',
      });
    }
  };

  const handleImageUpload = async (event: React.ChangeEvent<HTMLInputElement>) => {
    if (!event.target.files || !event.target.files[0]) {
      toast.error('Please upload a profile picture.')
      return
    }

    try {
      const base64Image = await fileToBase64(event.target.files[0]);

      const response = await uploadProfilePicture(userId, base64Image)
      setProfilePictureUrl(response)
      toast.success('Profile image updated successfully', {
        duration: 3000,
        position: 'top-center',
      })
    } catch (err) {
      console.error('Error uploading profile image:', err)
      toast.error('Failed to upload profile image', {
        duration: 4000,
        position: 'top-center',
      })
    }
  }


  const formatPosition = (position: string) => {
    return position.replace('POSITION_', '').charAt(0) + position.replace('POSITION_', '').slice(1).toLowerCase();
  };

  // Render profile page if user is logged in
  if (loading) return <div>Loading...</div>;

  if (error || !userDetails) return <div>Error: {error || 'User not found'}</div>;

  return (
    <div className="container mx-auto p-6">
      <div className='flex items-center mb-6'>
        <Button variant="ghost" onClick={() => navigate(-1)} className="mr-2">
          <ArrowLeft className="h-4 w-4 mr-2" />
          Back
        </Button>
      </div>
      <div className="bg-gray-900 rounded-lg p-6">
        <div className="flex items-center mb-6">
          <img
            src={profilePictureUrl || `https://picsum.photos/seed/${userDetails.id + 2000}/200/200`}
            alt={`${userDetails.username}'s profile`}
            className="w-24 h-24 rounded-full object-cover mr-6"
          />
          <div className=" mr-6">
            <h1 className="text-3xl font-bold">{userDetails ? userDetails.username : null}</h1>
            <p className="text-gray-400">User ID: {userDetails ? userDetails.id : null}</p>
          </div>
          <div>
            <Button
              variant="secondary"
              size="icon"
              className=" rounded-full bg-gray-800 hover:bg-gray-700"
              onClick={() => fileInputRef.current?.click()}
            >
              <Upload className="h-4 w-4" />
            </Button>
            <input
              type="file"
              ref={fileInputRef}
              onChange={handleImageUpload}
              accept="image/*"
              className="hidden"
            />
          </div>
        </div>

        {playerProfile ?
          <>
            <div className="mb-6">
              <h2 className="text-xl font-semibold mb-2">Player Profile Description</h2>
              <Input
                value={profileDescription}
                onChange={(e) => setProfileDescription(e.target.value)}
                placeholder="Describe yourself"
                className="w-full bg-gray-800 border-gray-700"
              />
            </div>
            <div className="mb-6">
              <h2 className="text-xl font-semibold mb-2">Preferred Positions</h2>
              <div className="flex flex-wrap">
                {Object.values(PlayerPosition).map((position) => (
                  <label key={position} className="mr-4 mb-2 flex items-center">
                    <input
                      type="checkbox"
                      checked={preferredPositions.includes(position)}
                      onChange={() => handlePreferredPositionsChange(position)}
                      className="form-checkbox h-4 w-4 text-blue-600"
                    />
                    <span className="ml-2">{formatPosition(position)}</span>
                  </label>
                ))}
              </div>
            </div>
          </> :
          <></>
        }

        <Button onClick={handleSubmit} className="bg-blue-600 hover:bg-blue-700">
          Update Profile
        </Button>
      </div>
    </div>
  );
}
