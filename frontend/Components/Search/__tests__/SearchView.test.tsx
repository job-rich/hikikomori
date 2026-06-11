import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import {
  render,
  screen,
  fireEvent,
  waitFor,
  cleanup,
  act,
} from '@testing-library/react';
import SearchView from '@/Components/Search/SearchView';
import * as searchApi from '@/lib/api/search';
import { RECENT_SEARCHES_KEY } from '@/lib/utils/recentSearches';
import type { SearchResult } from '@/lib/api/search';

// ── 라우터/검색파라미터 mock ────────────────────────────────────────────────
const routerMock = vi.hoisted(() => ({
  push: vi.fn(),
  replace: vi.fn(),
  prefetch: vi.fn(),
}));

vi.mock('next/navigation', () => ({
  useRouter: () => routerMock,
  useSearchParams: () => ({ get: () => null }),
}));

// ── search API mock ────────────────────────────────────────────────────────
vi.mock('@/lib/api/search', async (importOriginal) => {
  const actual = await importOriginal<typeof import('@/lib/api/search')>();
  return {
    ...actual,
    searchPosts: vi.fn(),
    suggest: vi.fn(),
    getSearchCounts: vi.fn(),
  };
});

// ── PostCard mock (PostCard 특유 마크업 검증용) ───────────────────────────
vi.mock('@/Components/Common/Post/Post-Card/Post-Card', () => ({
  default: vi.fn(({ title }: { title?: string }) => (
    <article data-testid="post-card">
      <button aria-label="추천">추천</button>
      {title && <span data-testid="post-card-title">{title}</span>}
    </article>
  )),
}));

// ── 헬퍼 ─────────────────────────────────────────────────────────────────
function makePageResponse(
  items: Partial<SearchResult>[]
): import('@/lib/api/posts').PageResponse<SearchResult> {
  return {
    content: items.map((item) => ({
      type: 'POST' as const,
      id: item.id ?? '1',
      title: item.title ?? '제목',
      nickName: item.nickName ?? '유저',
      tag: item.tag ?? null,
      snippet: item.snippet ?? '내용',
      commentCount: item.commentCount ?? 0,
      postCount: item.postCount ?? 0,
      createdAt: item.createdAt ?? '2026-01-01T00:00:00Z',
      ...item,
    })),
    totalElements: items.length,
    totalPages: 1,
    number: 0,
    size: 10,
  };
}

// ── 공통 beforeEach/afterEach ─────────────────────────────────────────────
beforeEach(() => {
  localStorage.clear();
  vi.mocked(searchApi.searchPosts).mockResolvedValue(makePageResponse([]));
  vi.mocked(searchApi.suggest).mockResolvedValue([]);
  vi.mocked(searchApi.getSearchCounts).mockResolvedValue({
    post: 0,
    comment: 0,
    total: 0,
  });
});

afterEach(() => {
  cleanup();
  vi.restoreAllMocks();
});

// ══════════════════════════════════════════════════════════════════════════
describe('SearchView', () => {
  // ── autoFocus ────────────────────────────────────────────────────────
  it('검색 입력창이 autoFocus로 포커스를 가진다', () => {
    render(<SearchView />);
    const input = screen.getByPlaceholderText('키워드, 닉네임, 태그...');
    // React는 autoFocus prop을 HTML attribute가 아닌 .focus() 호출로 처리
    expect(document.activeElement).toBe(input);
  });

  // ── ④ 최근검색 저장 시점 ─────────────────────────────────────────────
  describe('④ 최근검색 저장 시점', () => {
    it('타이핑만으로는 검색·최근검색 저장 안 함', async () => {
      vi.useFakeTimers();
      try {
        render(<SearchView />);
        const input = screen.getByPlaceholderText('키워드, 닉네임, 태그...');

        fireEvent.change(input, { target: { value: '라이브테스트' } });

        // 디바운스 충분히 경과해도 searchPosts 미호출 (자동완성 우선)
        await act(async () => {
          vi.advanceTimersByTime(400);
        });

        expect(searchApi.searchPosts).not.toHaveBeenCalled();
        // 저장 안 됨
        expect(localStorage.getItem(RECENT_SEARCHES_KEY)).toBeNull();
      } finally {
        vi.useRealTimers();
      }
    });

    it('Enter 키 검색 시 최근검색 저장', async () => {
      vi.mocked(searchApi.searchPosts).mockResolvedValue(
        makePageResponse([{ title: '결과' }])
      );

      render(<SearchView />);
      const input = screen.getByPlaceholderText('키워드, 닉네임, 태그...');

      fireEvent.change(input, { target: { value: '엔터검색어' } });
      fireEvent.keyDown(input, { key: 'Enter' });

      await waitFor(() => {
        const recent = JSON.parse(
          localStorage.getItem(RECENT_SEARCHES_KEY) ?? '[]'
        ) as string[];
        expect(recent).toContain('엔터검색어');
      });
    });

    it('검색 버튼 클릭 시 최근검색 저장', async () => {
      vi.mocked(searchApi.searchPosts).mockResolvedValue(
        makePageResponse([{ title: '결과' }])
      );

      render(<SearchView />);
      const input = screen.getByPlaceholderText('키워드, 닉네임, 태그...');

      fireEvent.change(input, { target: { value: '버튼검색어' } });
      fireEvent.click(screen.getByRole('button', { name: /🔍/ }));

      await waitFor(() => {
        const recent = JSON.parse(
          localStorage.getItem(RECENT_SEARCHES_KEY) ?? '[]'
        ) as string[];
        expect(recent).toContain('버튼검색어');
      });
    });
  });

  // ── ⑥ 인기검색어 제거 ────────────────────────────────────────────────
  describe('⑥ 인기검색어 제거', () => {
    it('idle 드롭다운에 "인기 검색어" 섹션 없음', async () => {
      localStorage.setItem(
        RECENT_SEARCHES_KEY,
        JSON.stringify(['최근검색어1'])
      );

      render(<SearchView />);
      const input = screen.getByPlaceholderText('키워드, 닉네임, 태그...');
      fireEvent.focus(input);

      await screen.findByText('최근 검색어');

      expect(screen.queryByText('인기 검색어')).toBeNull();
      expect(screen.queryByText(/🔥/)).toBeNull();
    });
  });

  // ── ⑤-a 타입탭 개수 배지 ─────────────────────────────────────────────
  describe('⑤-a 탭 개수 배지', () => {
    it('검색 성공 후 탭에 개수 배지 렌더', async () => {
      vi.mocked(searchApi.searchPosts).mockResolvedValue(
        makePageResponse([{ title: '결과' }])
      );
      vi.mocked(searchApi.getSearchCounts).mockResolvedValue({
        post: 12,
        comment: 5,
        total: 17,
      });

      render(<SearchView />);
      const input = screen.getByPlaceholderText('키워드, 닉네임, 태그...');

      fireEvent.change(input, { target: { value: '테스트' } });
      fireEvent.keyDown(input, { key: 'Enter' });

      await waitFor(() => {
        const postTab = screen.getByRole('button', { name: /게시글/ });
        expect(postTab.textContent).toContain('12');
      });

      // "댓글" 타입탭과 "댓글많은순" 정렬버튼이 함께 있으므로 필터링
      const commentTypeTab = screen
        .getAllByRole('button', { name: /댓글/ })
        .find((btn) => !btn.textContent?.includes('많은순'));
      expect(commentTypeTab?.textContent).toContain('5');
    });
  });

  // ── ⑤-d 빈결과 개선 + 전체삭제 ──────────────────────────────────────
  describe('⑤-d 빈결과 + 전체삭제', () => {
    it('결과 0건 시 "다른 키워드로 검색해보세요" 표시', async () => {
      vi.mocked(searchApi.searchPosts).mockResolvedValue(makePageResponse([]));

      render(<SearchView />);
      const input = screen.getByPlaceholderText('키워드, 닉네임, 태그...');

      fireEvent.change(input, { target: { value: '없는검색어' } });
      fireEvent.keyDown(input, { key: 'Enter' });

      await screen.findByText('다른 키워드로 검색해보세요');
    });

    it('전체 삭제 버튼 클릭 시 recent 비움', async () => {
      localStorage.setItem(
        RECENT_SEARCHES_KEY,
        JSON.stringify(['검색1', '검색2'])
      );

      render(<SearchView />);
      const input = screen.getByPlaceholderText('키워드, 닉네임, 태그...');
      fireEvent.focus(input);

      await screen.findByText('전체 삭제');
      fireEvent.mouseDown(screen.getByRole('button', { name: '전체 삭제' }));

      await waitFor(() => {
        expect(localStorage.getItem(RECENT_SEARCHES_KEY)).toBeNull();
      });
    });
  });

  // ── sticky 컨트롤 헤더 ───────────────────────────────────────────────
  describe('sticky 컨트롤 헤더', () => {
    it('search-controls 컨테이너가 sticky 클래스를 가진다', () => {
      render(<SearchView />);
      const controls = screen.getByTestId('search-controls');
      expect(controls.className).toContain('sticky');
    });
  });

  // ── ⑦ 검색 후 필터/정렬 변경 시 재검색 ──────────────────────────────
  describe('⑦ 검색 후 필터/정렬 변경', () => {
    it('검색 적용 후 정렬 변경 시 searchPosts 재호출', async () => {
      vi.mocked(searchApi.searchPosts).mockResolvedValue(makePageResponse([]));

      render(<SearchView />);
      const input = screen.getByPlaceholderText('키워드, 닉네임, 태그...');

      fireEvent.change(input, { target: { value: '필터테스트' } });
      fireEvent.keyDown(input, { key: 'Enter' });

      // 첫 검색 완료 대기
      await screen.findByText('다른 키워드로 검색해보세요');
      vi.mocked(searchApi.searchPosts).mockClear();

      fireEvent.click(screen.getByRole('button', { name: '최신순' }));

      await waitFor(() =>
        expect(searchApi.searchPosts).toHaveBeenCalledTimes(1)
      );
    });
  });

  // ── ② POST 결과 PostCard 렌더 ─────────────────────────────────────────
  describe('② POST 결과 PostCard 마크업', () => {
    it('POST 결과 항목이 PostCard(추천 버튼 포함) 로 렌더', async () => {
      vi.mocked(searchApi.searchPosts).mockResolvedValue(
        makePageResponse([
          {
            type: 'POST',
            id: '42',
            title: 'PostCard 테스트 제목',
            snippet: '내용 미리보기',
          },
        ])
      );

      render(<SearchView />);
      const input = screen.getByPlaceholderText('키워드, 닉네임, 태그...');

      fireEvent.change(input, { target: { value: 'PostCard' } });
      fireEvent.keyDown(input, { key: 'Enter' });

      const card = await screen.findByTestId('post-card');
      expect(card).toBeInTheDocument();
      expect(screen.getByRole('button', { name: '추천' })).toBeInTheDocument();
    });

  });
});
