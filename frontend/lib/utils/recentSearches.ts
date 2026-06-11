export const RECENT_SEARCHES_KEY = 'hikikomori-recent-searches';
const MAX_RECENT = 8;

export function getRecentSearches(): string[] {
  if (typeof window === 'undefined') return [];
  try {
    const raw = localStorage.getItem(RECENT_SEARCHES_KEY);
    if (!raw) return [];
    return JSON.parse(raw) as string[];
  } catch {
    return [];
  }
}

export function addRecentSearch(q: string): string[] {
  if (typeof window === 'undefined') return [];
  const trimmed = q.trim();
  if (!trimmed) return getRecentSearches();
  const current = getRecentSearches().filter((s) => s !== trimmed);
  const next = [trimmed, ...current].slice(0, MAX_RECENT);
  localStorage.setItem(RECENT_SEARCHES_KEY, JSON.stringify(next));
  return next;
}

export function removeRecentSearch(q: string): string[] {
  if (typeof window === 'undefined') return [];
  const next = getRecentSearches().filter((s) => s !== q);
  localStorage.setItem(RECENT_SEARCHES_KEY, JSON.stringify(next));
  return next;
}

export function clearRecentSearches(): void {
  if (typeof window === 'undefined') return;
  localStorage.removeItem(RECENT_SEARCHES_KEY);
}
