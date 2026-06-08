'use client';

import Modal from '@/Components/Common/Modal/Modal';

interface CreateProps {
  open: boolean;
  setCreateOpen: (open: boolean) => void;
}

export default function Create({ open, setCreateOpen }: CreateProps) {
  return (
    <Modal open={open} onClose={() => setCreateOpen(false)} ariaLabel="글 추가">
      <div
        className="mx-auto flex h-72 w-72 items-center justify-center border border-green-500 bg-green-500 text-white"
        onClick={() => setCreateOpen(false)}
      >
        글 추가
      </div>
    </Modal>
  );
}
