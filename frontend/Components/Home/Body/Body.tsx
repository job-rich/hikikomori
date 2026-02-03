'use client';
import Button from '@/Components/Common/Button/Button';
import Create from '@/Components/Common/Modals/Create';
import { useState } from 'react';
export default function Body() {
  const [createOpen, setCreateOpen] = useState(false);
  const [deleteOpen, setDeleteOpen] = useState(false);
  return (
    <>
      <div className="flex min-h-screen items-center justify-center bg-zinc-50 font-sans dark:bg-black">
        <main className="flex min-h-screen border border-red-500 w-full max-w-3xl flex-col items-center justify-between py-32 px-16 bg-white dark:bg-black sm:items-start">
          <div>
            <Button
              type={'create'}
              setCreateOpen={setCreateOpen}
              setDeleteOpen={setDeleteOpen}
            />
          </div>
        </main>
      </div>
      {createOpen && <Create setCreateOpen={setCreateOpen} />}
    </>
  );
}
