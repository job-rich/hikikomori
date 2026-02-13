'use client';

import {
  MessageSquare,
  Eye,
  TriangleAlert,
  ArrowUp,
  ArrowDown,
} from 'lucide-react';

interface PostCardProps {
  id: number;
  content: string;
  timestamp: string;
  replies: number;
  views: number;
  username: string;
  votes: number;
}

export default function PostCard({
  content,
  timestamp,
  replies,
  views,
  username,
  votes,
}: PostCardProps) {
  return (
    <article className="border border-border w-full mt-10 rounded-md bg-card">
      <div className="flex p-4">
        <div className="flex flex-col items-center gap-1 pr-4 border-r border-border">
          <button
            type="button"
            className="p-1 text-muted-foreground hover:text-foreground"
            aria-label="추천"
          >
            <ArrowUp className="h-4 w-4" />
          </button>
          <span className="text-sm font-medium text-muted-foreground">
            {votes}
          </span>
          <button
            type="button"
            className="p-1 text-muted-foreground hover:text-foreground"
            aria-label="비추천"
          >
            <ArrowDown className="h-4 w-4" />
          </button>
        </div>

        <div className="flex-1 min-w-0 pl-4">
          <div className="flex items-center gap-2 text-xs text-muted-foreground flex-wrap">
            <span>{username}</span>
            <span>{timestamp}</span>
          </div>

          <p className="mt-2 text-sm text-foreground leading-relaxed">
            {content}
          </p>

          <div className="mt-3 flex items-center gap-4 text-xs text-muted-foreground pt-2 border-t border-border">
            <button
              type="button"
              className="flex items-center gap-1.5 hover:text-foreground"
            >
              <MessageSquare className="h-3.5 w-3.5" />
              <span>{replies}</span>
            </button>
            <div className="flex items-center gap-1.5">
              <Eye className="h-3.5 w-3.5" />
              <span>{views}</span>
            </div>
            <button
              type="button"
              className="flex items-center gap-1.5 hover:text-destructive ml-auto"
              aria-label="신고"
            >
              <TriangleAlert className="h-3.5 w-3.5" />
            </button>
          </div>
        </div>
      </div>
    </article>
  );
}
