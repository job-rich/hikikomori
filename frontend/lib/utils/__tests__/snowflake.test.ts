import { describe, it, expect } from 'vitest';
import { generateSnowflakeId } from '@/lib/utils/snowflake';

describe('generateSnowflakeId', () => {
  it('문자열을 반환해야 한다', () => {
    const id = generateSnowflakeId();
    expect(typeof id).toBe('string');
  });

  it('빈 문자열이 아니어야 한다', () => {
    const id = generateSnowflakeId();
    expect(id.length).toBeGreaterThan(0);
  });

  it('숫자로만 구성되어야 한다', () => {
    const id = generateSnowflakeId();
    expect(id).toMatch(/^\d+$/);
  });

  it('호출할 때마다 고유한 값을 반환해야 한다', () => {
    const ids = new Set(
      Array.from({ length: 100 }, () => generateSnowflakeId())
    );
    expect(ids.size).toBe(100);
  });
});
