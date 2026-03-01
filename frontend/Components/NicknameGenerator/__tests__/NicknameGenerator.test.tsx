import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { render, screen, fireEvent, cleanup } from '@testing-library/react';
import NicknameGenerator from '@/Components/NicknameGenerator/NicknameGenerator';
import { generateNickname } from '@/lib/utils/nickname';
import { generateSnowflakeId } from '@/lib/utils/snowflake';
import { useUserStore } from '@/lib/stores/userStore';

vi.mock('@/lib/utils/nickname');
vi.mock('@/lib/utils/snowflake');

const mockGenerateNickname = vi.mocked(generateNickname);
const mockGenerateSnowflakeId = vi.mocked(generateSnowflakeId);

describe('NicknameGenerator', () => {
  afterEach(() => {
    cleanup();
    document.body.style.overflow = '';
  });

  beforeEach(() => {
    mockGenerateNickname.mockReset();
    mockGenerateSnowflakeId.mockReset();
    useUserStore.setState({
      nickname: null,
      snowflakeId: null,
      nicknameModalOpen: false,
    });

    mockGenerateNickname
      .mockReturnValueOnce('위대한 아인슈타인')
      .mockReturnValueOnce('전설적인 뉴턴')
      .mockReturnValue('용감한 테슬라');

    mockGenerateSnowflakeId
      .mockReturnValueOnce('123456789')
      .mockReturnValueOnce('987654321')
      .mockReturnValue('111111111');
  });

  it('모달이 닫혀있으면 아무것도 렌더링하지 않는다', () => {
    render(<NicknameGenerator />);
    expect(screen.queryByText('위대한 아인슈타인')).not.toBeInTheDocument();
  });

  it('모달이 열리면 닉네임이 표시된다', () => {
    useUserStore.setState({ nicknameModalOpen: true });
    render(<NicknameGenerator />);
    expect(screen.getByText('위대한 아인슈타인')).toBeInTheDocument();
  });

  it('Snowflake 키값이 화면에 노출되지 않아야 한다', () => {
    useUserStore.setState({ nicknameModalOpen: true });
    render(<NicknameGenerator />);
    expect(screen.getByText('위대한 아인슈타인')).toBeInTheDocument();
    expect(screen.queryByText('123456789')).not.toBeInTheDocument();
  });

  it('재배정 클릭 시 닉네임이 변경되어야 한다', () => {
    useUserStore.setState({ nicknameModalOpen: true });
    render(<NicknameGenerator />);
    expect(screen.getByText('위대한 아인슈타인')).toBeInTheDocument();

    fireEvent.click(screen.getByRole('button', { name: '재배정' }));
    expect(screen.getByText('전설적인 뉴턴')).toBeInTheDocument();
  });

  it('입장 클릭 시 store에 저장하고 모달이 닫혀야 한다', () => {
    useUserStore.setState({ nicknameModalOpen: true });
    render(<NicknameGenerator />);

    fireEvent.click(screen.getByRole('button', { name: '입장' }));

    const { nickname, snowflakeId, nicknameModalOpen } =
      useUserStore.getState();
    expect(nickname).toBe('위대한 아인슈타인');
    expect(snowflakeId).toBe('123456789');
    expect(nicknameModalOpen).toBe(false);
  });

  it('재배정 후 입장 시 재생성된 닉네임이 store에 저장되어야 한다', () => {
    useUserStore.setState({ nicknameModalOpen: true });
    render(<NicknameGenerator />);

    fireEvent.click(screen.getByRole('button', { name: '재배정' }));
    expect(screen.getByText('전설적인 뉴턴')).toBeInTheDocument();

    fireEvent.click(screen.getByRole('button', { name: '입장' }));

    const { nickname, snowflakeId, nicknameModalOpen } =
      useUserStore.getState();
    expect(nickname).toBe('전설적인 뉴턴');
    expect(snowflakeId).toBe('987654321');
    expect(nicknameModalOpen).toBe(false);
  });

  it('여러 번 재배정 후 입장 시 마지막 닉네임이 store에 저장되어야 한다', () => {
    useUserStore.setState({ nicknameModalOpen: true });
    render(<NicknameGenerator />);

    fireEvent.click(screen.getByRole('button', { name: '재배정' }));
    fireEvent.click(screen.getByRole('button', { name: '재배정' }));
    expect(screen.getByText('용감한 테슬라')).toBeInTheDocument();

    fireEvent.click(screen.getByRole('button', { name: '입장' }));

    const { nickname, snowflakeId } = useUserStore.getState();
    expect(nickname).toBe('용감한 테슬라');
    expect(snowflakeId).toBe('111111111');
  });

  it('ESC 키를 누르면 모달이 닫혀야 한다', () => {
    useUserStore.setState({ nicknameModalOpen: true });
    render(<NicknameGenerator />);
    expect(screen.getByRole('dialog')).toBeInTheDocument();

    fireEvent.keyDown(window, { key: 'Escape' });

    expect(useUserStore.getState().nicknameModalOpen).toBe(false);
  });

  it('모달 배경 클릭 시 모달이 닫혀야 한다', () => {
    useUserStore.setState({ nicknameModalOpen: true });
    render(<NicknameGenerator />);
    const backdrop = screen.getByRole('dialog');

    fireEvent.click(backdrop);

    expect(useUserStore.getState().nicknameModalOpen).toBe(false);
  });

  it('모달 카드 내부 클릭 시 모달이 닫히지 않아야 한다', () => {
    useUserStore.setState({ nicknameModalOpen: true });
    render(<NicknameGenerator />);

    fireEvent.click(screen.getByText('위대한 아인슈타인'));

    expect(useUserStore.getState().nicknameModalOpen).toBe(true);
  });

  it('openNicknameModal은 모달을 열어야 한다', () => {
    useUserStore.getState().openNicknameModal();
    expect(useUserStore.getState().nicknameModalOpen).toBe(true);
  });

  it('closeNicknameModal은 모달을 닫아야 한다', () => {
    useUserStore.setState({ nicknameModalOpen: true });
    useUserStore.getState().closeNicknameModal();
    expect(useUserStore.getState().nicknameModalOpen).toBe(false);
  });

  it('requireNickname은 비로그인 시 모달을 열고 true를 반환한다', () => {
    const result = useUserStore.getState().requireNickname();
    expect(result).toBe(true);
    expect(useUserStore.getState().nicknameModalOpen).toBe(true);
  });

  it('requireNickname은 로그인 시 모달을 열지 않고 false를 반환한다', () => {
    useUserStore.setState({ nickname: '기존닉', snowflakeId: '999' });
    const result = useUserStore.getState().requireNickname();
    expect(result).toBe(false);
    expect(useUserStore.getState().nicknameModalOpen).toBe(false);
  });
});
