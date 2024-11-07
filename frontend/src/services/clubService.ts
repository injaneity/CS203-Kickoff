import api from './api';
import { Club, ClubProfile } from '../types/club';
import { AxiosResponse } from 'axios';
import { fetchPlayerProfileById } from './userService';

const clubBaseURL = import.meta.env.VITE_CLUB_SERVICE_BASE_URL || 'http://localhost:8082/api/v1';

export const fetchClubs = async (): Promise<Club[]> => {
  const response = await api.get('/clubs', {
    baseURL: clubBaseURL,
  });
  const clubsData = response.data;

  const updatedClubsWithPlayerInfo = await Promise.all(
    clubsData.map(async (club: any) => {
      // Fetch player profiles for each player in the club
      const playerProfiles = await Promise.all(
        club.players.map((playerId: number) => fetchPlayerProfileById(playerId.toString()))
      );

      // Check if any player is blacklisted
      const hasPenalisedPlayer = playerProfiles.some(
        (player) => player.status === 'STATUS_BLACKLISTED'
      );

      return {
        ...club,
        players: playerProfiles, // Replace players array with full player profiles
        penaltyStatus: {
          ...club.penaltyStatus,
          hasPenalisedPlayer, // Add the hasPenalisedPlayer flag
        },
      };
    })
  );

  return updatedClubsWithPlayerInfo;
};

export const applyToClub = async (clubId: number, playerId: number, desiredPosition: string): Promise<any> => {
  const response = await api.post(`/clubs/${clubId}/apply`, { playerId, desiredPosition }, {
    baseURL: clubBaseURL,
  });
  return response.data;
};

export const createClub = async (clubData: object): Promise<AxiosResponse> => {
  const response = await api.post('/clubs/createClub', clubData, {
    baseURL: clubBaseURL,
  });
  return response;
};

export const getClubByPlayerId = async (playerId: number): Promise<Club> => {
  const response = await api.get(`/clubs/player/${playerId}`, {
    baseURL: clubBaseURL,
  });
  return response.data;
};

export const getClubProfileById = async (clubId: number): Promise<ClubProfile> => {
  const response = await api.get(`/clubs/${clubId}`, {
    baseURL: clubBaseURL,
  });
  return response.data;
};

export const getClubApplication = async (clubId: number): Promise<AxiosResponse> => {
  const response = await api.get(`/clubs/${clubId}/applications`, {
    baseURL: clubBaseURL,
  });
  return response;
};

export const updatePlayerApplication = async (clubId: number, playerId: number, applicationStatus: string): Promise<AxiosResponse> => {
  const response = await api.post(`/clubs/${clubId}/applications/${playerId}`, { applicationStatus }, {
    baseURL: clubBaseURL,
  });
  return response;
};

export const updateClubPenaltyStatus = async (clubId: number, penaltyType: string, banUntil?: string): Promise<Club> => {
  const response = await api.put(`/clubs/${clubId}/status`, {
    banUntil,
    penaltyType,
  }, {
    baseURL: clubBaseURL,
  });
  return response.data;
};

export const leaveClub = async (clubId: number, playerId: number): Promise<AxiosResponse> => {
  const response = await api.patch(
    `/clubs/${clubId}/leavePlayer`,
    { playerId },
    {
      baseURL: clubBaseURL,
    }
  );
  return response;
};


export const getPlayersInClub = async (clubId: number): Promise<AxiosResponse> => {
  const response = await api.patch(`/clubs/${clubId}/players`, {
    baseURL: clubBaseURL,
  });
  return response;
};