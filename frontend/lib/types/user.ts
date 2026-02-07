export interface UserState {
  nickname: string | null;
  snowflakeId: string | null;
  setUser: (nickname: string, snowflakeId: string) => void;
  clearUser: () => void;
  isLoggedIn: () => boolean;
}
