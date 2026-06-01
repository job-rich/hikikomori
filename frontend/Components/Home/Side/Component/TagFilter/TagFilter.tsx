'use client';

import { usePostsStore } from '@/lib/stores/postsStore';
import { TAG_STYLES, TAG_COLORS, TAGS, type Tag } from '@/lib/utils/tagColors';
import { Tags } from 'lucide-react';

export default function TagFilter() {
  const posts = usePostsStore((s) => s.posts);

  const tagCounts = TAGS.reduce<Record<Tag, number>>(
    (acc, tag) => {
      acc[tag] = posts.filter((p) => p.tag === tag).length;
      return acc;
    },
    {} as Record<Tag, number>
  );

  return (
    <div className="rounded-lg border border-border bg-card p-4">
      <h3 className="text-sm font-bold mb-3 flex items-center gap-2">
        <Tags className="w-4 h-4 text-amber-400" />
        태그 필터
      </h3>
      <ul className="space-y-1.5 text-xs">
        {TAGS.map((tag) => (
          <li
            key={tag}
            className="flex justify-between items-center cursor-pointer hover:text-pink-400"
          >
            <span className="flex items-center gap-2">
              <span
                className={`${TAG_COLORS[tag]} bg-opacity-10 font-bold px-3 py-1 rounded-full text-xs`}
              >
                {tag}
              </span>
            </span>
            <span className="text-foreground">{tagCounts[tag]}</span>
          </li>
        ))}
      </ul>
    </div>
  );
}
