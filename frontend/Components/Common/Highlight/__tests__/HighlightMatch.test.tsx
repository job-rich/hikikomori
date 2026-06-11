import { afterEach, describe, it, expect } from 'vitest';
import { cleanup, render } from '@testing-library/react';
import HighlightMatch from '@/Components/Common/Highlight/HighlightMatch';

afterEach(cleanup);

describe('HighlightMatch', () => {
  it('매칭 구간을 하이라이트 span으로 감싼다', () => {
    const { container } = render(
      <HighlightMatch text="철학적 질문" query="철학" />
    );
    expect(container.querySelectorAll('span').length).toBeGreaterThan(0);
  });

  it('대소문자를 무시하고 매칭한다', () => {
    const { container } = render(
      <HighlightMatch text="Hello World" query="hello" />
    );
    expect(container.querySelectorAll('span').length).toBeGreaterThan(0);
  });

  it('정규식 특수문자를 이스케이프한다', () => {
    expect(() =>
      render(<HighlightMatch text="a.b*c" query="a.b" />)
    ).not.toThrow();
    const { container } = render(<HighlightMatch text="a.b*c" query="a.b" />);
    expect(container.querySelectorAll('span').length).toBeGreaterThan(0);
  });

  it('매칭 없으면 평문 그대로 렌더링한다', () => {
    const { container } = render(
      <HighlightMatch text="안녕하세요" query="xyz" />
    );
    expect(container.textContent).toBe('안녕하세요');
    expect(container.querySelectorAll('span').length).toBe(0);
  });

  it('빈 query면 text를 그대로 렌더링한다', () => {
    const { container } = render(<HighlightMatch text="안녕하세요" query="" />);
    expect(container.textContent).toBe('안녕하세요');
    expect(container.querySelectorAll('span').length).toBe(0);
  });

  it('빈 text면 빈 결과를 렌더링한다', () => {
    const { container } = render(<HighlightMatch text="" query="철학" />);
    expect(container.textContent).toBe('');
  });
});
