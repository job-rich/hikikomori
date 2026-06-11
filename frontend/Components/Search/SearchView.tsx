'use client';

import {
  useState,
  useEffect,
  useCallback,
  useRef,
  type KeyboardEvent,
} from 'react';
import { useRouter, useSearchParams } from 'next/navigation';
import { searchPosts, suggest, getSearchCounts } from '@/lib/api/search';
import type {
  SearchResult,
  SearchType,
  SearchSort,
  SearchCounts,
} from '@/lib/api/search';
import type { PageResponse } from '@/lib/api/posts';
import { TAGS, TAG_STYLES } from '@/lib/utils/tagColors';
import { formatDate } from '@/lib/utils/formatDate';
import HighlightMatch from '@/Components/Common/Highlight/HighlightMatch';
import SearchSuggestions from '@/Components/Search/SearchSuggestions';
import PostCard from '@/Components/Common/Post/Post-Card/Post-Card';
import {
  getRecentSearches,
  addRecentSearch,
  removeRecentSearch,
  clearRecentSearches,
} from '@/lib/utils/recentSearches';

const TYPE_TABS: { label: string; value: SearchType }[] = [
  { label: '전체', value: 'all' },
  { label: '게시글', value: 'post' },
  { label: '댓글', value: 'comment' },
];

const SORT_OPTIONS: { label: string; value: SearchSort }[] = [
  { label: '관련순', value: 'relevance' },
  { label: '최신순', value: 'latest' },
  { label: '댓글많은순', value: 'comments' },
];

function tabCount(value: SearchType, counts: SearchCounts | null): number {
  if (!counts) return 0;
  if (value === 'all') return counts.total;
  return counts[value as 'post' | 'comment'];
}

function CommentResultCard({
  item,
  query,
}: {
  item: SearchResult;
  query: string;
}) {
  const router = useRouter();
  const canNavigate = item.id != null;
  return (
    <div
      className={`rounded-lg border border-border bg-card p-4 transition-all ${
        canNavigate ? 'cursor-pointer hover:border-zinc-600' : 'opacity-60'
      }`}
      onClick={() => canNavigate && router.push(`/posts/${item.id}`)}
    >
      <div className="mb-1 flex flex-wrap items-center gap-2 text-xs text-muted-foreground">
        <span>{item.nickName}</span>
        <span>·</span>
        <span>{formatDate(item.createdAt)}</span>
        <span className="rounded bg-zinc-700 px-1.5 py-0.5 text-xs text-zinc-300">
          댓글
        </span>
      </div>
      <div className="text-xs text-muted-foreground">
        <HighlightMatch text={item.snippet} query={query} />
      </div>
    </div>
  );
}

function Pagination({
  current,
  total,
  onChange,
}: {
  current: number;
  total: number;
  onChange: (page: number) => void;
}) {
  const pages = Array.from({ length: Math.min(total, 10) }, (_, i) => i);
  return (
    <div className="mt-6 flex flex-wrap items-center justify-center gap-2">
      <button
        disabled={current === 0}
        onClick={() => onChange(current - 1)}
        className="border border-border px-3 py-1 font-mono text-xs text-muted-foreground transition-all hover:border-zinc-500 hover:text-foreground disabled:opacity-40"
      >
        이전
      </button>
      {pages.map((i) => (
        <button
          key={i}
          onClick={() => onChange(i)}
          className={`border px-3 py-1 font-mono text-xs transition-all ${
            i === current
              ? 'border-[hsl(var(--neon))] text-[hsl(var(--neon))]'
              : 'border-border text-muted-foreground hover:border-zinc-500 hover:text-foreground'
          }`}
        >
          {i + 1}
        </button>
      ))}
      <button
        disabled={current === total - 1}
        onClick={() => onChange(current + 1)}
        className="border border-border px-3 py-1 font-mono text-xs text-muted-foreground transition-all hover:border-zinc-500 hover:text-foreground disabled:opacity-40"
      >
        다음
      </button>
    </div>
  );
}

export default function SearchView() {
  const router = useRouter();
  const searchParams = useSearchParams();

  const [inputQuery, setInputQuery] = useState('');
  const [appliedQuery, setAppliedQuery] = useState('');
  const [type, setType] = useState<SearchType>('all');
  const [tag, setTag] = useState<string | null>(null);
  const [sort, setSort] = useState<SearchSort>('relevance');
  const [page, setPage] = useState(0);
  const [results, setResults] = useState<PageResponse<SearchResult> | null>(
    null
  );
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [elapsed, setElapsed] = useState<number | null>(null);
  const [counts, setCounts] = useState<SearchCounts | null>(null);

  // 드롭다운 관련 상태
  const [focused, setFocused] = useState(false);
  const [suggestions, setSuggestions] = useState<string[]>([]);
  const [recent, setRecent] = useState<string[]>([]);
  const [activeIndex, setActiveIndex] = useState(-1);
  const blurTimerRef = useRef<ReturnType<typeof setTimeout> | null>(null);

  // 마운트 시 최근검색어 로드
  useEffect(() => {
    setRecent(getRecentSearches());
  }, []);

  // URL 파라미터로 초기화 (마운트 시 1회)
  useEffect(() => {
    const queryParam = searchParams.get('query');
    const typeParam = searchParams.get('type') as SearchType | null;
    const tagParam = searchParams.get('tag');
    const sortParam = searchParams.get('sort') as SearchSort | null;
    if (queryParam) {
      const resolvedType =
        typeParam && ['all', 'post', 'comment'].includes(typeParam) ? typeParam : 'all';
      const resolvedSort =
        sortParam && ['relevance', 'latest', 'comments'].includes(sortParam) ? sortParam : 'relevance';
      setInputQuery(queryParam);
      if (resolvedType !== 'all') setType(resolvedType);
      if (tagParam) setTag(tagParam);
      if (resolvedSort !== 'relevance') setSort(resolvedSort);
      doSearch(queryParam, resolvedType, tagParam, resolvedSort, 0);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const doSearch = useCallback(
    async (
      q: string,
      t: SearchType,
      tg: string | null,
      s: SearchSort,
      p: number,
      opts?: { remember?: boolean }
    ) => {
      if (!q.trim()) return;
      setLoading(true);
      setError(null);
      const start = performance.now();
      try {
        const data = await searchPosts({
          query: q,
          type: t,
          tag: tg ?? undefined,
          sort: s,
          page: p,
        });
        setResults(data);
        setAppliedQuery(q);
        setElapsed((performance.now() - start) / 1000);
        if (opts?.remember) {
          setRecent(addRecentSearch(q));
        }
        // URL 갱신
        const qs = new URLSearchParams();
        qs.set('query', q);
        if (t !== 'all') qs.set('type', t);
        if (tg) qs.set('tag', tg);
        if (s !== 'relevance') qs.set('sort', s);
        router.replace(`/search?${qs}`, { scroll: false });
        // 결과 개수 배지 (비동기, 결과 블로킹 안 함)
        getSearchCounts({ query: q, tag: tg ?? undefined })
          .then(setCounts)
          .catch(() => {});
      } catch (err) {
        setError(
          err instanceof Error ? err.message : '검색 중 오류가 발생했습니다.'
        );
      } finally {
        setLoading(false);
      }
    },
    [router]
  );

  // 입력이 비면 결과 초기화 (검색 트리거 아님)
  useEffect(() => {
    if (!inputQuery.trim()) {
      setResults(null);
      setAppliedQuery('');
      setElapsed(null);
      setCounts(null);
    }
  }, [inputQuery]);

  // 검색 후 필터/정렬 변경 시 재검색
  useEffect(() => {
    if (!appliedQuery) return;
    setPage(0);
    doSearch(appliedQuery, type, tag, sort, 0);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [type, tag, sort]);

  // suggest 디바운스 (200ms)
  useEffect(() => {
    if (!inputQuery.trim()) {
      setSuggestions([]);
      return;
    }
    const handle = setTimeout(() => {
      suggest(inputQuery)
        .then(setSuggestions)
        .catch(() => setSuggestions([]));
    }, 200);
    return () => clearTimeout(handle);
  }, [inputQuery]);

  const handleSearch = () => {
    const q = inputQuery.trim();
    if (!q) return;
    setPage(0);
    doSearch(q, type, tag, sort, 0, { remember: true });
  };

  const handleKeyDown = (e: KeyboardEvent<HTMLInputElement>) => {
    const dropMode = inputQuery.trim() ? 'typing' : 'idle';
    const listLen = dropMode === 'typing' ? suggestions.length : recent.length;

    if (e.key === 'ArrowDown') {
      e.preventDefault();
      setActiveIndex((i) => (listLen === 0 ? -1 : (i + 1) % listLen));
    } else if (e.key === 'ArrowUp') {
      e.preventDefault();
      setActiveIndex((i) => (i <= 0 ? -1 : i - 1));
    } else if (e.key === 'Enter') {
      if (activeIndex >= 0) {
        const list = dropMode === 'typing' ? suggestions : recent;
        if (list[activeIndex]) {
          handlePick(list[activeIndex]);
          return;
        }
      }
      handleSearch();
    } else if (e.key === 'Escape') {
      setFocused(false);
      setActiveIndex(-1);
    }
  };

  const handleTypeChange = (t: SearchType) => setType(t);
  const handleTagChange = (tg: string | null) => setTag(tg);
  const handleSortChange = (s: SearchSort) => setSort(s);

  const handlePageChange = (p: number) => {
    setPage(p);
    doSearch(appliedQuery, type, tag, sort, p);
  };

  const handleFocus = () => {
    if (blurTimerRef.current) clearTimeout(blurTimerRef.current);
    setFocused(true);
  };

  const handleBlur = () => {
    blurTimerRef.current = setTimeout(() => setFocused(false), 120);
  };

  const handlePick = (q: string) => {
    setInputQuery(q);
    setFocused(false);
    setActiveIndex(-1);
    setPage(0);
    doSearch(q, type, tag, sort, 0, { remember: true });
  };

  const handleRemoveRecent = (q: string) => {
    setRecent(removeRecentSearch(q));
  };

  const handleClearRecent = () => {
    clearRecentSearches();
    setRecent([]);
  };

  const dropdownMode = inputQuery.trim() ? 'typing' : 'idle';
  const showDropdown =
    focused &&
    (dropdownMode === 'typing' ? suggestions.length > 0 : recent.length > 0);

  return (
    <div className="mx-auto max-w-3xl px-4 py-8">
      <h1 className="mb-6 font-mono text-2xl font-bold tracking-[0.3em] text-[hsl(var(--neon))]">
        [ 수 색 ]
      </h1>

      <div
        data-testid="search-controls"
        className="sticky top-0 z-20 bg-background pb-2 pt-2"
      >
        <div className="relative z-30 mb-6 flex gap-2">
          <input
            type="text"
            value={inputQuery}
            onChange={(e) => {
              setInputQuery(e.target.value);
              setActiveIndex(-1);
            }}
            onKeyDown={handleKeyDown}
            onFocus={handleFocus}
            onBlur={handleBlur}
            autoFocus
            placeholder="키워드, 닉네임, 태그..."
            className="flex-1 rounded border border-border bg-card px-4 py-2 font-mono text-sm text-foreground placeholder:text-muted-foreground focus:outline-none focus:ring-1 focus:ring-[hsl(var(--neon))]"
          />
          <button
            onClick={handleSearch}
            disabled={loading}
            className="border border-border bg-card px-4 py-2 font-mono text-sm text-foreground transition-all hover:border-[hsl(var(--neon))] hover:text-[hsl(var(--neon))] disabled:opacity-50"
          >
            🔍
          </button>
          {showDropdown && (
            <SearchSuggestions
              mode={dropdownMode}
              recent={recent}
              suggestions={suggestions}
              activeIndex={activeIndex}
              onPick={handlePick}
              onRemoveRecent={handleRemoveRecent}
              onClearRecent={handleClearRecent}
              query={inputQuery}
            />
          )}
        </div>

        {/* 타입 탭 */}
        <div className="mb-4 flex gap-2">
          {TYPE_TABS.map((tab) => {
            const cnt = tabCount(tab.value, counts);
            return (
              <button
                key={tab.value}
                onClick={() => handleTypeChange(tab.value)}
                className={`border px-3 py-1 font-mono text-xs transition-all ${
                  type === tab.value
                    ? 'border-[hsl(var(--neon))] text-[hsl(var(--neon))]'
                    : 'border-border text-muted-foreground hover:border-zinc-500 hover:text-foreground'
                }`}
              >
                {tab.label}
                {cnt > 0 && (
                  <span className="ml-1 rounded bg-zinc-700 px-1 py-0.5 font-mono text-xs">
                    {cnt}
                  </span>
                )}
              </button>
            );
          })}
        </div>

        {/* 태그 필터 칩 — 미선택 시 자기 색 + opacity-50 */}
        <div className="mb-4 flex flex-wrap gap-2">
          <button
            onClick={() => handleTagChange(null)}
            className={`rounded px-2 py-0.5 font-mono text-xs transition-all ${
              tag === null
                ? 'bg-zinc-600 text-white'
                : 'bg-zinc-800 text-zinc-400 hover:bg-zinc-700'
            }`}
          >
            전체
          </button>
          {TAGS.map((t) => (
            <button
              key={t}
              onClick={() => handleTagChange(t)}
              className={`rounded px-2 py-0.5 font-mono text-xs transition-all ${
                tag === t
                  ? TAG_STYLES[t]
                  : `${TAG_STYLES[t]} opacity-50 hover:opacity-80`
              }`}
            >
              {t}
            </button>
          ))}
        </div>

        <div className="mb-6 flex gap-4 border-b border-border pb-3">
          {SORT_OPTIONS.map((opt) => (
            <button
              key={opt.value}
              onClick={() => handleSortChange(opt.value)}
              className={`font-mono text-xs transition-all ${
                sort === opt.value
                  ? 'text-[hsl(var(--neon))]'
                  : 'text-muted-foreground hover:text-foreground'
              }`}
            >
              {opt.label}
            </button>
          ))}
        </div>
      </div>

      {loading && (
        <div className="py-12 text-center font-mono text-sm text-muted-foreground">
          검색 중...
        </div>
      )}

      {error && (
        <div className="py-8 text-center font-mono text-sm text-red-400">
          {error}
        </div>
      )}

      {!loading && !error && results && (
        <>
          <div className="mb-4 font-mono text-xs text-muted-foreground">
            &quot;{appliedQuery}&quot; 검색 결과 {results.totalElements}건
            {elapsed !== null && ` · ${elapsed.toFixed(2)}초`}
          </div>

          {results.content.length === 0 ? (
            <EmptyState recent={recent} onPick={handlePick} />
          ) : (
            <div className="flex flex-col gap-3">
              {results.content.map((item, idx) =>
                item.type === 'POST' ? (
                  <PostCard
                    key={`POST-${idx}`}
                    id={item.id ?? ''}
                    title={item.title ?? ''}
                    content={item.snippet}
                    tag={item.tag ?? undefined}
                    timestamp={item.createdAt}
                    username={item.nickName}
                    commentCount={item.commentCount}
                  />
                ) : (
                  <CommentResultCard
                    key={`COMMENT-${idx}`}
                    item={item}
                    query={appliedQuery}
                  />
                )
              )}
            </div>
          )}

          {results.totalPages > 1 && (
            <Pagination
              current={page}
              total={results.totalPages}
              onChange={handlePageChange}
            />
          )}
        </>
      )}

      {!loading && !error && !results && (
        <div className="py-12 text-center font-mono text-sm text-muted-foreground">
          검색어를 입력하세요.
        </div>
      )}
    </div>
  );
}

function EmptyState({
  recent,
  onPick,
}: {
  recent: string[];
  onPick: (q: string) => void;
}) {
  return (
    <div className="py-12 text-center">
      <div className="mb-2 font-mono text-2xl">🔍</div>
      <div className="mb-1 font-mono text-sm text-muted-foreground">
        검색 결과가 없습니다.
      </div>
      <div className="font-mono text-xs text-muted-foreground">
        다른 키워드로 검색해보세요
      </div>
      {recent.length > 0 && (
        <div className="mt-4 flex flex-wrap items-center justify-center gap-2">
          {recent.map((r) => (
            <button
              key={r}
              onClick={() => onPick(r)}
              className="rounded border border-border px-2 py-1 font-mono text-xs text-muted-foreground hover:text-foreground"
            >
              {r}
            </button>
          ))}
        </div>
      )}
    </div>
  );
}
