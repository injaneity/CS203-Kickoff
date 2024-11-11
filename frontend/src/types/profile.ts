// Enums for PlayerPosition
export enum PlayerPosition {
  POSITION_FORWARD = "POSITION_FORWARD",
  POSITION_MIDFIELDER = "POSITION_MIDFIELDER",
  POSITION_DEFENDER = "POSITION_DEFENDER",
  POSITION_GOALKEEPER = "POSITION_GOALKEEPER"
}

// Enums for PlayerStatus
export enum PlayerStatus {
  STATUS_REPORTED = "STATUS_REPORTED",
  STATUS_BLACKLISTED = "STATUS_BLACKLISTED",
}

// Interface for Club
export interface Club {
  id: number;
  name: string;
  description?: string;
  // Add other Club properties if needed
}

// Interface for User
export interface UserPublicDetails {
  id: number;
  username: string;
  profilePictureUrl: string;
}

// Interface for PlayerProfile
export interface PlayerProfile {
  id: number;
  username: string;
  preferredPositions: PlayerPosition[];
  profileDescription: string;
  status: PlayerStatus | null;
  profilePictureUrl: string;
}