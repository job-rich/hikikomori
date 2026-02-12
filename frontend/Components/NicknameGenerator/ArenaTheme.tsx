'use client';

import './arena-theme.css';

interface ArenaThemeProps {
  nickname: string;
  onConfirm: () => void;
  onRegenerate: () => void;
}

export default function ArenaTheme({
  nickname,
  onConfirm,
  onRegenerate,
}: ArenaThemeProps) {
  return (
    <div className="arena-flicker fixed inset-0 flex items-center justify-center bg-[#0a0a0a]">
      <div className="arena-scanlines" />
      <div className="arena-vignette" />

      <div className="relative z-10 mx-4 w-full max-w-lg">
        {/* Caution tape top */}
        <div className="arena-caution mb-1" />

        <div className="arena-card border-2 border-red-900/40 bg-[#0d0d0d] p-8 font-mono">
          {/* Stamps */}
          <div className="mb-3 flex flex-wrap items-center justify-between gap-2">
            <span className="arena-stamp -rotate-3 text-xs tracking-[3px] text-red-500">
              CLASSIFIED
            </span>
            <span className="arena-stamp rotate-1 text-[10px] tracking-[2px] text-red-400/60">
              RESTRICTED
            </span>
            <span className="arena-stamp rotate-3 text-xs tracking-[3px] text-amber-500">
              TOP SECRET
            </span>
          </div>

          {/* Redacted */}
          <div className="mb-4 flex gap-1">
            <span className="arena-redacted text-xs">████████████</span>
            <span className="arena-redacted text-xs">██████</span>
            <span className="arena-redacted text-xs">████████████████</span>
          </div>

          <div className="mb-5 h-px bg-red-900/30" />

          {/* Mask */}
          <div className="mb-3 flex justify-center text-red-500/70">
            <svg viewBox="0 0 200 120" className="h-20 w-32" fill="none">
              <path
                d="M20 55 Q100 15 180 55 Q170 95 100 105 Q30 95 20 55Z"
                stroke="currentColor"
                strokeWidth="2.5"
              />
              <path
                d="M55 52 Q75 35 95 52 Q75 65 55 52Z"
                fill="currentColor"
              />
              <path
                d="M105 52 Q125 35 145 52 Q125 65 105 52Z"
                fill="currentColor"
              />
            </svg>
          </div>

          {/* Tagline */}
          <p className="mb-1 text-center text-xs tracking-[2px] text-zinc-500">
            잠깐이지만, 여기선 누구든 될 수 있다.
          </p>

          {/* Codename label */}
          <p className="mb-2 text-center text-[10px] tracking-[4px] text-red-500/50">
            ▌ CODENAME ASSIGNMENT ▌
          </p>

          {/* Nickname */}
          <div className="mb-8 flex justify-center">
            <span
              className="arena-glitch text-3xl font-bold tracking-wide text-white"
              data-text={nickname}
            >
              {nickname}
            </span>
          </div>

          {/* Buttons */}
          <div className="mb-6 flex justify-center gap-3">
            <button
              onClick={onConfirm}
              className="cursor-pointer border-2 border-red-600/60 bg-red-700 px-8 py-3 font-mono text-sm font-bold tracking-[3px] text-white transition-all hover:bg-red-600 hover:shadow-[0_0_20px_rgba(220,38,38,0.3)]"
            >
              입장
            </button>
            <button
              onClick={onRegenerate}
              className="cursor-pointer border border-zinc-700 bg-transparent px-8 py-3 font-mono text-sm tracking-wider text-zinc-500 transition-all hover:border-zinc-500 hover:bg-zinc-900 hover:text-zinc-300"
            >
              재배정
            </button>
          </div>

          <div className="mb-4 h-px bg-red-900/30" />

          {/* Redacted */}
          <div className="mb-3 flex justify-end gap-1">
            <span className="arena-redacted text-xs">██████</span>
            <span className="arena-redacted text-xs">████████████████</span>
          </div>

          {/* Footer */}
          <div className="mb-1 flex justify-center gap-3 text-[10px] tracking-[2px] text-zinc-600">
            <span>매일 초기화</span>
            <span className="text-red-700">·</span>
            <span>규칙 따윈 없다</span>
            <span className="text-red-700">·</span>
            <span>투기장</span>
          </div>

          <p className="text-center text-[9px] tracking-[1px] text-red-900/60">
            ⚠ 책임은 본인에게 있습니다만, 내일이면 아무도 기억하지 못할겁니다. ⚠
          </p>
        </div>

        {/* Caution tape bottom */}
        <div className="arena-caution mt-1" />
      </div>
    </div>
  );
}
