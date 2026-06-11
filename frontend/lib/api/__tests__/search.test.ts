import { describe, it, expect, vi, beforeEach } from 'vitest';
import { searchPosts, getSearchCounts } from '@/lib/api/search';

function mockFetchJson(data: unknown): Response {
  const json = JSON.stringify(data);
  return {
    ok: true,
    status: 200,
    text: () => Promise.resolve(json),
  } as Response;
}

const mockSearchResponse = {
  content: [
    {
      type: 'POST',
      id: '123',
      title: '철학적 질문',
      nickName: '고독한철학자',
      tag: 'PHILOSOPHY',
      snippet: '인생이란 무엇인가',
      commentCount: 5,
      postCount: 0,
      createdAt: '2026-06-01T10:00:00',
    },
  ],
  totalElements: 1,
  totalPages: 1,
  number: 0,
  size: 10,
};

beforeEach(() => {
  vi.restoreAllMocks();
});

describe('searchPosts', () => {
  it('query 파라미터를 포함한 GET 요청을 보내야 한다', async () => {
    vi.spyOn(global, 'fetch').mockResolvedValue(
      mockFetchJson(mockSearchResponse)
    );
    await searchPosts({ query: '철학' });
    const [[url]] = vi.mocked(fetch).mock.calls;
    expect(url).toContain('/api/posts/search');
    expect(url).toContain('query=');
  });

  it('type을 대문자로 변환해 전송해야 한다', async () => {
    vi.spyOn(global, 'fetch').mockResolvedValue(
      mockFetchJson(mockSearchResponse)
    );
    await searchPosts({ query: '테스트', type: 'post' });
    const [[url]] = vi.mocked(fetch).mock.calls;
    expect(url).toContain('type=POST');
  });

  it('sort를 대문자로 변환해 전송해야 한다', async () => {
    vi.spyOn(global, 'fetch').mockResolvedValue(
      mockFetchJson(mockSearchResponse)
    );
    await searchPosts({ query: '테스트', sort: 'latest' });
    const [[url]] = vi.mocked(fetch).mock.calls;
    expect(url).toContain('sort=LATEST');
  });

  it('한글 tag를 API enum으로 변환해 전송해야 한다', async () => {
    vi.spyOn(global, 'fetch').mockResolvedValue(
      mockFetchJson(mockSearchResponse)
    );
    await searchPosts({ query: '테스트', tag: '철학' });
    const [[url]] = vi.mocked(fetch).mock.calls;
    expect(url).toContain('tag=PHILOSOPHY');
  });

  it('응답의 tag를 한글로 변환해 반환해야 한다', async () => {
    vi.spyOn(global, 'fetch').mockResolvedValue(
      mockFetchJson(mockSearchResponse)
    );
    const result = await searchPosts({ query: '철학' });
    expect(result.content[0].tag).toBe('철학');
  });

  it('page 파라미터를 포함해야 한다', async () => {
    vi.spyOn(global, 'fetch').mockResolvedValue(
      mockFetchJson(mockSearchResponse)
    );
    await searchPosts({ query: '테스트', page: 2 });
    const [[url]] = vi.mocked(fetch).mock.calls;
    expect(url).toContain('page=2');
  });

  it('size 미지정 시 URL에 size=6이 포함돼야 한다', async () => {
    vi.spyOn(global, 'fetch').mockResolvedValue(
      mockFetchJson(mockSearchResponse)
    );
    await searchPosts({ query: '테스트' });
    const [[url]] = vi.mocked(fetch).mock.calls;
    expect(url).toContain('size=6');
  });
});

describe('getSearchCounts', () => {
  it('빈 q면 즉시 0 반환 (fetch 호출 없음)', async () => {
    const spy = vi.spyOn(global, 'fetch');
    const result = await getSearchCounts({ query: '  ' });
    expect(result).toEqual({ post: 0, comment: 0, total: 0 });
    expect(spy).not.toHaveBeenCalled();
  });

  it('size=1 로 2회 fetch 호출', async () => {
    vi.spyOn(global, 'fetch').mockResolvedValue(
      mockFetchJson({ ...mockSearchResponse, totalElements: 5 })
    );
    await getSearchCounts({ query: '철학' });
    expect(vi.mocked(fetch).mock.calls.length).toBe(2);
    for (const [url] of vi.mocked(fetch).mock.calls) {
      expect(String(url)).toContain('size=1');
    }
  });

  it('totalElements 집계 — total=post+comment', async () => {
    vi.spyOn(global, 'fetch')
      .mockResolvedValueOnce(
        mockFetchJson({ ...mockSearchResponse, totalElements: 10 })
      )
      .mockResolvedValueOnce(
        mockFetchJson({ ...mockSearchResponse, totalElements: 5 })
      );
    const result = await getSearchCounts({ query: '철학' });
    expect(result.post).toBe(10);
    expect(result.comment).toBe(5);
    expect(result.total).toBe(15);
  });
});
