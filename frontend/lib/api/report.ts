import { apiClient } from './client';

export type ReportTargetType = 'POST' | 'COMMENT';

export type ReportReason =
  | 'SPAM'
  | 'ABUSE'
  | 'SEXUAL'
  | 'PRIVACY'
  | 'COPYRIGHT'
  | 'ETC';

export interface ReportCreateRequest {
  reporterId: number;
  targetType: ReportTargetType;
  targetId: string;
  reason: ReportReason;
  description?: string;
}

export interface ReportResponse {
  id: string;
}

/** 신고 대상 작성자(targetUserId)를 경로로, 신고 내용을 본문으로 전송 */
export function reportContent(
  targetUserId: number,
  request: ReportCreateRequest
): Promise<ReportResponse> {
  return apiClient<ReportResponse>(`/api/reports/${targetUserId}`, {
    method: 'POST',
    body: JSON.stringify(request),
  });
}

export const REPORT_REASONS: { value: ReportReason; label: string }[] = [
  { value: 'SPAM', label: '스팸·광고' },
  { value: 'ABUSE', label: '욕설·혐오 표현' },
  { value: 'SEXUAL', label: '음란·선정성' },
  { value: 'PRIVACY', label: '개인정보 노출' },
  { value: 'COPYRIGHT', label: '저작권 침해' },
  { value: 'ETC', label: '기타' },
];
