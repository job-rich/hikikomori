import { apiClient } from './client';
import type { PageResponse } from './posts';

export interface UserProfile {
  userId: number;
  nickName: string;
  power: number;
  voteNet: number;
  reports: number;
  rank: number;
  banned: boolean;
}

export interface RankingEntry {
  userId: number;
  nickName: string;
  power: number;
  banned: boolean;
}

export function getProfile(userId: number): Promise<UserProfile> {
  return apiClient<UserProfile>(`/api/users/${userId}/profile`);
}

export function getRanking(
  page = 0,
  size = 20
): Promise<PageResponse<RankingEntry>> {
  return apiClient<PageResponse<RankingEntry>>(
    `/api/users/ranking?page=${page}&size=${size}`
  );
}
