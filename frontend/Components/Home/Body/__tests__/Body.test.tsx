import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { render, screen, fireEvent, cleanup } from '@testing-library/react';
import Body from '@/Components/Home/Body/Body';
import { useUserStore } from '@/lib/stores/userStore';
import * as postsApi from '@/lib/api/posts';

vi.mock('@/lib/api/posts');

const mockPosts = [
  {
    id: '1',
    userId: 12345,
    nickName: '유저1',
    title: '전체 게시글',
    content: '내용1',
    tag: 'VOID',
    createdAt: '2026-03-01T08:00:00',
  },
  {
    id: '2',
    userId: 99999,
    nickName: '유저2',
    title: '다른 게시글',
    content: '내용2',
    tag: 'VOID',
    createdAt: '2026-03-01T09:00:00',
  },
];

const mockMyPosts = [
  {
    id: '1',
    userId: 12345,
    nickName: '유저1',
    title: '내 게시글',
    content: '내용1',
    tag: 'VOID',
    createdAt: '2026-03-01T08:00:00',
  },
];

const mockPageResponse = {
  content: mockPosts,
  totalElements: 2,
  totalPages: 1,
  number: 0,
  size: 6,
};

const mockMyPageResponse = {
  content: mockMyPosts,
  totalElements: 1,
  totalPages: 1,
  number: 0,
  size: 6,
};

let intersectionCallback: IntersectionObserverCallback;
const mockObserve = vi.fn();
const mockDisconnect = vi.fn();

beforeEach(() => {
  vi.mocked(postsApi.getPosts).mockResolvedValue(mockPageResponse);
  vi.mocked(postsApi.getMyPosts).mockResolvedValue(mockMyPageResponse);
  useUserStore.setState({
    nickname: '유저1',
    snowflakeId: '12345',
    nicknameModalOpen: false,
  });

  const MockIntersectionObserver = vi.fn(
    function (this: unknown, callback: IntersectionObserverCallback) {
      intersectionCallback = callback;
      return {
        observe: mockObserve,
        disconnect: mockDisconnect,
        unobserve: vi.fn(),
      };
    }
  );
  vi.stubGlobal('IntersectionObserver', MockIntersectionObserver);
});

afterEach(() => {
  cleanup();
  vi.restoreAllMocks();
});

describe('Body - 내가 쓴 게시글 기능', () => {
  it('기본 상태에서 "전체 게시글" 탭이 활성화되어야 한다', async () => {
    render(<Body />);

    const allTab = await screen.findByRole('button', { name: '전체 게시글' });
    expect(allTab).toBeInTheDocument();
    expect(
      screen.getByRole('button', { name: '내가 쓴 글' })
    ).toBeInTheDocument();
  });

  it('"내가 쓴 글" 버튼 클릭 시 내 게시글만 표시되어야 한다', async () => {
    render(<Body />);

    await screen.findByText('전체 게시글');
    fireEvent.click(screen.getByRole('button', { name: '내가 쓴 글' }));

    expect(postsApi.getMyPosts).toHaveBeenCalledWith(12345, 0);
  });

  it('"내가 쓴 글" 모드에서 PostForm이 표시되지 않아야 한다', async () => {
    render(<Body />);

    await screen.findByText('전체 게시글');

    expect(
      screen.getByPlaceholderText('제목을 입력하세요...')
    ).toBeInTheDocument();

    fireEvent.click(screen.getByRole('button', { name: '내가 쓴 글' }));

    expect(
      screen.queryByPlaceholderText('제목을 입력하세요...')
    ).not.toBeInTheDocument();
  });

  it('"전체 게시글" 탭으로 돌아오면 PostForm이 다시 표시되어야 한다', async () => {
    render(<Body />);

    await screen.findByText('전체 게시글');

    fireEvent.click(screen.getByRole('button', { name: '내가 쓴 글' }));
    fireEvent.click(screen.getByRole('button', { name: '전체 게시글' }));

    expect(
      screen.getByPlaceholderText('제목을 입력하세요...')
    ).toBeInTheDocument();
  });

  it('내 게시글이 없으면 빈 상태 안내가 표시되어야 한다', async () => {
    vi.mocked(postsApi.getMyPosts).mockResolvedValue({
      content: [],
      totalElements: 0,
      totalPages: 0,
      number: 0,
      size: 6,
    });

    render(<Body />);

    await screen.findByText('전체 게시글');
    fireEvent.click(screen.getByRole('button', { name: '내가 쓴 글' }));

    expect(
      await screen.findByText('작성한 게시글이 없습니다.')
    ).toBeInTheDocument();
  });
});

describe('Body - 무한 스크롤', () => {
  it('sentinel 요소가 렌더링되어야 한다', async () => {
    render(<Body />);

    await screen.findByText('전체 게시글');
    expect(screen.getByTestId('scroll-sentinel')).toBeInTheDocument();
  });

  it('IntersectionObserver가 sentinel을 관찰해야 한다', async () => {
    render(<Body />);

    await screen.findByText('전체 게시글');
    expect(mockObserve).toHaveBeenCalled();
  });

  it('sentinel이 뷰포트에 들어오면 다음 페이지를 로드해야 한다', async () => {
    const page0Response = {
      content: mockPosts,
      totalElements: 8,
      totalPages: 2,
      number: 0,
      size: 6,
    };

    const page1Posts = [
      {
        id: '3',
        userId: 12345,
        nickName: '유저3',
        title: '추가 게시글',
        content: '내용3',
        tag: 'VOID',
        createdAt: '2026-03-01T10:00:00',
      },
    ];

    const page1Response = {
      content: page1Posts,
      totalElements: 8,
      totalPages: 2,
      number: 1,
      size: 6,
    };

    vi.mocked(postsApi.getPosts)
      .mockResolvedValueOnce(page0Response)
      .mockResolvedValueOnce(page1Response);

    render(<Body />);

    await screen.findByText('전체 게시글');

    intersectionCallback(
      [{ isIntersecting: true }] as IntersectionObserverEntry[],
      {} as IntersectionObserver
    );

    expect(await screen.findByText('추가 게시글')).toBeInTheDocument();
    expect(screen.getByText('다른 게시글')).toBeInTheDocument();
  });

  it('마지막 페이지면 추가 로드하지 않아야 한다', async () => {
    render(<Body />);

    await screen.findByText('전체 게시글');

    vi.mocked(postsApi.getPosts).mockClear();

    intersectionCallback(
      [{ isIntersecting: true }] as IntersectionObserverEntry[],
      {} as IntersectionObserver
    );

    expect(postsApi.getPosts).not.toHaveBeenCalled();
  });

  it('viewMode 전환 시 page가 0으로 리셋되어야 한다', async () => {
    render(<Body />);

    await screen.findByText('전체 게시글');

    fireEvent.click(screen.getByRole('button', { name: '내가 쓴 글' }));

    expect(postsApi.getMyPosts).toHaveBeenCalledWith(12345, 0);
  });
});
