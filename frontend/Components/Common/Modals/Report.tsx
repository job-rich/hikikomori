'use client';

import { useState } from 'react';
import {
  reportContent,
  REPORT_REASONS,
  type ReportReason,
  type ReportTargetType,
} from '@/lib/api/report';
import { isApiError } from '@/lib/api/client';
import { useUserStore } from '@/lib/stores/userStore';

interface ReportModalProps {
  targetType: ReportTargetType;
  targetId: string;
  /** 신고 대상 작성자 userId */
  targetUserId: number;
  onClose: () => void;
  onReported?: () => void;
}

export default function ReportModal({
  targetType,
  targetId,
  targetUserId,
  onClose,
  onReported,
}: ReportModalProps) {
  const snowflakeId = useUserStore((s) => s.snowflakeId);
  const [reason, setReason] = useState<ReportReason>('ABUSE');
  const [description, setDescription] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [message, setMessage] = useState<string | null>(null);

  const handleSubmit = async () => {
    if (snowflakeId == null || submitting) return;
    setSubmitting(true);
    setMessage(null);
    try {
      await reportContent(targetUserId, {
        reporterId: Number(snowflakeId),
        targetType,
        targetId,
        reason,
        description: description.trim() || undefined,
      });
      onReported?.();
      onClose();
    } catch (err) {
      if (isApiError(err, 409)) {
        setMessage('이미 신고한 콘텐츠입니다.');
      } else if (isApiError(err, 400)) {
        setMessage('본인의 콘텐츠는 신고할 수 없습니다.');
      } else {
        setMessage('신고 처리에 실패했습니다. 잠시 후 다시 시도하세요.');
      }
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 px-4"
      onClick={onClose}
    >
      <div
        className="w-full max-w-md rounded-lg border border-border bg-card p-6"
        onClick={(e) => e.stopPropagation()}
      >
        <div className="mb-4 flex items-center justify-between">
          <h3 className="text-base font-bold text-foreground">⚠ 신고하기</h3>
          <button
            type="button"
            onClick={onClose}
            className="text-lg text-muted-foreground hover:text-foreground"
            aria-label="닫기"
          >
            ×
          </button>
        </div>

        <div className="mb-4 space-y-1">
          {REPORT_REASONS.map((r) => (
            <label
              key={r.value}
              className="flex cursor-pointer items-center gap-2 rounded p-2 hover:bg-muted/50"
            >
              <input
                type="radio"
                name="report-reason"
                checked={reason === r.value}
                onChange={() => setReason(r.value)}
              />
              <span className="text-sm text-foreground">{r.label}</span>
            </label>
          ))}
        </div>

        <textarea
          value={description}
          onChange={(e) => setDescription(e.target.value)}
          rows={3}
          placeholder="추가 설명 (선택)"
          className="w-full resize-y rounded border border-border bg-transparent px-3 py-2 text-sm text-foreground placeholder:text-muted-foreground focus:outline-none"
        />

        {message && <p className="mt-2 text-xs text-destructive">{message}</p>}

        <div className="mt-4 flex justify-end gap-2">
          <button
            type="button"
            onClick={onClose}
            className="rounded border border-border px-3 py-1.5 text-xs hover:bg-muted"
          >
            취소
          </button>
          <button
            type="button"
            onClick={handleSubmit}
            disabled={submitting}
            className="rounded bg-accent px-3 py-1.5 text-xs font-medium text-accent-foreground disabled:opacity-50"
          >
            {submitting ? '신고 중…' : '신고하기'}
          </button>
        </div>
      </div>
    </div>
  );
}
