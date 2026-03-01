'use client';

import { useEffect } from 'react';
import './arena-theme.css';

interface ArenaThemeProps {
  nickname: string;
  onConfirm: () => void;
  onRegenerate: () => void;
  onClose: () => void;
}

export default function ArenaTheme({
  nickname,
  onConfirm,
  onRegenerate,
  onClose,
}: ArenaThemeProps) {
  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      if (e.key === 'Escape') onClose();
    };
    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [onClose]);

  return (
    <div
      className="modal-backdrop modal-enter arena-backdrop arena-flicker"
      role="dialog"
      aria-modal="true"
      onClick={(e) => {
        if (e.target === e.currentTarget) onClose();
      }}
    >
      <div className="arena-scanlines" />
      <div className="arena-vignette" />

      <div className="modal-content modal-content-enter arena-content">
        <div className="arena-caution" />

        <div className="arena-card">
          <div className="arena-stamps">
            <span className="arena-stamp arena-stamp--classified">
              CLASSIFIED
            </span>
            <span className="arena-stamp arena-stamp--restricted">
              RESTRICTED
            </span>
            <span className="arena-stamp arena-stamp--secret">TOP SECRET</span>
          </div>

          <div className="arena-redacted-row">
            <span className="arena-redacted">████████████</span>
            <span className="arena-redacted">██████</span>
            <span className="arena-redacted">████████████████</span>
          </div>

          <div className="arena-divider" />

          <div className="arena-mask">
            <svg viewBox="0 0 200 120" className="arena-mask-icon" fill="none">
              <path
                d="M20 55 Q100 15 180 55 Q170 95 100 105 Q30 95 20 55Z"
                stroke="currentColor"
                strokeWidth="2.5"
              />
              <path d="M55 52 Q75 35 95 52 Q75 65 55 52Z" fill="currentColor" />
              <path
                d="M105 52 Q125 35 145 52 Q125 65 105 52Z"
                fill="currentColor"
              />
            </svg>
          </div>

          <p className="arena-tagline">잠깐이지만, 여기선 누구든 될 수 있다.</p>

          <p className="arena-codename-label">▌ CODENAME ASSIGNMENT ▌</p>

          <div className="arena-nickname">
            <span className="arena-glitch" data-text={nickname}>
              {nickname}
            </span>
          </div>

          <div className="arena-actions">
            <button
              onClick={onConfirm}
              className="arena-btn arena-btn--confirm"
            >
              입장
            </button>
            <button
              onClick={onRegenerate}
              className="arena-btn arena-btn--secondary"
            >
              재배정
            </button>
          </div>

          <div className="arena-divider" />

          <div className="arena-redacted-row arena-redacted-row--end">
            <span className="arena-redacted">██████</span>
            <span className="arena-redacted">████████████████</span>
          </div>

          <div className="arena-footer">
            <span>매일 초기화</span>
            <span className="arena-footer-dot">·</span>
            <span>규칙 따윈 없다</span>
            <span className="arena-footer-dot">·</span>
            <span>투기장</span>
          </div>

          <p className="arena-disclaimer">
            ⚠ 책임은 본인에게 있습니다만, 내일이면 아무도 기억하지 못할겁니다. ⚠
          </p>
        </div>

        <div className="arena-caution" />
      </div>
    </div>
  );
}
