import { describe, it, expect, vi, beforeEach } from 'vitest';
import { createPost, getPosts } from '@/lib/api/posts';

const mockPostResponse = {
  id: '019ca88c-7e32-73bd-b211-aef07da86168',
  userId: 12345,
  nickName: '묵직한 바흐',
  title: '테스트',
  content: '입니다.',
  tag: 'VOID',
  createdAt: '2026-03-01T08:38:25.586252',
};

const mockPageResponse = {
  content: [mockPostResponse],
  totalElements: 1,
  totalPages: 1,
  number: 0,
  size: 20,
};

beforeEach(() => {
  vi.restoreAllMocks();
});

describe('createPost', () => {
  it('POST 요청을 보내고 생성된 게시글을 반환해야 한다', async () => {
    vi.spyOn(global, 'fetch').mockResolvedValue({
      ok: true,
      json: () => Promise.resolve(mockPostResponse),
    } as Response);

    const result = await createPost({
      title: '테스트',
      content: '입니다.',
      tag: 'VOID',
      userId: 12345,
      nickName: '묵직한 바흐',
    });

    expect(result).toEqual(mockPostResponse);
    expect(fetch).toHaveBeenCalledWith(
      expect.stringContaining('/api/posts'),
      expect.objectContaining({ method: 'POST' })
    );
  });

  it('요청 실패 시 에러를 던져야 한다', async () => {
    vi.spyOn(global, 'fetch').mockResolvedValue({
      ok: false,
      status: 400,
    } as Response);

    await expect(
      createPost({
        title: '',
        content: '',
        tag: 'VOID',
        userId: 12345,
        nickName: '묵직한 바흐',
      })
    ).rejects.toThrow('API 요청 실패: 400');
  });
});

describe('getPosts', () => {
  it('게시글 목록을 페이징하여 조회해야 한다', async () => {
    vi.spyOn(global, 'fetch').mockResolvedValue({
      ok: true,
      json: () => Promise.resolve(mockPageResponse),
    } as Response);

    const result = await getPosts();

    expect(result.content).toHaveLength(1);
    expect(result.content[0].title).toBe('테스트');
    expect(fetch).toHaveBeenCalledWith(
      expect.stringContaining('/api/posts?page=0&size=20'),
      expect.any(Object)
    );
  });

  it('페이지와 사이즈를 지정하여 조회할 수 있어야 한다', async () => {
    vi.spyOn(global, 'fetch').mockResolvedValue({
      ok: true,
      json: () => Promise.resolve(mockPageResponse),
    } as Response);

    await getPosts(2, 10);

    expect(fetch).toHaveBeenCalledWith(
      expect.stringContaining('/api/posts?page=2&size=10'),
      expect.any(Object)
    );
  });
});
