export interface Match {
  id: number;
  matchNumber: number;
  club1Id: number | null;
  club2Id: number | null;
  club1Score: number;
  club2Score: number;
  winningClubId: number | null;
  over: boolean;
}

export interface Round {
  id: number;
  roundNumber: number;
  matches: Match[];
}

export interface Bracket {
  rounds: Round[];
} 