import { AxiosResponse } from 'axios';
import { PlayerPosition, PlayerProfile, UserPublicDetails , PlayerStatus} from '../types/profile';
import api from './api';


// Set the base URL for the user service
const userServiceBaseURL = import.meta.env.VITE_USER_SERVICE_BASE_URL || 'http://localhost:8081/api/v1';

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

export const fetchAllPlayerProfiles = async (): Promise<PlayerProfile[]> => {
  const response = await api.get('/playerProfiles', {
    baseURL: userServiceBaseURL 
  });
  return response.data;
};

export const updatePlayerStatus = async (playerId: number, status: PlayerStatus | null): Promise<any> => {
  try {
    const response = await api.put(`/playerProfiles/${playerId}/status`, { playerStatus: status }, {
      baseURL: userServiceBaseURL
    });
    return response.data;
  } catch (error) {
    console.error("Failed to update player status:", error);
    throw error; // Re-throw the error for the calling function to handle it
  }
};