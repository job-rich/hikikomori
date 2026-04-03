import { apiClient } from './client';

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
  createdAt: string;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

export function createPost(request: PostCreateRequest): Promise<PostResponse> {
  return apiClient<PostResponse>('/api/posts', {
    method: 'POST',
    body: JSON.stringify(request),
  });
}

export interface CommentResponse {
  id: string;
  userId: number;
  nickName: string;
  content: string;
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

export function getPosts(
  page = 0,
  size = 20
): Promise<PageResponse<PostResponse>> {
  return apiClient<PageResponse<PostResponse>>(
    `/api/posts?page=${page}&size=${size}`
  );
}

// 포스트 상세 화면

// 포스트 조회
export function getPost(id: string): Promise<PostResponse> {
  return apiClient<PostResponse>(`/api/posts/${id}`);
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
    body: JSON.stringify(request),
  });
}

export function deletePost(id: string, userId: number): Promise<void> {
  const q = new URLSearchParams({ userId: String(userId) });
  return apiClient<void>(`/api/posts/${id}?${q}`, {
    method: 'DELETE',
  });
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
