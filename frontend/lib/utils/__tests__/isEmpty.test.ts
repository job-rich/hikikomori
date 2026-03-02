import { describe, it, expect } from 'vitest';
import { isEmpty } from '@/lib/utils/isEmpty';

describe('isEmpty', () => {
  it('null이면 true를 반환해야 한다', () => {
    expect(isEmpty(null)).toBe(true);
  });

  it('undefined이면 true를 반환해야 한다', () => {
    expect(isEmpty(undefined)).toBe(true);
  });

  it('빈 문자열이면 true를 반환해야 한다', () => {
    expect(isEmpty('')).toBe(true);
  });

  it('공백만 있는 문자열이면 true를 반환해야 한다', () => {
    expect(isEmpty('   ')).toBe(true);
  });

  it('값이 있는 문자열이면 false를 반환해야 한다', () => {
    expect(isEmpty('hello')).toBe(false);
  });

  it('숫자 0이면 false를 반환해야 한다', () => {
    expect(isEmpty(0)).toBe(false);
  });

  it('false이면 false를 반환해야 한다', () => {
    expect(isEmpty(false)).toBe(false);
  });
});
