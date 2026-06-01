'use client';

import Popularity from './Component/Popularity/Popularity';

export default function Side() {
  return (
    <aside className="hidden lg:block w-64 shrink-0">
      <div className="sticky  flex flex-col gap-3">
        <Popularity />
      </div>
    </aside>
  );
}
