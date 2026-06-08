import { afterEach, beforeEach, describe, expect, it } from 'vitest';
import { cleanup, fireEvent, render } from '@testing-library/react';
import { useScrolledToBottom } from '@/lib/hooks/useScrolledToBottom';

// jsdom 은 layout 을 측정하지 않으므로 scrollHeight/clientHeight/scrollTop 을 prototype mock.
const originals: Record<string, PropertyDescriptor | undefined> = {};

function mockMetrics({
  scrollHeight,
  clientHeight,
  scrollTop = 0,
}: {
  scrollHeight: number;
  clientHeight: number;
  scrollTop?: number;
}) {
  Object.defineProperty(HTMLElement.prototype, 'scrollHeight', {
    configurable: true,
    value: scrollHeight,
  });
  Object.defineProperty(HTMLElement.prototype, 'clientHeight', {
    configurable: true,
    value: clientHeight,
  });
  Object.defineProperty(HTMLElement.prototype, 'scrollTop', {
    configurable: true,
    writable: true,
    value: scrollTop,
  });
}

function Probe({ resetKey }: { resetKey: number }) {
  const { ref, isBottom, onScroll } = useScrolledToBottom<HTMLDivElement>(
    resetKey
  );
  return (
    <div
      ref={ref}
      onScroll={onScroll}
      data-testid="probe"
      data-bottom={isBottom ? '1' : '0'}
    />
  );
}

beforeEach(() => {
  originals.scrollHeight = Object.getOwnPropertyDescriptor(
    HTMLElement.prototype,
    'scrollHeight'
  );
  originals.clientHeight = Object.getOwnPropertyDescriptor(
    HTMLElement.prototype,
    'clientHeight'
  );
  originals.scrollTop = Object.getOwnPropertyDescriptor(
    HTMLElement.prototype,
    'scrollTop'
  );
});

afterEach(() => {
  for (const [k, d] of Object.entries(originals)) {
    if (d) Object.defineProperty(HTMLElement.prototype, k, d);
    else delete (HTMLElement.prototype as unknown as Record<string, unknown>)[k];
  }
  cleanup();
});

describe('useScrolledToBottom', () => {
  it('콘텐츠가 viewport 보다 길어 스크롤이 가능하면 초기 isBottom=false', () => {
    mockMetrics({ scrollHeight: 1000, clientHeight: 400 });
    const { getByTestId } = render(<Probe resetKey={0} />);
    expect(getByTestId('probe').getAttribute('data-bottom')).toBe('0');
  });

  it('콘텐츠가 viewport 안에 다 들어가면 마운트 직후 isBottom=true', () => {
    mockMetrics({ scrollHeight: 300, clientHeight: 400 });
    const { getByTestId } = render(<Probe resetKey={0} />);
    expect(getByTestId('probe').getAttribute('data-bottom')).toBe('1');
  });

  it('스크롤이 끝까지 도달하면 isBottom=true 로 전환', () => {
    mockMetrics({ scrollHeight: 1000, clientHeight: 400 });
    const { getByTestId } = render(<Probe resetKey={0} />);
    const el = getByTestId('probe') as HTMLDivElement;
    Object.defineProperty(el, 'scrollTop', { configurable: true, value: 600 });
    fireEvent.scroll(el);
    expect(el.getAttribute('data-bottom')).toBe('1');
  });

  it('resetKey 가 바뀌면 isBottom=false 로 회귀 (스크롤 가능한 콘텐츠)', () => {
    mockMetrics({ scrollHeight: 1000, clientHeight: 400 });
    const { getByTestId, rerender } = render(<Probe resetKey={0} />);
    const el = getByTestId('probe') as HTMLDivElement;
    Object.defineProperty(el, 'scrollTop', { configurable: true, value: 600 });
    fireEvent.scroll(el);
    expect(el.getAttribute('data-bottom')).toBe('1');
    rerender(<Probe resetKey={1} />);
    Object.defineProperty(el, 'scrollTop', { configurable: true, value: 0 });
    expect(el.getAttribute('data-bottom')).toBe('0');
  });
});
