import { afterEach, describe, expect, it } from 'vitest';
import { cleanup, render, screen } from '@testing-library/react';
import Highlight from '@/Components/Common/Highlight/Highlight';

afterEach(cleanup);

describe('Highlight', () => {
  it('default color(#ef4444) + fontWeight 600 으로 children 을 감싼다', () => {
    render(<Highlight>강조</Highlight>);
    const el = screen.getByText('강조');
    expect(el.tagName).toBe('SPAN');
    expect(el.style.color).toBe('rgb(239, 68, 68)');
    expect(el.style.fontWeight).toBe('600');
  });

  it('color prop 으로 색을 override 한다', () => {
    render(<Highlight color="#f59e0b">경고</Highlight>);
    const el = screen.getByText('경고');
    expect(el.style.color).toBe('rgb(245, 158, 11)');
  });
});
