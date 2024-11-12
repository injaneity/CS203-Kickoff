import { createSlice, createAsyncThunk, PayloadAction } from '@reduxjs/toolkit';
import { fetchClubs, applyToClub, getClubApplication } from '../services/clubService';
import { Club } from '../types/club';

export const fetchClubsAsync = createAsyncThunk(
  'clubs/fetchClubs',
  async () => {
    return await fetchClubs();
  }
);

export const applyToClubAsync = createAsyncThunk(
  'clubs/applyToClub',
  async ({ clubId, playerProfileId, desiredPosition }: { clubId: number, playerProfileId: number, desiredPosition: string }) => {
    return await applyToClub(clubId, playerProfileId, desiredPosition);
  }
);

export const getClubApplicationAsync = createAsyncThunk(
  'clubs/getClubApplication',
  async (clubId: number) => {
    return await getClubApplication(clubId);
  }
);

const initialState = {
  clubs: [] as Club[],          
  selectedClubId: null as number | null, 
  clubApplication: null as any,
  status: 'idle',
  error: null as string | null,
};

const clubSlice = createSlice({
  name: 'clubs',
  initialState,
  reducers: {
    
    setSelectedClubId: (state, action) => {
      state.selectedClubId = action.payload;
    },
    
    clearSelectedClubId: (state) => {
      state.selectedClubId = null;
    },
    
    setClubApplication: (state, action: PayloadAction<any>) => {
      state.clubApplication = action.payload; // Store the application result
    },
  },
  extraReducers: (builder) => {
    builder
      .addCase(fetchClubsAsync.pending, (state) => {
        state.status = 'loading';
      })
      .addCase(fetchClubsAsync.fulfilled, (state, action) => {
        state.status = 'succeeded';
        state.clubs = action.payload;
      })
      .addCase(fetchClubsAsync.rejected, (state, action) => {
        state.status = 'failed';
        state.error = action.error.message || null;
      })
      .addCase(applyToClubAsync.fulfilled, (state) => {
        state.status = 'succeeded';
      });
  },
});

export const {
  setSelectedClubId,
  clearSelectedClubId,
  setClubApplication,
} = clubSlice.actions;

export const selectClubId = (state: any) => state.clubs.selectedClubId;

export const selectClubApplication = (state: any) => state.clubs.clubApplication;

export default clubSlice.reducer;
