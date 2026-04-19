'use client';

import {
  MessageSquare,
  Eye,
  TriangleAlert,
  ChevronUp,
  ChevronDown,
  Bookmark,
  Pencil,
  Trash2,
} from 'lucide-react';
import { formatDate } from '@/lib/utils/formatDate';
import { useRouter } from 'next/navigation';
import { useUserStore } from '@/lib/stores/userStore';
import { TAG_STYLES } from '@/lib/utils/tagColors';
import { deletePost } from '@/lib/api/posts';

interface PostCardProps {
  id: string | number;
  title?: string;
  content: string;
  tag?: string;
  timestamp: string;
  username: string;
  replies?: number;
  views?: number;
  votes?: number;
  voteRatio?: number;
  /** 현재 로그인 사용자가 작성한 글이면 상세와 동일하게 수정·삭제 진입 */
  isOwner?: boolean;
  /** 카드에서 삭제 성공 시 목록 새로고침 */
  onDeleted?: () => void;
}

export default function PostCard({
  id,
  title,
  content,
  tag,
  timestamp,
  username,
  replies = 0,
  views = 0,
  votes = 0,
  voteRatio = 0,
  isOwner = false,
  onDeleted,
}: PostCardProps) {
  const router = useRouter();
  const snowflakeId = useUserStore((s) => s.snowflakeId);
  const isLoggedIn = useUserStore(
    (s) => s.nickname !== null && s.snowflakeId !== null
  );

  const goToEdit = (e: React.MouseEvent) => {
    e.stopPropagation();
    router.push(`/posts/${id}`);
  };

  const handleDeleteCard = async (e: React.MouseEvent) => {
    e.stopPropagation();
    if (!isOwner || !onDeleted || snowflakeId == null) return;
    if (!window.confirm('이 게시글을 삭제할까요?')) return;
    try {
      await deletePost(String(id), Number(snowflakeId));
      onDeleted();
    } catch (err) {
      console.error('게시글 삭제 실패:', err);
    }
  };

  return (
    <article
      className="w-full cursor-pointer rounded-lg border border-border bg-card transition-colors hover:bg-muted/50"
      onClick={() => router.push(`/posts/${id}`)}
    >
      <div className="flex gap-0 p-4">
        {/* 투표 영역 */}
        <div className="flex min-w-[48px] flex-col items-center gap-0.5 pr-4">
          <button
            type="button"
            className="rounded p-1 text-muted-foreground hover:text-foreground"
            aria-label="추천"
            onClick={(e) => e.stopPropagation()}
          >
            <ChevronUp className="h-5 w-5" />
          </button>
          <span className="text-lg font-bold text-foreground">{votes}</span>
          <button
            type="button"
            className="rounded p-1 text-muted-foreground hover:text-foreground"
            aria-label="비추천"
            onClick={(e) => e.stopPropagation()}
          >
            <ChevronDown className="h-5 w-5" />
          </button>
        </div>

        {/* 콘텐츠 영역 */}
        <div className="min-w-0 flex-1">
          {/* 메타 정보 */}
          <div className="flex flex-wrap items-center gap-2 text-xs text-muted-foreground">
            {tag && (
              <span
                className={`rounded px-2 py-0.5 text-xs font-medium ${TAG_STYLES[tag] ?? 'bg-gray-500 text-white'}`}
              >
                {tag}
              </span>
            )}
            <span className="font-medium">{username}</span>
            <span>✕ {views.toLocaleString()}</span>
            <span className="text-rose-500">
              ▲ {voteRatio}%
            </span>
            <span>· {formatDate(timestamp)}</span>
          </div>

          {/* 제목 */}
          {title && (
            <h3 className="mt-1.5 line-clamp-1 text-base font-bold text-foreground">
              {title}
            </h3>
          )}

          {/* 본문 미리보기 */}
          <p className="mt-1 line-clamp-1 text-sm leading-relaxed text-muted-foreground">
            <span className="mr-1 text-xs">▼</span>
            {content}
          </p>

          {/* 하단 액션 */}
          <div className="mt-3 flex items-center gap-4 border-t border-border pt-2 text-xs text-muted-foreground">
            <span className="flex items-center gap-1.5">
              <MessageSquare className="h-3.5 w-3.5" />
              {replies}
            </span>
            <span className="flex items-center gap-1.5">
              <Eye className="h-3.5 w-3.5" />
              {views}
            </span>
            {isLoggedIn && (
              <>
                <button
                  type="button"
                  className="flex items-center gap-1.5 hover:text-foreground"
                  onClick={(e) => e.stopPropagation()}
                >
                  <Bookmark className="h-3.5 w-3.5" />
                  북마크
                </button>
                {isOwner && (
                  <>
                    <button
                      type="button"
                      className="flex items-center gap-1.5 hover:text-foreground"
                      onClick={goToEdit}
                    >
                      <Pencil className="h-3.5 w-3.5" />
                      수정
                    </button>
                    {onDeleted ? (
                      <button
                        type="button"
                        className="flex items-center gap-1.5 hover:text-destructive"
                        onClick={handleDeleteCard}
                      >
                        <Trash2 className="h-3.5 w-3.5" />
                        삭제
                      </button>
                    ) : null}
                  </>
                )}
                {!isOwner && (
                  <button
                    type="button"
                    className="ml-auto flex items-center gap-1.5 hover:text-destructive"
                    aria-label="신고"
                    onClick={(e) => e.stopPropagation()}
                  >
                    <TriangleAlert className="h-3.5 w-3.5" />
                    신고
                  </button>
                )}
              </>
            )}
          </div>
        </div>
      </div>
    </article>
  );
}
