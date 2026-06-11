import { cleanup, fireEvent, render, screen } from '@testing-library/react';
import { afterEach, describe, expect, it, vi } from 'vitest';
import SearchSuggestions from '@/Components/Search/SearchSuggestions';

afterEach(() => cleanup());

const RECENT = ['검색어1', '검색어2'];
const SUGGESTIONS = ['자동완성1', '자동완성2', '자동완성3'];

describe('SearchSuggestions — idle 모드', () => {
  it('최근 검색어 섹션 렌더', () => {
    render(
      <SearchSuggestions
        mode="idle"
        recent={RECENT}
        suggestions={[]}
        onPick={vi.fn()}
        onRemoveRecent={vi.fn()}
      />
    );
    expect(screen.getByText('최근 검색어')).toBeInTheDocument();
    expect(screen.getByText('검색어1')).toBeInTheDocument();
    expect(screen.getByText('검색어2')).toBeInTheDocument();
  });

  it('최근 검색어 항목 클릭 → onPick 호출', () => {
    const onPick = vi.fn();
    render(
      <SearchSuggestions
        mode="idle"
        recent={RECENT}
        suggestions={[]}
        onPick={onPick}
        onRemoveRecent={vi.fn()}
      />
    );
    fireEvent.mouseDown(screen.getByText('검색어1'));
    expect(onPick).toHaveBeenCalledWith('검색어1');
  });

  it('최근 검색어 × 버튼 클릭 → onRemoveRecent 호출', () => {
    const onRemoveRecent = vi.fn();
    render(
      <SearchSuggestions
        mode="idle"
        recent={RECENT}
        suggestions={[]}
        onPick={vi.fn()}
        onRemoveRecent={onRemoveRecent}
      />
    );
    const deleteButtons = screen.getAllByRole('button', { name: /×/ });
    fireEvent.mouseDown(deleteButtons[0]);
    expect(onRemoveRecent).toHaveBeenCalledWith(RECENT[0]);
  });

  it('onClearRecent 있으면 "전체 삭제" 버튼 렌더 및 클릭 시 호출', () => {
    const onClearRecent = vi.fn();
    render(
      <SearchSuggestions
        mode="idle"
        recent={RECENT}
        suggestions={[]}
        onPick={vi.fn()}
        onRemoveRecent={vi.fn()}
        onClearRecent={onClearRecent}
      />
    );
    const btn = screen.getByRole('button', { name: '전체 삭제' });
    expect(btn).toBeInTheDocument();
    fireEvent.mouseDown(btn);
    expect(onClearRecent).toHaveBeenCalled();
  });

  it('onClearRecent 없으면 "전체 삭제" 버튼 미렌더', () => {
    render(
      <SearchSuggestions
        mode="idle"
        recent={RECENT}
        suggestions={[]}
        onPick={vi.fn()}
        onRemoveRecent={vi.fn()}
      />
    );
    expect(screen.queryByRole('button', { name: '전체 삭제' })).toBeNull();
  });

  it('idle activeIndex 해당 항목 강조 스타일 적용', () => {
    const { container } = render(
      <SearchSuggestions
        mode="idle"
        recent={RECENT}
        suggestions={[]}
        onPick={vi.fn()}
        onRemoveRecent={vi.fn()}
        activeIndex={0}
      />
    );
    const highlighted = container.querySelector('.bg-zinc-800');
    expect(highlighted).not.toBeNull();
  });

  it('최근 검색어 비면 idle null', () => {
    const { container } = render(
      <SearchSuggestions
        mode="idle"
        recent={[]}
        suggestions={[]}
        onPick={vi.fn()}
        onRemoveRecent={vi.fn()}
      />
    );
    expect(container.firstChild).toBeNull();
  });
});

describe('SearchSuggestions — typing 모드', () => {
  it('suggestions 목록 렌더', () => {
    render(
      <SearchSuggestions
        mode="typing"
        recent={[]}
        suggestions={SUGGESTIONS}
        onPick={vi.fn()}
        onRemoveRecent={vi.fn()}
      />
    );
    expect(screen.getByText('자동완성1')).toBeInTheDocument();
    expect(screen.getByText('자동완성2')).toBeInTheDocument();
  });

  it('suggestion 클릭 → onPick 호출', () => {
    const onPick = vi.fn();
    render(
      <SearchSuggestions
        mode="typing"
        recent={[]}
        suggestions={SUGGESTIONS}
        onPick={onPick}
        onRemoveRecent={vi.fn()}
      />
    );
    fireEvent.mouseDown(screen.getByText('자동완성1'));
    expect(onPick).toHaveBeenCalledWith('자동완성1');
  });

  it('typing activeIndex 해당 항목 강조 스타일 적용', () => {
    render(
      <SearchSuggestions
        mode="typing"
        recent={[]}
        suggestions={SUGGESTIONS}
        onPick={vi.fn()}
        onRemoveRecent={vi.fn()}
        activeIndex={1}
      />
    );
    const buttons = screen.getAllByRole('button');
    expect(buttons[1].classList.contains('bg-zinc-800')).toBe(true);
    expect(buttons[0].classList.contains('bg-zinc-800')).toBe(false);
    expect(buttons[2].classList.contains('bg-zinc-800')).toBe(false);
  });

  it('suggestions 비면 렌더 안 함', () => {
    const { container } = render(
      <SearchSuggestions
        mode="typing"
        recent={[]}
        suggestions={[]}
        onPick={vi.fn()}
        onRemoveRecent={vi.fn()}
      />
    );
    expect(container.firstChild).toBeNull();
  });

  it('query 전달 시 typing 후보 안에 completion이 <b>로 렌더', () => {
    const { container } = render(
      <SearchSuggestions
        mode="typing"
        recent={[]}
        suggestions={SUGGESTIONS}
        onPick={vi.fn()}
        onRemoveRecent={vi.fn()}
        query="자동"
      />
    );
    // SuggestionHighlight 가 completion 부분을 <b> 로 감쌈
    expect(container.querySelector('b')).not.toBeNull();
  });

  it('query 미전달 시 typing 후보가 평문으로 렌더(회귀 없음)', () => {
    render(
      <SearchSuggestions
        mode="typing"
        recent={[]}
        suggestions={SUGGESTIONS}
        onPick={vi.fn()}
        onRemoveRecent={vi.fn()}
      />
    );
    expect(screen.getByText('자동완성1')).toBeInTheDocument();
  });
});
