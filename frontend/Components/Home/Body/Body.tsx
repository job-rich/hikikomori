'use client';

import { useCallback, useEffect, useState } from 'react';
import PostForm from '@/Components/Common/Post/Post-Form/Post-Form';
import PostCard from '@/Components/Common/Post/Post-Card/Post-Card';
import { createPost, getPosts, type PostResponse } from '@/lib/api/posts';
import { useUserStore } from '@/lib/stores/userStore';
import { isEmpty } from '@/lib/utils/isEmpty';

export default function Body() {
  const { snowflakeId, nickname } = useUserStore();
  const [posts, setPosts] = useState<PostResponse[]>([]);
  const [isSubmitting, setIsSubmitting] = useState(false);

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

  return (
    <main className="min-w-0 flex-1 font-sans">
      <div className="mx-auto flex max-w-3xl flex-col items-start">
        <PostForm onSubmit={handleSubmit} />
        {posts.length === 0 ? (
          <p className="w-full mt-4 text-center text-sm text-muted-foreground py-10">
            게시글이 없습니다.
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
      </div>
    </main>
  );
}
