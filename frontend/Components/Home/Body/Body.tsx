'use client';

import { useCallback, useEffect, useRef, useState } from 'react';
import PostForm from '@/Components/Common/Post/Post-Form/Post-Form';
import PostCard from '@/Components/Common/Post/Post-Card/Post-Card';
import {
  createPost,
  getPosts,
  getMyPosts,
  type PostResponse,
} from '@/lib/api/posts';
import { isApiError } from '@/lib/api/client';
import { useUserStore } from '@/lib/stores/userStore';
import { usePostsStore } from '@/lib/stores/postsStore';
import { isEmpty } from '@/lib/utils/isEmpty';
import './body.css';
import FillterTab from './component/fillterTab/FillterTab';
import PostTab from './component/postTab/PostTab';

type ViewMode = 'all' | 'my';

type SortTab = 'latest' | 'votes' | 'comments';

export default function Body() {
  const { snowflakeId, nickname, openNicknameModal } = useUserStore();
  const setStorePosts = usePostsStore((s) => s.setPosts);
  const isLoggedIn = useUserStore(
    (s) => s.nickname !== null && s.snowflakeId !== null
  );
  const [posts, setPosts] = useState<PostResponse[]>([]);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [sortTab, setSortTab] = useState<SortTab>('latest');
  const [viewMode, setViewMode] = useState<ViewMode>('all');
  const [isLoading, setIsLoading] = useState(false);
  const [hasMore, setHasMore] = useState(true);
  const pageRef = useRef(0);
  const hasMoreRef = useRef(true);
  const isLoadingRef = useRef(false);
  const sortTabRef = useRef<SortTab>(sortTab);
  sortTabRef.current = sortTab;
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
        const sortParam =
          sortTabRef.current === 'latest'
            ? 'createdAt,desc'
            : sortTabRef.current === 'comments'
              ? 'commentCount,desc'
              : sortTabRef.current === 'votes'
                ? 'likeCount,desc'
                : undefined;

        const data =
          mode === 'my' && !isEmpty(snowflakeId)
            ? await getMyPosts(Number(snowflakeId!), pageNum, 6, sortParam)
            : await getPosts(pageNum, 6, sortParam);

        if (append) {
          setPosts((prev) => {
            const seen = new Set(prev.map((p) => p.id));
            return [...prev, ...data.content.filter((p) => !seen.has(p.id))];
          });
        } else {
          setPosts(data.content);
        }
        const more = pageNum + 1 < data.totalPages;
        hasMoreRef.current = more;
        setHasMore(more);
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
      setHasMore(true);
      setPosts([]);
      fetchPosts(mode, 0, false);
    },
    [fetchPosts]
  );

  useEffect(() => {
    setStorePosts(posts);
  }, [posts, setStorePosts]);

  useEffect(() => {
    resetAndFetch(viewMode);
  }, [viewMode, sortTab, resetAndFetch]);

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
    } catch (err) {
      if (isApiError(err, 403)) {
        window.alert('신고 누적으로 작성이 제한되었습니다.');
      } else {
        console.error('게시글 생성 실패:', err);
      }
    } finally {
      setIsSubmitting(false);
    }
  };

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
        <div className="mt-4 flex w-full flex-col gap-5">
          {posts.length === 0 && !isLoading ? (
            <p className="w-full py-10 text-center text-sm text-muted-foreground">
              {viewMode === 'my'
                ? '작성한 게시글이 없습니다.'
                : '게시글이 없습니다.'}
            </p>
          ) : (
            posts.map((post) => (
              <PostCard
                key={post.id}
                id={post.id}
                title={post.title}
                content={post.content}
                tag={post.tag}
                timestamp={post.createdAt}
                username={post.nickName}
                commentCount={post.commentCount}
                views={post.viewCount}
                likeCount={post.likeCount}
                isOwner={!!snowflakeId && post.userId === Number(snowflakeId)}
                authorUserId={post.userId}
                onDeleted={() => resetAndFetch(viewMode)}
              />
            ))
          )}
        </div>

        {/* 더 불러오기 인디케이터 */}
        {posts.length > 0 && (
          <p className="w-full py-6 text-center text-xs text-muted-foreground">
            {isLoading
              ? '불러오는 중...'
              : hasMore
                ? '스크롤하여 더 불러오기...'
                : '모든 게시글을 불러왔습니다.'}
          </p>
        )}

        <div ref={sentinelRef} data-testid="scroll-sentinel" />
      </div>
    </main>
  );
}
