'use client';

import { useCallback, useEffect, useState } from 'react';
import PostForm from '@/Components/Common/Post/Post-Form/Post-Form';
import PostCard from '@/Components/Common/Post/Post-Card/Post-Card';
import { createPost, getPosts, type PostResponse } from '@/lib/api/posts';
import { useUserStore } from '@/lib/stores/userStore';
import { isEmpty } from '@/lib/utils/isEmpty';

type MainTab = 'all' | 'mine';
type SortTab = 'latest' | 'votes' | 'comments';

export default function Body() {
  const { snowflakeId, nickname, openNicknameModal } = useUserStore();
  const isLoggedIn = useUserStore(
    (s) => s.nickname !== null && s.snowflakeId !== null
  );
  const [posts, setPosts] = useState<PostResponse[]>([]);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [mainTab, setMainTab] = useState<MainTab>('all');
  const [sortTab, setSortTab] = useState<SortTab>('latest');

  const fetchPosts = useCallback(async () => {
    try {
      const data = await getPosts();
      setPosts(data.content);
    } catch (error) {
      console.error('게시글 목록 조회 실패:', error);
    }
  }, []);

  useEffect(() => {
    fetchPosts();
  }, [fetchPosts]);

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
      await fetchPosts();
    } catch (error) {
      console.error('게시글 생성 실패:', error);
    } finally {
      setIsSubmitting(false);
    }
  };

  const filteredPosts =
    mainTab === 'mine' && snowflakeId
      ? posts.filter((p) => p.userId === Number(snowflakeId))
      : posts;

  return (
    <main className="min-w-0 flex-1 font-sans">
      <div className="mx-auto flex max-w-3xl flex-col items-start">
        {/* 메인 탭 */}
        <div className="flex w-full border-b border-border">
          <button
            type="button"
            onClick={() => setMainTab('all')}
            className={`px-4 py-2.5 text-sm font-medium transition-colors ${
              mainTab === 'all'
                ? 'border-b-2 border-foreground text-foreground'
                : 'text-muted-foreground hover:text-foreground'
            }`}
          >
            전체 게시글
          </button>
          {isLoggedIn && (
            <button
              type="button"
              onClick={() => setMainTab('mine')}
              className={`px-4 py-2.5 text-sm font-medium transition-colors ${
                mainTab === 'mine'
                  ? 'border-b-2 border-foreground text-foreground'
                  : 'text-muted-foreground hover:text-foreground'
              }`}
            >
              내가 쓴 글
            </button>
          )}
        </div>

        {/* 글 작성 영역 또는 닉네임 생성 배너 */}
        <div className="mt-4 w-full">
          {isLoggedIn ? (
            <PostForm onSubmit={handleSubmit} />
          ) : (
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
          )}
        </div>

        {/* 정렬 탭 */}
        <div className="mt-6 flex w-full items-center gap-1 text-sm">
          {(
            [
              { key: 'latest', label: '최신순' },
              { key: 'votes', label: '추천순' },
              { key: 'comments', label: '댓글순' },
            ] as const
          ).map(({ key, label }) => (
            <button
              key={key}
              type="button"
              onClick={() => setSortTab(key)}
              className={`px-3 py-1.5 transition-colors ${
                sortTab === key
                  ? 'border-b-2 border-foreground font-semibold text-foreground'
                  : 'text-muted-foreground hover:text-foreground'
              }`}
            >
              {label}
            </button>
          ))}
        </div>

        {/* 게시글 목록 */}
        <div className="mt-4 flex w-full flex-col gap-3">
          {filteredPosts.length === 0 ? (
            <p className="w-full py-10 text-center text-sm text-muted-foreground">
              게시글이 없습니다.
            </p>
          ) : (
            filteredPosts.map((post) => (
              <PostCard
                key={post.id}
                id={post.id}
                title={post.title}
                content={post.content}
                tag={post.tag}
                timestamp={post.createdAt}
                username={post.nickName}
                isOwner={
                  !!snowflakeId && post.userId === Number(snowflakeId)
                }
                onDeleted={fetchPosts}
              />
            ))
          )}
        </div>

        {/* 더 불러오기 인디케이터 */}
        {filteredPosts.length > 0 && (
          <p className="w-full py-6 text-center text-xs text-muted-foreground">
            스크롤하여 더 불러오기...
          </p>
        )}
      </div>
    </main>
  );
}
