'use client';

import Link from 'next/link';
import { usePostStore } from '@/lib/stores/postStore';
import { formatDate } from '@/lib/utils/formatDate';

interface PostDetailProps {
  postId: string;
}

export default function PostDetail({ postId }: PostDetailProps) {
  const getPostById = usePostStore((s) => s.getPostById);
  const post = getPostById(postId);

  if (!post) {
    return (
      <main className="min-w-0 flex-1 font-sans px-4 py-10">
        <div className="mx-auto max-w-3xl">
          <p className="text-sm text-muted-foreground mb-4">글이 없습니다.</p>
          <Link
            href="/"
            className="text-sm text-primary hover:underline"
          >
            목록으로
          </Link>
        </div>
      </main>
    );
  }

  return (
    <main className="min-w-0 flex-1 font-sans px-4 py-6">
      <div className="mx-auto max-w-3xl">
        <Link
          href="/"
          className="text-sm text-muted-foreground hover:text-foreground mb-4 inline-block"
        >
          ← 목록
        </Link>
        <article className="border border-border rounded-md bg-card p-6">
          <div className="flex items-center gap-2 text-xs text-muted-foreground flex-wrap">
            <span>{post.nickName}</span>
            {post.tag ? (
              <span className="px-1.5 py-0.5 rounded bg-muted">{post.tag}</span>
            ) : null}
            <span>{formatDate(post.createdAt)}</span>
          </div>
          <h1 className="mt-3 text-lg font-semibold text-foreground">
            {post.title}
          </h1>
          <p className="mt-3 text-sm text-foreground leading-relaxed whitespace-pre-wrap">
            {post.content}
          </p>
        </article>
      </div>
    </main>
  );
}
