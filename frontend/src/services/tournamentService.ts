import api from './api';
import { Tournament, TournamentFilter, VerificationData } from '../types/tournament';
import { PlayerAvailabilityDTO, UpdatePlayerAvailabilityDTO } from '../types/playerAvailability';
import { Location, MatchUpdateDTO } from '../types/tournament';

const tournamentBaseURL = import.meta.env.VITE_TOURNAMENT_SERVICE_BASE_URL || 'http://localhost:8080/api/v1';

// Fetch a specific tournament by its ID
export const fetchTournamentById = async (tournamentId: number): Promise<Tournament> => {
  const response = await api.get(`/tournaments/${tournamentId}`, {
    baseURL: tournamentBaseURL,
  });
  return response.data;
};

// Fetch all tournaments
export const fetchTournaments = async (): Promise<Tournament[]> => {
  const response = await api.get('/tournaments', {
    baseURL: tournamentBaseURL,
  });
  return response.data;
};

// Join a tournament as a club
export const joinTournament = async (clubId: number, tournamentId: number): Promise<any> => {
  const response = await api.post('/tournaments/join', {clubId, tournamentId} , {
    baseURL: tournamentBaseURL,
  });
  return response.data;
};

// Create a new tournament
export const createTournament = async (tournamentData: Partial<Tournament>): Promise<Tournament> => {
  const response = await api.post('/tournaments', tournamentData, {
    baseURL: tournamentBaseURL,
  });
  return response.data;
};

// Update an existing tournament
export const updateTournament = async (tournamentId: number, tournamentData: Partial<Tournament>): Promise<Tournament> => {
  const response = await api.put(`/tournaments/${tournamentId}`, tournamentData, {
    baseURL: tournamentBaseURL,
  });
  return response.data;
};

// Remove a club from a tournament
export const removeClubFromTournament = async (tournamentId: number, clubId: number): Promise<void> => {
  const response = await api.delete(`/tournaments/${tournamentId}/clubs/${clubId}`, {
    baseURL: tournamentBaseURL,
  });
  return response.data;
};

// Fetch player availability for a specific tournament
export const getPlayerAvailability = async (tournamentId: number): Promise<PlayerAvailabilityDTO[]> => {
  const response = await api.get(`/tournaments/${tournamentId}/availability`, {
    baseURL: tournamentBaseURL,
  });
  return response.data;
};

// Update a player's availability for a tournament
export const updatePlayerAvailability = async (data: UpdatePlayerAvailabilityDTO): Promise<void> => {

  const response = await api.put('/tournaments/availability', data, {
    baseURL: tournamentBaseURL,
  });
  return response.data;
};

// Get tournaments by ClubId
export const getTournamentsByClubId = async (id: number, filter: TournamentFilter): Promise<Tournament[]> => {

  const response = await api.get(`/tournaments/${id}/tournaments`, {
    baseURL: tournamentBaseURL,
    params: { filter },
  });
  return response.data;
};

// Get all locations
export const getAllLocations = async (): Promise<Location[]> => {

  const response = await api.get('/locations', {
    baseURL: tournamentBaseURL,
  });
  return response.data;
};

// Get all locations
export const getTournamentsHosted = async (hostId: number): Promise<Tournament[]> => {

  const response = await api.get(`/tournaments/host/${hostId}`, {
    baseURL: tournamentBaseURL,
  });
  return response.data;
};

// Start a tournament
export const startTournament = async (tournamentId: number): Promise<Tournament> => {
  const response = await api.post(`/tournaments/${tournamentId}/start`, {}, {
    baseURL: tournamentBaseURL,
  });
  return response.data;
};

// Update match score in a tournament
export const updateMatchInTournament = async (
  tournamentId: number,
  matchId: number,
  matchUpdate: MatchUpdateDTO
): Promise<any> => {
  const response = await api.put(`/tournaments/${tournamentId}/${matchId}`, matchUpdate, {
    baseURL: tournamentBaseURL,
  });
  return response.data;
};

export const createLocation = async (locationData: { name: string }): Promise<Location> => {
  const response = await api.post('/locations', locationData, {
    baseURL: tournamentBaseURL,
  });
  return response.data;
};

// Payment status check endpoint
export const checkPaymentStatus = async (tournamentId: number): Promise<{ paid: boolean; status: string }> => {
  const response = await api.get(`/tournaments/${tournamentId}/payment-status`, {
    baseURL: tournamentBaseURL,
  });
  return response.data;
};

export const verifyTournamentAsync = async (tournamentId: number, verificationData: VerificationData) => {
  const response = await api.post(`/tournaments/${tournamentId}/verify`, verificationData, {
    baseURL: tournamentBaseURL,
    headers: {
      'Content-Type': 'application/json',
    },
  });
  return response.data;
};

export const approveVerification = async (tournamentId: number): Promise<Tournament> => {
  const response = await api.post(`/tournaments/${tournamentId}/approve`, null, {
    baseURL: tournamentBaseURL,
  });
  return response.data;
};

export const rejectVerification = async (tournamentId: number): Promise<Tournament> => {
  const response = await api.post(`/tournaments/${tournamentId}/reject`, null, {
    baseURL: tournamentBaseURL,
  });
  return response.data;
};

export const fetchPendingVerifications = async (): Promise<Tournament[]> => {
  const response = await api.get('/tournaments/pending-verifications', {
    baseURL: tournamentBaseURL,
  });
  return response.data;
};

export const fetchApprovedVerifications = async (): Promise<Tournament[]> => {
  const response = await api.get('/tournaments/approved-verifications', {
    baseURL: tournamentBaseURL,
  });
  return response.data;
};

export const fetchRejectedVerifications = async (): Promise<Tournament[]> => {
  const response = await api.get('/tournaments/rejected-verifications', {
    baseURL: tournamentBaseURL,
  });
  return response.data;
};
