'use client';

import {
  useLayoutEffect,
  useRef,
  useState,
  type RefObject,
  type UIEventHandler,
} from 'react';

const DEFAULT_TOLERANCE_RATIO = 0.06;

interface UseScrolledToBottomResult<T extends HTMLElement> {
  ref: RefObject<T | null>;
  isBottom: boolean;
  onScroll: UIEventHandler<T>;
}

// resetKey 변경 → unlockKey 와 mismatch 로 자동 false 회귀 (race-free derive 패턴).
// tolerance 는 viewport 의 6% — sub-pixel / zoom / 살짝 넘친 콘텐츠 보정.
export function useScrolledToBottom<T extends HTMLElement = HTMLDivElement>(
  resetKey: unknown,
  options: { toleranceRatio?: number } = {}
): UseScrolledToBottomResult<T> {
  const toleranceRatio = options.toleranceRatio ?? DEFAULT_TOLERANCE_RATIO;
  const ref = useRef<T | null>(null);
  const [unlockKey, setUnlockKey] = useState<unknown>(Symbol('initial-locked'));
  const isBottom = unlockKey === resetKey;

  useLayoutEffect(() => {
    const el = ref.current;
    if (!el) return;
    const measure = () => {
      const tolerance = el.clientHeight * toleranceRatio;
      if (el.scrollHeight <= el.clientHeight + tolerance) {
        setUnlockKey(resetKey);
      }
    };
    measure();
    const ro =
      typeof ResizeObserver !== 'undefined'
        ? new ResizeObserver(measure)
        : null;
    ro?.observe(el);
    return () => ro?.disconnect();
  }, [resetKey, toleranceRatio]);

  const onScroll: UIEventHandler<T> = (event) => {
    const el = event.currentTarget;
    const tolerance = el.clientHeight * toleranceRatio;
    if (el.scrollTop + el.clientHeight >= el.scrollHeight - tolerance) {
      setUnlockKey(resetKey);
    }
  };

  return { ref, isBottom, onScroll };
}
