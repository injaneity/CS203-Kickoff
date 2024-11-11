import React, { useState, useEffect } from 'react';
import { Button } from '../components/ui/button';
import { ClubProfile } from '../types/club';
import { getClubProfileById, updateClubDescription } from '../services/clubService';
import { useSelector } from 'react-redux';
import { selectUserId } from '../store/userSlice';
import { useNavigate, useParams } from 'react-router-dom';
import { toast } from 'react-hot-toast';
import { ArrowLeft } from 'lucide-react';

const EditClub: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const clubId = id ? parseInt(id) : null;
  const userId = useSelector(selectUserId);
  const navigate = useNavigate();

  const [club, setClub] = useState<ClubProfile | null>(null);
  const [clubName, setClubName] = useState('');
  const [clubDescription, setClubDescription] = useState('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false); // For loading state on submit

  useEffect(() => {
    if (!clubId) {
      setError('Invalid club ID.');
      setLoading(false);
      return;
    }

    const fetchClub = async () => {
      try {
        const clubData = await getClubProfileById(clubId);
        if (clubData.captainId !== userId) {
          setError('You are not authorized to edit this club.');
          return;
        }
        setClub(clubData);
        setClubName(clubData.name || '');
        setClubDescription(clubData.clubDescription || '');
      } catch (err: any) {
        console.error('Error fetching club data:', err);
        setError('Failed to load club data.');
      } finally {
        setLoading(false);
      }
    };

    fetchClub();
  }, [clubId, userId]);

  const handleSubmit = async () => {
    if (!clubId) {
      toast.error('Invalid club ID.');
      return;
    }

    // Basic validation
    if (!clubName.trim()) {
      toast.error('Club name cannot be empty.');
      return;
    }

    if (!clubDescription.trim()) {
      toast.error('Club description cannot be empty.');
      return;
    }

    setIsSubmitting(true);

    try {
      console.log(clubDescription);

      await updateClubDescription(clubId, clubDescription);
      toast.success('Club details updated successfully!', {
        duration: 3000,
        position: 'top-center',
      });
      navigate(`/clubs`);
    } catch (err: any) {
      console.error('Error updating club profile:', err);
      toast.error('Failed to update club details.', {
        duration: 4000,
        position: 'top-center',
      });
    } finally {
      setIsSubmitting(false);
    }
  };

  if (loading)
    return (
      <div className="flex justify-center items-center h-screen">Loading...</div>
    );

  if (error || !club) {
    return (
      <div className="flex justify-center items-center h-screen">
        {error || 'Club not found.'}
      </div>
    );
  }

  return (
    <div className="container mx-auto p-6">
      <div className='flex items-center mb-6'>
        <Button variant="ghost" onClick={() => navigate(-1)} className="mr-2">
          <ArrowLeft className="h-4 w-4 mr-2" />
          Back
        </Button>
      </div>
      <div className="bg-gray-900 rounded-lg p-6">
        <h1 className="text-2xl font-bold mb-6">Edit Club Details</h1>

        {/* Club Description */}
        <div className="mb-6">
          <label htmlFor="clubDescription" className="block text-gray-300 mb-2">
            Club Description
          </label>
          <textarea
            id="clubDescription"
            value={clubDescription}
            onChange={(e) => setClubDescription(e.target.value)}
            placeholder="Enter your club description"
            className="w-full bg-gray-800 border-gray-700 text-gray-200 p-2 h-20 resize-none rounded-md"
          ></textarea>
        </div>

        <Button
          onClick={handleSubmit}
          className="bg-blue-600 hover:bg-blue-700"
          disabled={isSubmitting}
        >
          {isSubmitting ? 'Saving...' : 'Save Changes'}
        </Button>
      </div>
    </div>
  );
};

export default EditClub;
