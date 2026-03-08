import { create } from 'zustand';

export interface LocalPost {
  id: string;
  userId: number;
  nickName: string;
  title: string;
  content: string;
  tag: string;
  createdAt: string;
}

interface PostState {
  posts: LocalPost[];
  addPost: (post: Omit<LocalPost, 'id' | 'createdAt'>) => void;
  getPostById: (id: string) => LocalPost | undefined;
}

function generateId(): string {
  return `local-${Date.now()}-${Math.random().toString(36).slice(2, 9)}`;
}

export const usePostStore = create<PostState>()((set, get) => ({
  posts: [],
  addPost: (post) =>
    set((state) => {
      const newPost: LocalPost = {
        ...post,
        id: generateId(),
        createdAt: new Date().toISOString(),
      };
      return { posts: [newPost, ...state.posts] };
    }),
  getPostById: (id) => get().posts.find((p) => p.id === id),
}));
