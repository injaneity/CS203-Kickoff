import api from './api';
import { PlayerProfile, PlayerPosition, UserPublicDetails} from '../types/profile';
import { AxiosResponse } from 'axios';

// Set the base URL for the user service
const userServiceBaseURL = import.meta.env.VITE_USER_SERVICE_BASE_URL || 'http://localhost:8081/api/v1';

// Set the base URL for the tournament service
const tournamentServiceBaseURL = import.meta.env.VITE_TOURNAMENT_SERVICE_BASE_URL || 'http://localhost:8080/api';

export const fetchPlayerProfileById = async (id: string): Promise<PlayerProfile> => {
  const response = await api.get(`/playerProfiles/${id}`, {
    baseURL: userServiceBaseURL,
  });
  return response.data;
};

export const fetchUserPublicInfoById = async (id: string): Promise<UserPublicDetails> => {
  const response = await api.get(`/users/publicinfo/${id}`, {
    baseURL: userServiceBaseURL,
  });
  return response.data;
};

export const updatePlayerProfile = async (playerId: number, preferredPositions: PlayerPosition[], profileDescription: string): Promise<any> => {
  const response = await api.put(`/playerProfiles/${playerId}/update`, { preferredPositions, profileDescription }, {
    baseURL: userServiceBaseURL
  });
  return response.data;
};

export const signup = async (payload: object): Promise<AxiosResponse> => {
  const response = await api.post('users', payload, {
    baseURL: userServiceBaseURL
  });
  return response;
};

export const login = async (username: string, password: string): Promise<AxiosResponse> => {
  const response = await api.post('/users/login', { username, password }, {
    baseURL: userServiceBaseURL
  });
  return response;
};

export const fetchAllPlayers = async (): Promise<PlayerProfile[]> => {
  const response = await api.get('/playerProfiles', {
    baseURL: userServiceBaseURL 
  });
  return response.data;
};

export interface Tournament {
  id: number;
  name: string;
  verificationImageUrl: string;
  verificationStatus: string;
}

// Verification Methods
export const fetchPendingVerifications = async (): Promise<Tournament[]> => {
  const response = await api.get('/tournaments/pending-verifications', {
    baseURL: tournamentServiceBaseURL,
  });
  return response.data;
};

export const approveVerification = async (tournamentId: number): Promise<void> => {
  await api.post(`/tournaments/${tournamentId}/approve`, null, {
    baseURL: tournamentServiceBaseURL,
  });
};

export const rejectVerification = async (tournamentId: number): Promise<void> => {
  await api.post(`/tournaments/${tournamentId}/reject`, null, {
    baseURL: tournamentServiceBaseURL,
  });
};