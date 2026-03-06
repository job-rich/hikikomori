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

export function getPosts(
  page = 0,
  size = 20
): Promise<PageResponse<PostResponse>> {
  return apiClient<PageResponse<PostResponse>>(
    `/api/posts?page=${page}&size=${size}`
  );
}
