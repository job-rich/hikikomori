import { displayTagFromApi, toApiTag } from '@/lib/utils/postTagApi';
import { apiClient } from './client';

function mapPost(p: PostResponse): PostResponse {
  return { ...p, tag: displayTagFromApi(p.tag) };
}

export interface PostCreateRequest {
  title: string;
  content: string;
  tag: string;
  userId: number;
  nickName: string;
}

export interface PostResponse {
  id: string;
  userId: number;
  nickName: string;
  title: string;
  content: string;
  tag: string;
  commentCount: number;
  viewCount: number;
  likeCount: number;
  createdAt: string;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

export async function createPost(
  request: PostCreateRequest
): Promise<PostResponse> {
  const raw = await apiClient<PostResponse>('/api/posts', {
    method: 'POST',
    body: JSON.stringify({ ...request, tag: toApiTag(request.tag) }),
  });
  return mapPost(raw);
}

export interface CommentResponse {
  id: string;
  userId: number;
  nickName: string;
  content: string;
  hidden?: boolean;
  createdAt: string;
  deletedAt?: string | null;
  children: CommentResponse[];
}

export interface CommentCreateRequest {
  content: string;
  parentId?: string | null;
  userId: number;
  nickName: string;
}

export async function getPosts(
  page = 0,
  size = 6,
  sort?: string
): Promise<PageResponse<PostResponse>> {
  const params = new URLSearchParams({ page: String(page), size: String(size) });
  if (sort) params.append('sort', sort);
  const data = await apiClient<PageResponse<PostResponse>>(
    `/api/posts?${params}`
  );
  return { ...data, content: data.content.map(mapPost) };
}

export async function getMyPosts(
  userId: number,
  page = 0,
  size = 6,
  sort?: string
): Promise<PageResponse<PostResponse>> {
  const params = new URLSearchParams({ page: String(page), size: String(size) });
  if (sort) params.append('sort', sort);
  const data = await apiClient<PageResponse<PostResponse>>(
    `/api/posts/my/${userId}?${params}`
  );
  return { ...data, content: data.content.map(mapPost) };
}

// 포스트 상세 화면

// 포스트 조회
export async function getPost(id: string): Promise<PostResponse> {
  const raw = await apiClient<PostResponse>(`/api/posts/${id}`);
  return mapPost(raw);
}

export interface PostUpdateRequest {
  userId: number;
  title: string;
  content: string;
  tag: string;
}

/** PATCH 성공 시 본문 없음(204). 이후 `getPost`로 다시 조회한다. */
export function updatePost(
  id: string,
  request: PostUpdateRequest
): Promise<void> {
  return apiClient<void>(`/api/posts/${id}`, {
    method: 'PATCH',
    body: JSON.stringify({ ...request, tag: toApiTag(request.tag) }),
  });
}

export function deletePost(id: string, userId: number): Promise<void> {
  const q = new URLSearchParams({ userId: String(userId) });
  return apiClient<void>(`/api/posts/${id}?${q}`, {
    method: 'DELETE',
  });
}

export function recordView(id: string): Promise<void> {
  return apiClient<void>(`/api/posts/${id}/view`, { method: 'POST' });
}

export function likePost(id: string): Promise<void> {
  return apiClient<void>(`/api/posts/${id}/like`, { method: 'POST' });
}

// 댓글 목록 조회
export function getComments(postId: string): Promise<CommentResponse[]> {
  return apiClient<CommentResponse[]>(`/api/posts/${postId}/comments`);
}

// 댓글 생성
export function createComment(
  postId: string,
  request: CommentCreateRequest
): Promise<CommentResponse> {
  return apiClient<CommentResponse>(`/api/posts/${postId}/comments`, {
    method: 'POST',
    body: JSON.stringify(request),
  });
}

export interface CommentUpdateRequest {
  userId: number;
  content: string;
}

/** PATCH 성공 시 본문 없음(204). 이후 `getComments`로 다시 조회한다. */
export function updateComment(
  postId: string,
  commentId: string,
  request: CommentUpdateRequest
): Promise<void> {
  return apiClient<void>(`/api/posts/${postId}/comments/${commentId}`, {
    method: 'PATCH',
    body: JSON.stringify(request),
  });
}

export function deleteComment(
  postId: string,
  commentId: string,
  userId: number
): Promise<void> {
  const q = new URLSearchParams({ userId: String(userId) });
  return apiClient<void>(`/api/posts/${postId}/comments/${commentId}?${q}`, {
    method: 'DELETE',
  });
}
