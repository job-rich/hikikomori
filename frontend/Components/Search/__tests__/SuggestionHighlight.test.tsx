import { cleanup, render } from '@testing-library/react';
import { afterEach, describe, expect, it } from 'vitest';
import SuggestionHighlight from '@/Components/Search/SuggestionHighlight';

afterEach(() => cleanup());

describe('SuggestionHighlight', () => {
  it('매칭 후미(completion)가 <b>로 렌더, 매칭부는 볼드 아님 — after only', () => {
    const { container } = render(
      <SuggestionHighlight text="자유게시판" query="자유" />
    );
    // completion "게시판"이 <b> 안에 있어야 함
    expect(container.querySelector('b')?.textContent).toBe('게시판');
    // 매칭부 "자유"는 <b> 밖(일반)
    const bTags = container.querySelectorAll('b');
    expect(bTags.length).toBe(1);
    expect(bTags[0].textContent).toBe('게시판');
  });

  it('before/after 둘 다 <b> — 중간 매칭', () => {
    const { container } = render(
      <SuggestionHighlight text="대한자유국" query="자유" />
    );
    const bTags = container.querySelectorAll('b');
    expect(bTags.length).toBe(2);
    expect(bTags[0].textContent).toBe('대한');
    expect(bTags[1].textContent).toBe('국');
  });

  it('query="" → 평문, <b> 없음', () => {
    const { container } = render(
      <SuggestionHighlight text="자유게시판" query="" />
    );
    expect(container.querySelector('b')).toBeNull();
    expect(container.textContent).toBe('자유게시판');
  });

  it('미매칭(text에 query 없음) → 평문, <b> 없음', () => {
    const { container } = render(
      <SuggestionHighlight text="게시판" query="자유" />
    );
    expect(container.querySelector('b')).toBeNull();
    expect(container.textContent).toBe('게시판');
  });

  it('대소문자 무시 매칭', () => {
    const { container } = render(
      <SuggestionHighlight text="Hello World" query="hello" />
    );
    const bTags = container.querySelectorAll('b');
    // " World" 가 after <b>로 렌더
    expect(bTags.length).toBe(1);
    expect(bTags[0].textContent).toBe(' World');
  });
});
