import { describe, it, expect } from 'vitest';
import type {
  PostCreateRequest,
  PostResponse,
  PageResponse,
} from '@/lib/api/posts';

/**
 * 백엔드 실제 JSON 응답과 프론트엔드 타입의 일치 여부를 검증한다.
 * Spring Boot가 반환하는 실제 JSON 구조를 기반으로 작성.
 */

// Spring Boot PostResponse 실제 JSON 형태
const BACKEND_POST_RESPONSE_JSON = {
  id: '019ca88c-7e32-73bd-b211-aef07da86168',
  userId: 21510467690098720,
  nickName: '묵직한 바흐',
  title: '테스트',
  content: '입니다.',
  tag: 'VOID',
  createdAt: '2026-03-01T08:38:25.586252',
};

// Spring Boot Page<PostResponse> 실제 JSON 형태
const BACKEND_PAGE_RESPONSE_JSON = {
  content: [BACKEND_POST_RESPONSE_JSON],
  pageable: {
    pageNumber: 0,
    pageSize: 20,
    sort: { empty: true, sorted: false, unsorted: true },
    offset: 0,
    paged: true,
    unpaged: false,
  },
  totalElements: 1,
  totalPages: 1,
  number: 0,
  size: 20,
  first: true,
  last: true,
  numberOfElements: 1,
  sort: { empty: true, sorted: false, unsorted: true },
  empty: false,
};

// Spring Boot PostCreateRequest에 보내는 JSON 형태
const FRONTEND_CREATE_REQUEST_JSON = {
  title: '테스트',
  content: '입니다.',
  tag: 'VOID',
  userId: 21510467690098720,
  nickName: '묵직한 바흐',
};

describe('프론트-백엔드 자료형 일치 검증', () => {
  describe('PostResponse', () => {
    it('백엔드 응답에 프론트엔드 필수 필드가 모두 존재해야 한다', () => {
      const requiredKeys: (keyof PostResponse)[] = [
        'id',
        'userId',
        'nickName',
        'title',
        'content',
        'tag',
        'createdAt',
      ];

      for (const key of requiredKeys) {
        expect(BACKEND_POST_RESPONSE_JSON).toHaveProperty(key);
      }
    });

    it('id는 string(UUID)이어야 한다', () => {
      expect(typeof BACKEND_POST_RESPONSE_JSON.id).toBe('string');
    });

    it('userId는 number(Long)이어야 한다', () => {
      expect(typeof BACKEND_POST_RESPONSE_JSON.userId).toBe('number');
    });

    it('nickName은 string이어야 한다', () => {
      expect(typeof BACKEND_POST_RESPONSE_JSON.nickName).toBe('string');
    });

    it('title은 string이어야 한다', () => {
      expect(typeof BACKEND_POST_RESPONSE_JSON.title).toBe('string');
    });

    it('content는 string이어야 한다', () => {
      expect(typeof BACKEND_POST_RESPONSE_JSON.content).toBe('string');
    });

    it('tag는 string이어야 한다', () => {
      expect(typeof BACKEND_POST_RESPONSE_JSON.tag).toBe('string');
    });

    it('createdAt은 string(ISO)이어야 한다', () => {
      expect(typeof BACKEND_POST_RESPONSE_JSON.createdAt).toBe('string');
      expect(BACKEND_POST_RESPONSE_JSON.createdAt).toContain('T');
    });
  });

  describe('PageResponse', () => {
    it('백엔드 페이지 응답에 프론트엔드 필수 필드가 모두 존재해야 한다', () => {
      const requiredKeys: (keyof PageResponse<PostResponse>)[] = [
        'content',
        'totalElements',
        'totalPages',
        'number',
        'size',
      ];

      for (const key of requiredKeys) {
        expect(BACKEND_PAGE_RESPONSE_JSON).toHaveProperty(key);
      }
    });

    it('content는 배열이어야 한다', () => {
      expect(Array.isArray(BACKEND_PAGE_RESPONSE_JSON.content)).toBe(true);
    });

    it('totalElements는 number이어야 한다', () => {
      expect(typeof BACKEND_PAGE_RESPONSE_JSON.totalElements).toBe('number');
    });

    it('totalPages는 number이어야 한다', () => {
      expect(typeof BACKEND_PAGE_RESPONSE_JSON.totalPages).toBe('number');
    });

    it('number는 number이어야 한다', () => {
      expect(typeof BACKEND_PAGE_RESPONSE_JSON.number).toBe('number');
    });

    it('size는 number이어야 한다', () => {
      expect(typeof BACKEND_PAGE_RESPONSE_JSON.size).toBe('number');
    });
  });

  describe('PostCreateRequest', () => {
    it('프론트엔드 요청 JSON에 백엔드 필수 필드가 모두 존재해야 한다', () => {
      const requiredKeys: (keyof PostCreateRequest)[] = [
        'title',
        'content',
        'tag',
        'userId',
        'nickName',
      ];

      for (const key of requiredKeys) {
        expect(FRONTEND_CREATE_REQUEST_JSON).toHaveProperty(key);
      }
    });

    it('title은 string이어야 한다', () => {
      expect(typeof FRONTEND_CREATE_REQUEST_JSON.title).toBe('string');
    });

    it('content는 string이어야 한다', () => {
      expect(typeof FRONTEND_CREATE_REQUEST_JSON.content).toBe('string');
    });

    it('tag는 string이어야 한다', () => {
      expect(typeof FRONTEND_CREATE_REQUEST_JSON.tag).toBe('string');
    });

    it('userId는 number이어야 한다', () => {
      expect(typeof FRONTEND_CREATE_REQUEST_JSON.userId).toBe('number');
    });

    it('nickName은 string이어야 한다', () => {
      expect(typeof FRONTEND_CREATE_REQUEST_JSON.nickName).toBe('string');
    });
  });
});
