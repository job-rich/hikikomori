import { beforeEach, describe, expect, it } from 'vitest';
import {
  RECENT_SEARCHES_KEY,
  addRecentSearch,
  clearRecentSearches,
  getRecentSearches,
  removeRecentSearch,
} from '@/lib/utils/recentSearches';

describe('recentSearches', () => {
  beforeEach(() => {
    localStorage.clear();
  });

  describe('getRecentSearches', () => {
    it('빈 localStorage → 빈 배열 반환', () => {
      expect(getRecentSearches()).toEqual([]);
    });

    it('깨진 JSON → 빈 배열 반환', () => {
      localStorage.setItem(RECENT_SEARCHES_KEY, 'not-json{{{');
      expect(getRecentSearches()).toEqual([]);
    });

    it('저장된 검색어 반환', () => {
      localStorage.setItem(RECENT_SEARCHES_KEY, JSON.stringify(['a', 'b']));
      expect(getRecentSearches()).toEqual(['a', 'b']);
    });
  });

  describe('addRecentSearch', () => {
    it('새 검색어를 맨 앞에 추가', () => {
      const result = addRecentSearch('hello');
      expect(result[0]).toBe('hello');
    });

    it('중복 제거 후 맨 앞에 재추가', () => {
      addRecentSearch('a');
      addRecentSearch('b');
      addRecentSearch('a');
      const result = getRecentSearches();
      expect(result[0]).toBe('a');
      expect(result.filter((x) => x === 'a').length).toBe(1);
    });

    it('8개 제한', () => {
      for (let i = 0; i < 10; i++) addRecentSearch(`q${i}`);
      expect(getRecentSearches()).toHaveLength(8);
    });

    it('빈 문자열(공백) 무시', () => {
      addRecentSearch('   ');
      expect(getRecentSearches()).toHaveLength(0);
    });

    it('추가 후 새 배열 반환', () => {
      const result = addRecentSearch('test');
      expect(result[0]).toBe('test');
    });
  });

  describe('removeRecentSearch', () => {
    it('해당 항목 제거 후 배열 반환', () => {
      addRecentSearch('a');
      addRecentSearch('b');
      const result = removeRecentSearch('a');
      expect(result).not.toContain('a');
      expect(result).toContain('b');
    });

    it('없는 항목 제거 시 기존 배열 유지', () => {
      addRecentSearch('a');
      removeRecentSearch('b');
      expect(getRecentSearches()).toContain('a');
    });
  });

  describe('clearRecentSearches', () => {
    it('모든 항목 제거', () => {
      addRecentSearch('a');
      addRecentSearch('b');
      clearRecentSearches();
      expect(getRecentSearches()).toHaveLength(0);
    });
  });
});
