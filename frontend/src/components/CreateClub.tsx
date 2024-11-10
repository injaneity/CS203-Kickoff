import { useState } from 'react';
import { toast } from 'react-hot-toast';
import { Button } from './ui/button';
import { Input } from './ui/input';
import { Dialog, DialogContent, DialogHeader, DialogTitle } from "./ui/dialog"; 
import { Club } from '../types/club';  
import { fetchUserClubAsync } from '../store/userSlice';
import { useDispatch } from 'react-redux';
import { AppDispatch } from '../store';
import { createClub } from '../services/clubService';

interface CreateClubProps {
  isCreateDialogOpen: boolean;
  setIsCreateDialogOpen: (open: boolean) => void;
  handleClubCreated: (newClub: Club) => void;
}

const CreateClub: React.FC<CreateClubProps> = ({ isCreateDialogOpen, setIsCreateDialogOpen, handleClubCreated }) => {
  const [clubName, setClubName] = useState('');
  const [elo, setElo] = useState<number>(500);
  const [ratingDeviation, setRatingDeviation] = useState<number>(200);
  const [loading, setLoading] = useState(false);
  const dispatch = useDispatch<AppDispatch>();

  const handleCreateClub = async () => {
    if (!clubName) {
      toast.error('Club name is required!');
      return;
    }

    const clubData = {
      club: {
        name: clubName,
        elo,
        ratingDeviation,
      }
    };

    try {
      setLoading(true);

      const createClubResponse = await createClub(clubData);

      if (createClubResponse.status === 201) {
        const newClub = createClubResponse.data;  
        toast.success('Club created successfully!');
        const completeClub: Club = {
          ...newClub,
          players: [],
        };
        handleClubCreated(completeClub);     
        dispatch(fetchUserClubAsync());   

        // Reset the form and close the dialog
        setIsCreateDialogOpen(false);       
        setClubName('');
        setElo(500);
        setRatingDeviation(200);
        setIsCreateDialogOpen(false);
      }
    } catch (err: any) {
      console.error('Error creating club:', err);
      toast.error(err.response?.data?.message || 'An error occurred');
    } finally {
      setLoading(false);
    }
  };

  return (
    <>
      <Dialog open={isCreateDialogOpen} onOpenChange={setIsCreateDialogOpen}>
        <DialogContent className="sm:max-w-[300px] lg:max-w-[500px]">
          <DialogHeader>
            <DialogTitle>Create New Club</DialogTitle>
          </DialogHeader>

          {/* Information */}
          <div>
          All clubs start with 500 ELO. Win matches and earn more!
          </div>
          {/* Club creation form */}
          <div className="space-y-4">
            <div className="grid grid-cols-1 md:grid-cols-1 gap-4">
              <div>
                <label htmlFor="clubName" className="form-label">Club Name</label>
                <Input
                  id="clubName"
                  name="clubName"
                  value={clubName}
                  onChange={(e) => setClubName(e.target.value)}
                  className="form-input"
                  required
                />
              </div>
            </div>
            <div className="flex justify-end space-x-2 mt-6">
              <Button 
                type="button" 
                onClick={() => setIsCreateDialogOpen(false)} 
                className="bg-gray-600 hover:bg-gray-700"
              >
                Cancel
              </Button>
              <Button 
                type="button" 
                onClick={handleCreateClub} 
                className="bg-blue-600 hover:bg-blue-700"
                disabled={loading}
              >
                {loading ? 'Creating...' : 'Create Club'}
              </Button>
            </div>
          </div>
        </DialogContent>
      </Dialog>
    </>
  );
};

export default CreateClub;
