'use client';

import {
  MessageSquare,
  Eye,
  Heart,
  TriangleAlert,
  Bookmark,
  Pencil,
  Trash2,
} from 'lucide-react';
import { formatDate } from '@/lib/utils/formatDate';
import { useRouter } from 'next/navigation';
import { useState } from 'react';
import { useUserStore } from '@/lib/stores/userStore';
import { TAG_STYLES } from '@/lib/utils/tagColors';
import { deletePost } from '@/lib/api/posts';
import ReportModal from '@/Components/Common/Modals/Report';

interface PostCardProps {
  id: string | number;
  title?: string;
  content: string;
  tag?: string;
  timestamp: string;
  username: string;
  commentCount?: number;
  views?: number;
  likeCount?: number;
  /** 현재 로그인 사용자가 작성한 글이면 상세와 동일하게 수정·삭제 진입 */
  isOwner?: boolean;
  /** 카드에서 삭제 성공 시 목록 새로고침 */
  onDeleted?: () => void;
  /** 게시글 작성자 userId (신고 대상) */
  authorUserId?: number;
}

export default function PostCard({
  id,
  title,
  content,
  tag,
  timestamp,
  username,
  commentCount,
  views = 0,
  likeCount = 0,
  isOwner = false,
  onDeleted,
  authorUserId,
}: PostCardProps) {
  const router = useRouter();
  const [reportOpen, setReportOpen] = useState(false);
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
    <>
      <article
        className="w-full cursor-pointer rounded-lg border border-border bg-card transition-colors hover:bg-muted/50"
        onClick={() => router.push(`/posts/${id}`)}
      >
        <div className="p-4">
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
            <span>· {formatDate(timestamp)}</span>
            {!isOwner && (
              <button
                type="button"
                className="ml-auto flex items-center gap-1.5 hover:text-destructive"
                aria-label="신고"
                onClick={(e) => {
                  e.stopPropagation();
                  setReportOpen(true);
                }}
              >
                <TriangleAlert className="h-3.5 w-3.5" />
                신고
              </button>
            )}
          </div>

          {/* 제목 */}
          {title && (
            <h3 className="mt-1.5 line-clamp-1 text-base font-bold text-foreground">
              {title}
            </h3>
          )}

          {/* 본문 미리보기 */}
          <p className="mt-1 line-clamp-1 text-sm leading-relaxed text-muted-foreground">
            {content}
          </p>

          {/* 하단 액션 */}
          <div className="mt-5 flex border-t border-border pt-2 items-end justify-end gap-4  text-xs text-muted-foreground">
            <span className="flex items-center gap-1.5">
              <Heart className="h-3.5 w-3.5" />
              {likeCount}
            </span>
            <span className="flex items-center gap-1.5">
              <MessageSquare className="h-3.5 w-3.5" />
              {commentCount}
            </span>
            <span className="flex items-center gap-1.5">
              <Eye className="h-3.5 w-3.5" />
              {views.toLocaleString()}
            </span>
            {isLoggedIn && (
              <>
                {/* <button
                  type="button"
                  className="flex items-center gap-1.5 hover:text-foreground"
                  onClick={(e) => e.stopPropagation()}
                >
                  <Bookmark className="h-3.5 w-3.5" />
                  북마크
                </button> */}
              </>
            )}
          </div>
        </div>
      </article>
      {reportOpen && authorUserId != null && (
        <ReportModal
          targetType="POST"
          targetId={String(id)}
          targetUserId={authorUserId}
          onClose={() => setReportOpen(false)}
        />
      )}
    </>
  );
}
