import api from './api';
import { Tournament } from '../types/tournament';

export const fetchTournamentById = async (tournamentId: number): Promise<Tournament> => {
  const response = await api.get(`/tournaments/${tournamentId}`);
  return response.data;
};

export const fetchTournaments = async (): Promise<Tournament[]> => {
  const response = await api.get('/tournaments');
  return response.data;
};

export const joinTournament = async (clubId: number, tournamentId: number): Promise<any> => {
  const response = await api.post('/tournaments/join', { clubId, tournamentId });
  return response.data;
};

export const createTournament = async (tournamentData: Partial<Tournament>): Promise<Tournament> => {
  const response = await api.post('/tournaments', tournamentData);
  return response.data;
};

export const updateTournament = async (tournamentId: number, tournamentData: Partial<Tournament>): Promise<Tournament> => {
  const response = await api.put(`/tournaments/${tournamentId}`, tournamentData);
  return response.data;
};

export const removeClubFromTournament = async (tournamentId: number, clubId: number): Promise<void> => {
  const response = await api.delete(`/tournaments/${tournamentId}/clubs/${clubId}`);
  return response.data;
};