import { describe, it, expect } from 'vitest';
import { formatDate } from '@/lib/utils/formatDate';

describe('formatDate', () => {
  it('ISO 타임스탬프를 yyyy-MM-dd HH:mm:ss 형식으로 변환해야 한다', () => {
    expect(formatDate('2026-03-01T08:46:20.742285')).toBe(
      '2026-03-01 08:46:20'
    );
  });

  it('마이크로초가 없는 타임스탬프도 처리해야 한다', () => {
    expect(formatDate('2026-03-01T08:46:20')).toBe('2026-03-01 08:46:20');
  });

  it('T를 공백으로 치환해야 한다', () => {
    const result = formatDate('2026-01-15T12:30:45.123');
    expect(result).not.toContain('T');
    expect(result).toBe('2026-01-15 12:30:45');
  });
});
