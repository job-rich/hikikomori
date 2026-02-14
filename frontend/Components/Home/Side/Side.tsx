'use client';

import Eyes from './Component/Eyes';

export default function Side() {
  return (
    <aside className="hidden lg:block w-64 shrink-0">
      <div className="sticky  flex flex-col gap-3">
        <Eyes />
        <Eyes />
        <Eyes />
        <Eyes />
        <Eyes />
        <Eyes />
        <Eyes />
      </div>
    </aside>
  );
}
