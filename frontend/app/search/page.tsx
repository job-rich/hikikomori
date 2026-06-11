import { Suspense } from 'react';
import type { Metadata } from 'next';
import SearchView from '@/Components/Search/SearchView';

export const metadata: Metadata = {
  robots: { index: false, follow: false },
};

export default function SearchPage() {
  return (
    <Suspense
      fallback={
        <div className="py-12 text-center font-mono text-sm text-muted-foreground">
          로딩 중...
        </div>
      }
    >
      <SearchView />
    </Suspense>
  );
}
