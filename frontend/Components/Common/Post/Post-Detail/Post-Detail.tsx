'use client';

import { useCallback, useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import {
  ArrowLeft,
  ArrowUp,
  ArrowDown,
  MessageSquare,
  Eye,
  TriangleAlert,
  Send,
  Reply,
} from 'lucide-react';
import { formatDate } from '@/lib/utils/formatDate';
import {
  getPost,
  getComments,
  createComment,
  type PostResponse,
  type CommentResponse,
} from '@/lib/api/posts';
import { useUserStore } from '@/lib/stores/userStore';
import { isEmpty } from '@/lib/utils/isEmpty';

interface PostDetailProps {
  postId: string;
}

function CommentItem({
  comment,
  postId,
  onCommentAdded,
  depth = 0,
}: {
  comment: CommentResponse;
  postId: string;
  onCommentAdded: () => void;
  depth?: number;
}) {
  const { snowflakeId, nickname, requireNickname } = useUserStore();
  const [replyOpen, setReplyOpen] = useState(false);
  const [replyContent, setReplyContent] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);

  const handleReply = async () => {
    if (!replyContent.trim() || isSubmitting) return;
    if (requireNickname()) return;
    if (isEmpty(snowflakeId) || isEmpty(nickname)) return;

    setIsSubmitting(true);
    try {
      await createComment(postId, {
        content: replyContent,
        parentId: comment.id,
        userId: Number(snowflakeId!),
        nickName: nickname!,
      });
      setReplyContent('');
      setReplyOpen(false);
      onCommentAdded();
    } catch (error) {
      console.error('답글 작성 실패:', error);
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className={depth > 0 ? 'ml-6 border-l border-border pl-4' : ''}>
      <div className="py-3">
        <div className="flex items-center gap-2 text-xs text-muted-foreground">
          <span className="font-medium text-foreground">
            {comment.nickName}
          </span>
          <span>{formatDate(comment.createdAt)}</span>
        </div>
        <p className="mt-1.5 text-sm text-foreground leading-relaxed">
          {comment.content}
        </p>
        {depth === 0 && (
          <button
            type="button"
            onClick={() => setReplyOpen(!replyOpen)}
            className="mt-1.5 flex items-center gap-1 text-xs text-muted-foreground hover:text-foreground"
          >
            <Reply className="h-3 w-3" />
            답글
          </button>
        )}
        {replyOpen && (
          <div className="mt-2 flex gap-2">
            <input
              type="text"
              value={replyContent}
              onChange={(e) => setReplyContent(e.target.value)}
              placeholder="답글을 입력하세요..."
              className="flex-1 rounded border border-border bg-transparent px-3 py-1.5 text-sm text-foreground placeholder:text-muted-foreground focus:outline-none"
              onKeyDown={(e) => {
                if (e.key === 'Enter') handleReply();
              }}
            />
            <button
              type="button"
              onClick={handleReply}
              disabled={!replyContent.trim() || isSubmitting}
              className="flex items-center gap-1 rounded border border-border px-2.5 py-1.5 text-xs hover:bg-muted disabled:opacity-50 disabled:cursor-not-allowed"
            >
              <Send className="h-3 w-3" />
            </button>
          </div>
        )}
      </div>
      {comment.children?.map((child) => (
        <CommentItem
          key={child.id}
          comment={child}
          postId={postId}
          onCommentAdded={onCommentAdded}
          depth={depth + 1}
        />
      ))}
    </div>
  );
}

export default function PostDetail({ postId }: PostDetailProps) {
  const router = useRouter();
  const { snowflakeId, nickname, requireNickname } = useUserStore();
  const [post, setPost] = useState<PostResponse | null>(null);
  const [comments, setComments] = useState<CommentResponse[]>([]);
  const [commentContent, setCommentContent] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const fetchComments = useCallback(async () => {
    try {
      const data = await getComments(postId);
      setComments(data);
    } catch (err) {
      console.error('댓글 조회 실패:', err);
    }
  }, [postId]);

  useEffect(() => {
    async function fetchPost() {
      try {
        const data = await getPost(postId);
        setPost(data);
      } catch {
        setError('게시글을 찾을 수 없습니다.');
      } finally {
        setLoading(false);
      }
    }
    fetchPost();
    fetchComments();
  }, [postId, fetchComments]);

  const handleCommentSubmit = async () => {
    if (!commentContent.trim() || isSubmitting) return;
    if (requireNickname()) return;
    if (isEmpty(snowflakeId) || isEmpty(nickname)) return;

    setIsSubmitting(true);
    try {
      await createComment(postId, {
        content: commentContent,
        userId: Number(snowflakeId!),
        nickName: nickname!,
      });
      setCommentContent('');
      await fetchComments();
    } catch (err) {
      console.error('댓글 작성 실패:', err);
    } finally {
      setIsSubmitting(false);
    }
  };

  if (loading) {
    return (
      <div className="flex min-h-screen items-center justify-center">
        <p className="text-sm text-muted-foreground">불러오는 중...</p>
      </div>
    );
  }

  if (error || !post) {
    return (
      <div className="flex min-h-screen flex-col items-center justify-center gap-4">
        <p className="text-sm text-muted-foreground">
          {error ?? '게시글을 찾을 수 없습니다.'}
        </p>
        <button
          type="button"
          onClick={() => router.back()}
          className="flex items-center gap-1.5 text-sm text-muted-foreground hover:text-foreground"
        >
          <ArrowLeft className="h-4 w-4" />
          돌아가기
        </button>
      </div>
    );
  }

  return (
    <div className="mx-auto max-w-3xl px-4 py-6">
      <button
        type="button"
        onClick={() => router.back()}
        className="mb-4 flex items-center gap-1.5 text-sm text-muted-foreground hover:text-foreground"
      >
        <ArrowLeft className="h-4 w-4" />
        뒤로가기
      </button>

      <article className="rounded-md border border-border bg-card">
        <div className="flex p-5">
          <div className="flex flex-col items-center gap-1 pr-5 border-r border-border">
            <button
              type="button"
              className="p-1 text-muted-foreground hover:text-foreground"
              aria-label="추천"
            >
              <ArrowUp className="h-5 w-5" />
            </button>
            <span className="text-sm font-medium text-muted-foreground">0</span>
            <button
              type="button"
              className="p-1 text-muted-foreground hover:text-foreground"
              aria-label="비추천"
            >
              <ArrowDown className="h-5 w-5" />
            </button>
          </div>

          <div className="flex-1 min-w-0 pl-5">
            <div className="flex items-center gap-2 text-xs text-muted-foreground flex-wrap">
              <span className="font-medium">{post.nickName}</span>
              {post.tag ? (
                <span className="px-1.5 py-0.5 rounded bg-muted">
                  {post.tag}
                </span>
              ) : null}
              <span>{formatDate(post.createdAt)}</span>
            </div>

            <h1 className="mt-2 text-lg font-bold text-foreground">
              {post.title}
            </h1>

            <div className="mt-4 text-sm text-foreground leading-relaxed whitespace-pre-wrap">
              {post.content}
            </div>

            <div className="mt-5 flex items-center gap-4 text-xs text-muted-foreground pt-3 border-t border-border">
              <div className="flex items-center gap-1.5">
                <MessageSquare className="h-3.5 w-3.5" />
                <span>{comments.length}</span>
              </div>
              <div className="flex items-center gap-1.5">
                <Eye className="h-3.5 w-3.5" />
                <span>0</span>
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

      <section className="mt-6">
        <h2 className="text-sm font-semibold text-foreground">
          댓글 {comments.length}개
        </h2>

        <div className="mt-3 flex gap-2">
          <input
            type="text"
            value={commentContent}
            onChange={(e) => setCommentContent(e.target.value)}
            placeholder="댓글을 입력하세요..."
            className="flex-1 rounded-md border border-border bg-transparent px-3 py-2 text-sm text-foreground placeholder:text-muted-foreground focus:outline-none"
            onKeyDown={(e) => {
              if (e.key === 'Enter' && (e.metaKey || e.ctrlKey)) {
                handleCommentSubmit();
              }
            }}
          />
          <button
            type="button"
            onClick={handleCommentSubmit}
            disabled={!commentContent.trim() || isSubmitting}
            className="flex items-center gap-1.5 rounded-md border border-border px-3 py-2 text-sm hover:bg-muted disabled:opacity-50 disabled:cursor-not-allowed"
          >
            <Send className="h-4 w-4" />
            작성
          </button>
        </div>

        <div className="mt-4 divide-y divide-border">
          {comments.length === 0 ? (
            <p className="py-8 text-center text-sm text-muted-foreground">
              아직 댓글이 없습니다.
            </p>
          ) : (
            comments.map((comment) => (
              <CommentItem
                key={comment.id}
                comment={comment}
                postId={postId}
                onCommentAdded={fetchComments}
              />
            ))
          )}
        </div>
      </section>
    </div>
  );
}
