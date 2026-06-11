import { displayTagFromApi, toApiTag } from '@/lib/utils/postTagApi';
import { apiClient } from './client';
import type { PageResponse } from './posts';

export async function suggest(query: string, limit = 8): Promise<string[]> {
  if (!query.trim()) return [];
  const params = new URLSearchParams({ query, limit: String(limit) });
  return apiClient<string[]>(`/api/posts/search/suggest?${params}`);
}

export type SearchType = 'all' | 'post' | 'comment';
export type SearchSort = 'relevance' | 'latest' | 'comments';

export interface SearchResult {
  type: 'POST' | 'COMMENT' | 'USER';
  id: string | null;
  title: string | null;
  nickName: string;
  tag: string | null;
  snippet: string;
  commentCount: number;
  postCount: number;
  createdAt: string;
}

interface SearchParams {
  query: string;
  tag?: string;
  type?: SearchType;
  sort?: SearchSort;
  page?: number;
  size?: number;
}

export interface SearchCounts {
  post: number;
  comment: number;
  total: number;
}

export async function getSearchCounts(params: {
  query: string;
  tag?: string;
}): Promise<SearchCounts> {
  if (!params.query.trim()) return { post: 0, comment: 0, total: 0 };
  const [post, comment] = await Promise.all([
    searchPosts({ ...params, type: 'post', size: 1 }),
    searchPosts({ ...params, type: 'comment', size: 1 }),
  ]);
  return {
    post: post.totalElements,
    comment: comment.totalElements,
    total: post.totalElements + comment.totalElements,
  };
}

export async function searchPosts(
  params: SearchParams
): Promise<PageResponse<SearchResult>> {
  const {
    query,
    tag,
    type = 'all',
    sort = 'relevance',
    page = 0,
    size = 6,
  } = params;

  const qs = new URLSearchParams();
  qs.set('query', query);
  qs.set('type', type.toUpperCase());
  qs.set('sort', sort.toUpperCase());
  qs.set('page', String(page));
  qs.set('size', String(size));
  if (tag) qs.set('tag', toApiTag(tag));

  const data = await apiClient<PageResponse<SearchResult>>(
    `/api/posts/search?${qs}`
  );
  return {
    ...data,
    content: data.content.map((item) => ({
      ...item,
      tag: item.tag ? displayTagFromApi(item.tag) : null,
    })),
  };
}
