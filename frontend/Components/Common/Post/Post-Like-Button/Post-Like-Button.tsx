'use client';

import { Heart } from 'lucide-react';
import { useEffect, useState } from 'react';
import { toggleLike } from '@/lib/api/posts';
import { useUserStore } from '@/lib/stores/userStore';

interface PostLikeButtonProps {
  postId: string;
  likeCount: number;
  likedByMe?: boolean;
  authorUserId?: number;
  onToggled?: (liked: boolean, likeCount: number) => void;
  className?: string;
  /** 상세 화면 등에서 더 크게 표시 */
  size?: 'sm' | 'lg';
}

export default function PostLikeButton({
  postId,
  likeCount,
  likedByMe = false,
  authorUserId,
  onToggled,
  className = '',
  size = 'sm',
}: PostLikeButtonProps) {
  const { snowflakeId, requireNickname } = useUserStore();
  const [liked, setLiked] = useState(likedByMe);
  const [count, setCount] = useState(likeCount);
  const [pending, setPending] = useState(false);

  useEffect(() => {
    setLiked(likedByMe);
  }, [likedByMe]);

  useEffect(() => {
    setCount(likeCount);
  }, [likeCount]);

  const isOwner =
    authorUserId != null &&
    snowflakeId != null &&
    String(authorUserId) === snowflakeId;

  const handleToggle = async (e: React.MouseEvent) => {
    e.stopPropagation();
    if (pending || isOwner || snowflakeId == null) return;
    if (requireNickname()) return;

    const prevLiked = liked;
    const prevCount = count;

    setPending(true);

    try {
      const result = await toggleLike(postId, snowflakeId);
      setLiked(result.liked);
      setCount(result.likeCount);
      onToggled?.(result.liked, result.likeCount);
    } catch (err) {
      console.error('좋아요 처리 실패:', err);
      setLiked(prevLiked);
      setCount(prevCount);
    } finally {
      setPending(false);
    }
  };

  const isLg = size === 'lg';

  return (
    <button
      type="button"
      className={`flex items-center transition-colors disabled:cursor-not-allowed disabled:opacity-50 ${
        isLg
          ? 'gap-2 rounded-lg border border-border px-4 py-2.5 text-sm font-medium'
          : 'gap-1.5 text-xs'
      } ${
        liked ? 'text-rose-500' : 'text-muted-foreground hover:text-rose-400'
      } ${isLg && liked ? 'border-rose-500/30 bg-rose-500/5' : ''} ${
        isLg && !liked ? 'hover:bg-muted/50' : ''
      } ${className}`}
      aria-label={liked ? '좋아요 취소' : '좋아요'}
      aria-pressed={liked}
      disabled={pending || isOwner}
      onClick={handleToggle}
    >
      <Heart
        className={`${isLg ? 'h-5 w-5' : 'h-3.5 w-3.5'} ${liked ? 'fill-current' : ''}`}
      />
      <span>{count}</span>
    </button>
  );
}
