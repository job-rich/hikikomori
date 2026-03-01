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

export function createPost(request: PostCreateRequest): Promise<PostResponse> {
  return apiClient<PostResponse>('/api/posts', {
    method: 'POST',
    body: JSON.stringify(request),
  });
}
