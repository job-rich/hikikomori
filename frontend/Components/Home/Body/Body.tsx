'use client';

import { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import PostForm from '@/Components/Common/Post/Post-Form/Post-Form';
import PostCard from '@/Components/Common/Post/Post-Card/Post-Card';
import {
  createPost,
  getPosts,
  getMyPosts,
  type PostResponse,
} from '@/lib/api/posts';
import { useUserStore } from '@/lib/stores/userStore';
import { isEmpty } from '@/lib/utils/isEmpty';
import './body.css';
import FillterTab from './component/fillterTab/FillterTab';
import PostTab from './component/postTab/PostTab';

type ViewMode = 'all' | 'my';

type SortTab = 'latest' | 'votes' | 'comments';

export default function Body() {
  const { snowflakeId, nickname, openNicknameModal } = useUserStore();
  const isLoggedIn = useUserStore(
    (s) => s.nickname !== null && s.snowflakeId !== null
  );
  const [posts, setPosts] = useState<PostResponse[]>([]);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [sortTab, setSortTab] = useState<SortTab>('latest');
  const [viewMode, setViewMode] = useState<ViewMode>('all');
  const [isLoading, setIsLoading] = useState(false);
  const pageRef = useRef(0);
  const hasMoreRef = useRef(true);
  const isLoadingRef = useRef(false);
  const sentinelRef = useRef<HTMLDivElement>(null);
  const observerRef = useRef<IntersectionObserver | null>(null);

  const reobserveSentinel = useCallback(() => {
    const observer = observerRef.current;
    const sentinel = sentinelRef.current;
    if (observer && sentinel) {
      observer.unobserve(sentinel);
      observer.observe(sentinel);
    }
  }, []);

  const fetchPosts = useCallback(
    async (mode: ViewMode, pageNum: number, append: boolean) => {
      if (isLoadingRef.current) return;
      isLoadingRef.current = true;
      setIsLoading(true);
      try {
        const data =
          mode === 'my' && !isEmpty(snowflakeId)
            ? await getMyPosts(Number(snowflakeId!), pageNum)
            : await getPosts(pageNum);

        if (append) {
          setPosts((prev) => [...prev, ...data.content]);
        } else {
          setPosts(data.content);
        }
        hasMoreRef.current = pageNum + 1 < data.totalPages;
      } catch (error) {
        console.error(
          mode === 'my' ? '내 게시글 조회 실패:' : '게시글 목록 조회 실패:',
          error
        );
      } finally {
        isLoadingRef.current = false;
        setIsLoading(false);
        if (hasMoreRef.current) {
          requestAnimationFrame(reobserveSentinel);
        }
      }
    },
    [snowflakeId, reobserveSentinel]
  );

  const resetAndFetch = useCallback(
    (mode: ViewMode) => {
      pageRef.current = 0;
      hasMoreRef.current = true;
      setPosts([]);
      fetchPosts(mode, 0, false);
    },
    [fetchPosts]
  );

  useEffect(() => {
    resetAndFetch(viewMode);
  }, [viewMode, resetAndFetch]);

  useEffect(() => {
    const sentinel = sentinelRef.current;
    if (!sentinel) return;

    const observer = new IntersectionObserver(
      (entries) => {
        if (
          entries[0].isIntersecting &&
          hasMoreRef.current &&
          !isLoadingRef.current
        ) {
          const nextPage = pageRef.current + 1;
          pageRef.current = nextPage;
          fetchPosts(viewMode, nextPage, true);
        }
      },
      { threshold: 0.1 }
    );

    observerRef.current = observer;
    observer.observe(sentinel);
    return () => {
      observer.disconnect();
      observerRef.current = null;
    };
  }, [viewMode, fetchPosts]);

  const handleSubmit = async (title: string, content: string, tag: string) => {
    if (isEmpty(snowflakeId) || isEmpty(nickname) || isSubmitting) return;

    setIsSubmitting(true);
    try {
      await createPost({
        title,
        content,
        tag,
        userId: Number(snowflakeId!),
        nickName: nickname!,
      });
      resetAndFetch(viewMode);
    } catch (error) {
      console.error('게시글 생성 실패:', error);
    } finally {
      setIsSubmitting(false);
    }
  };

  const sortedPosts = useMemo(() => {
    if (sortTab === 'latest') {
      return [...posts].sort(
        (a, b) =>
          new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
      );
    }

    if (sortTab === 'comments') {
      return [...posts].sort((a, b) => b.commentCount - a.commentCount);
    }

    return posts;
  }, [posts, sortTab]);

  return (
    <main className="min-w-0 flex-1 font-sans">
      <div className="mx-auto flex max-w-3xl flex-col items-start">
        {/* 메인 탭 */}
        <PostTab viewMode={viewMode} setViewMode={setViewMode} />

        {/* 글 작성 영역 또는 닉네임 생성 배너 */}
        <div className="mt-4 w-full">
          {isLoggedIn && viewMode === 'all' ? (
            <PostForm onSubmit={handleSubmit} />
          ) : !isLoggedIn ? (
            <div className="flex flex-col items-center gap-4 rounded-lg border border-border border-t-rose-400 bg-card p-8">
              <p className="text-sm text-muted-foreground">
                글을 작성하려면 닉네임을 생성하세요
              </p>
              <button
                type="button"
                onClick={openNicknameModal}
                className="rounded-full bg-accent px-6 py-2 text-sm font-medium text-accent-foreground transition-colors hover:bg-accent/90"
              >
                닉네임 생성
              </button>
            </div>
          ) : null}
        </div>

        {/* 정렬 탭 */}
        <FillterTab sortTab={sortTab} setSortTab={setSortTab} />

        {/* 게시글 목록 */}
        <div className="mt-4 flex w-full flex-col gap-3">
          {posts.length === 0 && !isLoading ? (
            <p className="w-full py-10 text-center text-sm text-muted-foreground">
              {viewMode === 'my'
                ? '작성한 게시글이 없습니다.'
                : '게시글이 없습니다.'}
            </p>
          ) : posts.length > 0 ? (
            sortedPosts.map((post) => (
              <PostCard
                key={post.id}
                id={post.id}
                title={post.title}
                content={post.content}
                tag={post.tag}
                timestamp={post.createdAt}
                username={post.nickName}
                commentCount={post.commentCount}
                isOwner={!!snowflakeId && post.userId === Number(snowflakeId)}
                onDeleted={() => resetAndFetch(viewMode)}
              />
            ))
          ) : null}
        </div>

        {/* 더 불러오기 인디케이터 */}
        {posts.length > 0 && (
          <p className="w-full py-6 text-center text-xs text-muted-foreground">
            스크롤하여 더 불러오기...
          </p>
        )}

        <div ref={sentinelRef} data-testid="scroll-sentinel" />
      </div>
    </main>
  );
}
