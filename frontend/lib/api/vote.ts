import { apiClient } from './client';

export type VoteTargetType = 'POST' | 'COMMENT';
export type VoteValue = 'UP' | 'DOWN';

export interface VoteRequest {
  voterId: number;
  targetType: VoteTargetType;
  targetId: string;
  value: VoteValue;
}

export interface VoteResponse {
  value: VoteValue | null; // 토글 후 내 표 상태(취소면 null)
  score: number; // 대상 콘텐츠 현재 순추천
}

/** 추천/비추천 토글. targetUserId = 대상 작성자 */
export function vote(
  targetUserId: number,
  request: VoteRequest
): Promise<VoteResponse> {
  return apiClient<VoteResponse>(`/api/votes/${targetUserId}`, {
    method: 'POST',
    body: JSON.stringify(request),
  });
}
