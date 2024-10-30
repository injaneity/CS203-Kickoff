import api from './api';
import { PlayerProfile, PlayerPosition, UserPublicDetails} from '../types/profile';
import { AxiosResponse } from 'axios';

// Set the base URL at the top of the file (from environment variable or a hardcoded value)
const playerProfileBaseURL = import.meta.env.VITE_USER_SERVICE_BASE_URL || 'http://localhost:8081/api/v1';

export const fetchPlayerProfileById = async (id: string): Promise<PlayerProfile> => {
  const response = await api.get(`/playerProfiles/${id}`, {
    baseURL: playerProfileBaseURL,
  });
  return response.data;
};

export const fetchUserPublicInfoById = async (id: string): Promise<UserPublicDetails> => {
  const response = await api.get(`/users/publicinfo/${id}`, {
    baseURL: playerProfileBaseURL,
  });
  return response.data;
};

// Update player profile
export const updatePlayerProfile = async (playerId: number, preferredPositions: PlayerPosition[], profileDescription: string): Promise<any> => {
  const response = await api.put(`/playerProfiles/${playerId}/update`, { preferredPositions, profileDescription }, {
    baseURL: playerProfileBaseURL // Use the baseURL set at the top
  });
  return response.data;
};

// Signup
export const signup = async (payload: object): Promise<AxiosResponse> => {
  const response = await api.post('users', payload, {
    baseURL: playerProfileBaseURL // Use the baseURL set at the top
  });
  return response;
};

// Login
export const login = async (username: string, password: string): Promise<AxiosResponse> => {
  const response = await api.post('/users/login', { username, password }, {
    baseURL: playerProfileBaseURL // Use the baseURL set at the top
  });
  return response;
};

// Fetch all player profiles
export const fetchAllPlayers = async (): Promise<PlayerProfile[]> => {
  const response = await api.get('/playerProfiles', {
    baseURL: playerProfileBaseURL 
  });
  return response.data;
};

export interface Verification {
  id: number;
  tournamentId: number;
  tournamentName: string;
  imageUrl: string;
}

// Verification Methods
export const fetchPendingVerifications = async (): Promise<Verification[]> => {
  const response = await api.get('/tournaments/pending-verifications', {
    baseURL: playerProfileBaseURL,
  });
  return response.data;
};

export const approveVerification = async (verificationId: number): Promise<void> => {
  await api.post(`/tournaments/${verificationId}/approve`, null, {
    baseURL: playerProfileBaseURL,
  });
};

export const rejectVerification = async (verificationId: number): Promise<void> => {
  await api.post(`/tournaments/${verificationId}/reject`, null, {
    baseURL: playerProfileBaseURL,
  });
};