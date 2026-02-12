import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import {
  render,
  screen,
  fireEvent,
  cleanup,
  waitFor,
} from '@testing-library/react';
import NicknameGenerator from '@/Components/NicknameGenerator/NicknameGenerator';
import { generateNickname } from '@/lib/utils/nickname';
import { generateSnowflakeId } from '@/lib/utils/snowflake';
import { useUserStore } from '@/lib/stores/userStore';

const pushMock = vi.fn();
const replaceMock = vi.fn();

vi.mock('next/navigation', () => ({
  useRouter: () => ({
    push: pushMock,
    replace: replaceMock,
  }),
}));

vi.mock('@/lib/utils/nickname');
vi.mock('@/lib/utils/snowflake');

const mockGenerateNickname = vi.mocked(generateNickname);
const mockGenerateSnowflakeId = vi.mocked(generateSnowflakeId);

describe('NicknameGenerator', () => {
  afterEach(() => {
    cleanup();
  });

  beforeEach(() => {
    pushMock.mockClear();
    replaceMock.mockClear();
    mockGenerateNickname.mockReset();
    mockGenerateSnowflakeId.mockReset();
    useUserStore.setState({ nickname: null, snowflakeId: null });

    mockGenerateNickname
      .mockReturnValueOnce('위대한 아인슈타인')
      .mockReturnValueOnce('전설적인 뉴턴')
      .mockReturnValue('용감한 테슬라');

    mockGenerateSnowflakeId
      .mockReturnValueOnce('123456789')
      .mockReturnValueOnce('987654321')
      .mockReturnValue('111111111');
  });

  it('닉네임이 화면에 표시되어야 한다', () => {
    render(<NicknameGenerator />);
    expect(screen.getByText('위대한 아인슈타인')).toBeInTheDocument();
  });

  it('Snowflake 키값이 화면에 노출되지 않아야 한다', () => {
    render(<NicknameGenerator />);
    expect(screen.getByText('위대한 아인슈타인')).toBeInTheDocument();
    expect(screen.queryByText('123456789')).not.toBeInTheDocument();
  });

  // ArenaTheme 적용으로 버튼 텍스트 변경됨 (확인→입장, 닉네임 재생성→재배정)
  // 테마 제거 시 아래 주석 해제하고 ArenaTheme 테스트 삭제할 것
  // it('"확인" 버튼이 존재해야 한다', () => {
  //   render(<NicknameGenerator />);
  //   expect(screen.getByRole('button', { name: '확인' })).toBeInTheDocument();
  // });

  // it('"닉네임 재생성" 버튼이 존재해야 한다', () => {
  //   render(<NicknameGenerator />);
  //   expect(
  //     screen.getByRole('button', { name: '닉네임 재생성' })
  //   ).toBeInTheDocument();
  // });

  // it('재생성 클릭 시 닉네임이 변경되어야 한다', () => {
  //   render(<NicknameGenerator />);
  //   expect(screen.getByText('위대한 아인슈타인')).toBeInTheDocument();
  //
  //   fireEvent.click(screen.getByRole('button', { name: '닉네임 재생성' }));
  //   expect(screen.getByText('전설적인 뉴턴')).toBeInTheDocument();
  // });

  // it('확인 클릭 시 store에 저장 후 /home으로 이동해야 한다', () => {
  //   render(<NicknameGenerator />);
  //   expect(screen.getByText('위대한 아인슈타인')).toBeInTheDocument();
  //
  //   fireEvent.click(screen.getByRole('button', { name: '확인' }));
  //   expect(pushMock).toHaveBeenCalledWith('/home');
  //
  //   const { nickname, snowflakeId } = useUserStore.getState();
  //   expect(nickname).toBe('위대한 아인슈타인');
  //   expect(snowflakeId).toBe('123456789');
  // });

  it('재방문 시 (store에 데이터 있으면) /home으로 리다이렉트해야 한다', async () => {
    useUserStore.setState({
      nickname: '기존닉네임',
      snowflakeId: '999999999',
    });

    render(<NicknameGenerator />);

    await waitFor(() => {
      expect(replaceMock).toHaveBeenCalledWith('/home');
    });
  });
});
