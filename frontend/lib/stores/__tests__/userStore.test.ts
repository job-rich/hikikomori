import { describe, it, expect, beforeEach } from 'vitest';
import { useUserStore } from '@/lib/stores/userStore';

describe('useUserStore', () => {
  beforeEach(() => {
    localStorage.clear();
    useUserStore.setState({ nickname: null, snowflakeId: null });
  });

  it('초기 상태: nickname과 snowflakeId가 null이어야 한다', () => {
    const { nickname, snowflakeId } = useUserStore.getState();
    expect(nickname).toBeNull();
    expect(snowflakeId).toBeNull();
  });

  it('setUser: nickname과 snowflakeId를 설정해야 한다', () => {
    const { setUser } = useUserStore.getState();
    setUser('위대한 아인슈타인', '123456789');

    const { nickname, snowflakeId } = useUserStore.getState();
    expect(nickname).toBe('위대한 아인슈타인');
    expect(snowflakeId).toBe('123456789');
  });

  it('clearUser: 초기 상태로 리셋해야 한다', () => {
    const { setUser } = useUserStore.getState();
    setUser('위대한 아인슈타인', '123456789');

    const { clearUser } = useUserStore.getState();
    clearUser();

    const { nickname, snowflakeId } = useUserStore.getState();
    expect(nickname).toBeNull();
    expect(snowflakeId).toBeNull();
  });

  it('isLoggedIn: nickname과 snowflakeId가 모두 존재하면 true', () => {
    const { setUser } = useUserStore.getState();
    setUser('위대한 아인슈타인', '123456789');

    const loggedIn = useUserStore.getState().isLoggedIn();
    expect(loggedIn).toBe(true);
  });

  it('isLoggedIn: nickname만 존재하면 false', () => {
    const loggedIn = useUserStore.getState().isLoggedIn();
    expect(loggedIn).toBe(false);
  });

  it('persist: localStorage에 저장되어야 한다', () => {
    const { setUser } = useUserStore.getState();
    setUser('위대한 아인슈타인', '123456789');

    const stored = localStorage.getItem('hikikomori-user');
    expect(stored).not.toBeNull();

    const parsed = JSON.parse(stored!);
    expect(parsed.state.nickname).toBe('위대한 아인슈타인');
    expect(parsed.state.snowflakeId).toBe('123456789');
  });
});
