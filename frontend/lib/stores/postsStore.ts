import { create } from 'zustand';
import { type PostResponse } from '@/lib/api/posts';

interface PostsState {
  posts: PostResponse[];
  setPosts: (posts: PostResponse[]) => void;
}

export const usePostsStore = create<PostsState>((set) => ({
  posts: [],
  setPosts: (posts) => set({ posts }),
}));
