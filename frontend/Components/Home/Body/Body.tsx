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
import { useUserStore } from '@/lib/stores/userStore';
import { isEmpty } from '@/lib/utils/isEmpty';
import './body.css';

type ViewMode = 'all' | 'my';

export default function Body() {
  const { snowflakeId, nickname } = useUserStore();
  const [posts, setPosts] = useState<PostResponse[]>([]);
  const [isSubmitting, setIsSubmitting] = useState(false);
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

  return (
    <main className="min-w-0 flex-1 font-sans">
      <div className="mx-auto flex max-w-3xl flex-col items-start">
        <div className="body-tab-group">
          <button
            type="button"
            className={`body-tab ${viewMode === 'all' ? 'body-tab--active' : ''}`}
            onClick={() => setViewMode('all')}
          >
            전체 게시글
          </button>
          <button
            type="button"
            className={`body-tab ${viewMode === 'my' ? 'body-tab--active' : ''}`}
            onClick={() => setViewMode('my')}
          >
            내가 쓴 글
          </button>
        </div>

        {viewMode === 'all' && <PostForm onSubmit={handleSubmit} />}

        {posts.length === 0 && !isLoading ? (
          <p className="w-full mt-4 text-center text-sm text-muted-foreground py-10">
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
            />
          ))
        )}

        <div ref={sentinelRef} data-testid="scroll-sentinel" />
      </div>
    </main>
  );
}
