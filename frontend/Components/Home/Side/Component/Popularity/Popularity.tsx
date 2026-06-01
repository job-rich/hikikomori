'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { ChevronUp, Flame } from 'lucide-react';
import { getPosts, type PostResponse } from '@/lib/api/posts';

export default function Popularity() {
  const router = useRouter();
  const [posts, setPosts] = useState<PostResponse[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    getPosts(0, 5, 'likeCount,desc')
      .then((data) => setPosts(data.content))
      .catch(() => setPosts([]))
      .finally(() => setLoading(false));
  }, []);

  return (
    <section className="rounded-lg border border-border bg-card p-4">
      <h2 className="mb-3 flex items-center gap-1.5 text-sm font-bold text-foreground">
        <Flame className="h-4 w-4 text-orange-500" />
        인기 게시글
      </h2>

      {loading ? (
        <ul className="flex flex-col gap-2">
          {Array.from({ length: 5 }).map((_, i) => (
            <li key={i} className="h-10 animate-pulse rounded bg-muted" />
          ))}
        </ul>
      ) : posts.length === 0 ? (
        <p className="text-xs text-muted-foreground">게시글이 없습니다.</p>
      ) : (
        <ol className="flex flex-col gap-2">
          {posts.map((post, idx) => (
            <li key={post.id}>
              <button
                type="button"
                className="flex w-full items-start gap-2 rounded p-1 text-left transition-colors hover:bg-muted/60"
                onClick={() => router.push(`/posts/${post.id}`)}
              >
                <span
                  className={`mt-0.5 min-w-[18px] text-center text-xs font-bold ${
                    idx === 0
                      ? 'text-orange-500'
                      : idx === 1
                        ? 'text-rose-400'
                        : idx === 2
                          ? 'text-amber-400'
                          : 'text-muted-foreground'
                  }`}
                >
                  {idx + 1}
                </span>
                <span className="min-w-0 flex-1">
                  <span className="line-clamp-1 text-xs font-medium text-foreground">
                    {post.title || post.content}
                  </span>
                  <span className="mt-0.5 flex items-center gap-1 text-[10px] text-muted-foreground">
                    <ChevronUp className="h-3 w-3 text-rose-400" />
                    {post.likeCount}
                    <span className="ml-1">{post.nickName}</span>
                  </span>
                </span>
              </button>
            </li>
          ))}
        </ol>
      )}
    </section>
  );
}
