// PenaltyType Enum
export enum PenaltyType {
  NONE = 'NONE',
  BLACKLISTED = 'BLACKLISTED',
  REPORTED = 'REPORTED'
}

// ClubPenaltyStatus Interface
export interface ClubPenaltyStatus {
  banUntil?: string; // ISO string for date-time
  penaltyType: PenaltyType;
  active:boolean;
  hasPenalisedPlayer:boolean;
}

export interface Club {
  id: number;
  name: string;
  description?: string;
  players: { id: number }[];
  elo: number;
  captainId: number;
  ratingDeviation: number;
  clubDescription: string;
  penaltyStatus: ClubPenaltyStatus;
}

export interface ClubProfile {
  id: number;
  name: string;
  description: string;
  elo: number;
  captainId: number;
  players: { id: number }[];
  ratingDeviation: number;
  clubDescription: string;
  penaltyStatus: ClubPenaltyStatus;
}