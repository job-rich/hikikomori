'use client';

import { useUserStore } from '@/lib/stores/userStore';
import './navbar-title.css';

const TITLE = '방구석 철학자 ';

export default function Navbar() {
  const { nickname, openNicknameModal } = useUserStore();

  return (
    <header className="sticky top-0 z-50 border-b border-border bg-background/90 backdrop-blur-sm">
      <div className="mx-auto flex max-w-5xl items-center justify-between px-4 py-3">
        <div className="flex items-center gap-4">
          <div
            className="navbar-title-static text-xl font-bold tracking-[0.3em] uppercase"
            data-text={TITLE}
          >
            {TITLE}
          </div>
        </div>
        <button
          onClick={openNicknameModal}
          className={`cursor-pointer border px-3 py-1.5 font-mono text-xs tracking-wider transition-all ${
            nickname
              ? 'border-zinc-700 text-zinc-400 hover:border-zinc-500 hover:text-zinc-200'
              : 'animate-pulse border-red-800/60 text-red-400 hover:border-red-600 hover:text-red-300'
          }`}
        >
          {nickname ?? '닉네임을 생성하세요'}
        </button>
      </div>
    </header>
  );
}
