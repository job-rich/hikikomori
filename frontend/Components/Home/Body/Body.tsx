'use client';

import { useState } from 'react';
import PostForm from '@/Components/Common/Post/Post-Form/Post-Form';
import PostCard from '@/Components/Common/Post/Post-Card/Post-Card';
import { createPost } from '@/lib/api/posts';
import { useUserStore } from '@/lib/stores/userStore';
import { isEmpty } from '@/lib/utils/isEmpty';

export default function Body() {
  const { snowflakeId, nickname } = useUserStore();
  const [isSubmitting, setIsSubmitting] = useState(false);

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
        <PostCard
          id={1}
          content="test"
          timestamp="test"
          replies={0}
          views={0}
          username="test"
          votes={0}
        />
      </div>
    </main>
  );
}
