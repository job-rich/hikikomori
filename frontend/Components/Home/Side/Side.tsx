'use client';

import Popularity from './Component/Popularity/Popularity';
import TagFilter from './Component/TagFilter/TagFilter';

export default function Side() {
  return (
    <aside className="hidden lg:block w-64 shrink-0 mt-[56px]">
      <div className="sticky  flex flex-col gap-5">
        <Popularity />
        <TagFilter />
      </div>
    </aside>
  );
}
