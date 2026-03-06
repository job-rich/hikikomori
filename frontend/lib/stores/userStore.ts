import { create } from 'zustand';
import { persist, createJSONStorage } from 'zustand/middleware';
import { UserState } from '@/lib/types/user';

export const useUserStore = create<UserState>()(
  persist(
    (set, get) => ({
      nickname: null,
      snowflakeId: null,
      nicknameModalOpen: false,
      setUser: (nickname: string, snowflakeId: string) =>
        set({ nickname, snowflakeId, nicknameModalOpen: false }),
      clearUser: () => set({ nickname: null, snowflakeId: null }),
      isLoggedIn: () => {
        const { nickname, snowflakeId } = get();
        return nickname !== null && snowflakeId !== null;
      },
      openNicknameModal: () => set({ nicknameModalOpen: true }),
      closeNicknameModal: () => set({ nicknameModalOpen: false }),
      requireNickname: () => {
        if (get().isLoggedIn()) return false;
        set({ nicknameModalOpen: true });
        return true;
      },
    }),
    {
      name: 'hikikomori-user',
      storage: createJSONStorage(() => localStorage),
      partialize: (state) => ({
        nickname: state.nickname,
        snowflakeId: state.snowflakeId,
      }),
    }
  )
);
