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
