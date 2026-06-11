'use client';

import SuggestionHighlight from '@/Components/Search/SuggestionHighlight';

interface Props {
  mode: 'idle' | 'typing';
  recent: string[];
  suggestions: string[];
  /** 기본 -1. 부모(SearchView)가 ↑↓로 제어 */
  activeIndex?: number;
  onPick: (q: string) => void;
  onRemoveRecent: (q: string) => void;
  /** '전체 삭제' */
  onClearRecent?: () => void;
  /** typing 모드 후보 강조용 쿼리 (optional — 기존 테스트 회귀 방지) */
  query?: string;
}

export default function SearchSuggestions({
  mode,
  recent,
  suggestions,
  activeIndex = -1,
  onPick,
  onRemoveRecent,
  onClearRecent,
  query,
}: Props) {
  if (mode === 'typing') {
    if (suggestions.length === 0) return null;
    return (
      <div className="absolute left-0 right-0 top-full z-50 mt-1 rounded border border-border bg-card shadow-lg">
        {suggestions.map((s, i) => (
          <button
            key={s}
            className={`flex w-full items-center gap-2 px-4 py-2 text-left font-mono text-sm text-foreground hover:bg-zinc-800 ${i === activeIndex ? 'bg-zinc-800' : ''}`}
            onMouseDown={() => onPick(s)}
          >
            <span>🔍</span>
            <span>
              {query ? <SuggestionHighlight text={s} query={query} /> : s}
            </span>
          </button>
        ))}
      </div>
    );
  }

  // idle mode — 최근검색어만 표시
  if (recent.length === 0) return null;

  return (
    <div className="absolute left-0 right-0 top-full z-50 mt-1 rounded border border-border bg-card shadow-lg">
      <div className="px-4 pb-2 pt-3">
        <div className="mb-2 flex items-center justify-between font-mono text-xs text-muted-foreground">
          <span>최근 검색어</span>
          {onClearRecent && (
            <button
              className="font-mono text-xs text-muted-foreground hover:text-foreground"
              onMouseDown={onClearRecent}
            >
              전체 삭제
            </button>
          )}
        </div>
        {recent.map((r, i) => (
          <div
            key={r}
            className={`flex items-center justify-between ${i === activeIndex ? 'bg-zinc-800' : ''}`}
          >
            <button
              className="flex flex-1 items-center gap-2 py-1 text-left font-mono text-sm text-foreground hover:text-[hsl(var(--neon))]"
              onMouseDown={() => onPick(r)}
            >
              <span>⏰</span>
              <span>{r}</span>
            </button>
            <button
              aria-label={`× ${r}`}
              className="px-2 py-1 font-mono text-xs text-muted-foreground hover:text-foreground"
              onMouseDown={() => onRemoveRecent(r)}
            >
              ×
            </button>
          </div>
        ))}
      </div>
    </div>
  );
}
