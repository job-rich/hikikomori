export interface UserState {
  nickname: string | null;
  snowflakeId: string | null;
  nicknameModalOpen: boolean;
  setUser: (nickname: string, snowflakeId: string) => void;
  clearUser: () => void;
  isLoggedIn: () => boolean;
  openNicknameModal: () => void;
  closeNicknameModal: () => void;
  requireNickname: () => boolean;
}
