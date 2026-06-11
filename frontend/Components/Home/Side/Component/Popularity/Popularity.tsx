'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { Heart, Trophy } from 'lucide-react';
import { getPosts, type PostResponse } from '@/lib/api/posts';

export default function Popularity() {
  const router = useRouter();
  const [posts, setPosts] = useState<PostResponse[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    getPosts(0, 5, 'likeCount,desc')
      .then((data) => {
        const seen = new Set<string>();
        setPosts(
          data.content.filter((post) => {
            if (seen.has(post.id)) return false;
            seen.add(post.id);
            return true;
          })
        );
      })
      .catch(() => setPosts([]))
      .finally(() => setLoading(false));
  }, []);

  return (
    <section className="rounded-lg border border-border bg-card p-4">
      <h2 className="mb-3 flex items-center gap-1.5 text-sm font-bold text-foreground">
        <Trophy className="h-4 w-4 text-amber-400" />
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
                  className={`mt-0.5 min-w-[18px] text-center text-md font-bold text-[#F31260]`}
                >
                  {idx + 1}
                </span>
                <span className="min-w-0 flex-1">
                  <span className="line-clamp-1 text-xs font-medium text-foreground">
                    {post.title || post.content}
                  </span>
                  <span className="mt-0.5 flex items-center gap-2 text-[10px] text-muted-foreground">
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
