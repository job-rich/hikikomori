import { create } from 'zustand';
import { persist, createJSONStorage } from 'zustand/middleware';
import { UserState } from '@/lib/types/user';

export const useUserStore = create<UserState>()(
  persist(
    (set, get) => ({
      nickname: null,
      snowflakeId: null,
      setUser: (nickname: string, snowflakeId: string) =>
        set({ nickname, snowflakeId }),
      clearUser: () => set({ nickname: null, snowflakeId: null }),
      isLoggedIn: () => {
        const { nickname, snowflakeId } = get();
        return nickname !== null && snowflakeId !== null;
      },
    }),
    {
      name: 'hikikomori-user',
      storage: createJSONStorage(() => localStorage),
    }
  )
);
