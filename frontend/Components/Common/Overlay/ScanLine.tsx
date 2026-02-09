'use client';

import { useEffect, useState } from 'react';

const SUBLIMINAL_TEXTS = [
  'you are being watched',
  'this is not a dream',
  'wake up',
  'do you remember?',
  'it knows your name',
  'look behind you',
  'H E L P',
  'you were here before',
  'the exit does not exist',
  'DONT LOOK UP',
  'it followed you home',
  'wrong timeline',
  'you chose this',
];

export function ScanlineOverlay() {
  const [subliminal, setSubliminal] = useState<string | null>(null);
  const [subliminalSize, setSubliminalSize] = useState('text-4xl');
  const [screenFlash, setScreenFlash] = useState(false);

  // Subliminal text flashes
  useEffect(() => {
    const interval = setInterval(
      () => {
        if (Math.random() > 0.5) {
          const text =
            SUBLIMINAL_TEXTS[
              Math.floor(Math.random() * SUBLIMINAL_TEXTS.length)
            ];
          const sizes = ['text-2xl', 'text-4xl', 'text-6xl', 'text-8xl'];
          setSubliminalSize(sizes[Math.floor(Math.random() * sizes.length)]);
          setSubliminal(text);
          setTimeout(() => setSubliminal(null), 60 + Math.random() * 120);
        }
      },
      5000 + Math.random() * 5000
    );
    return () => clearInterval(interval);
  }, []);

  // Random screen flash (very rare)
  useEffect(() => {
    const interval = setInterval(
      () => {
        if (Math.random() > 0.85) {
          setScreenFlash(true);
          setTimeout(() => setScreenFlash(false), 50);
        }
      },
      15000 + Math.random() * 15000
    );
    return () => clearInterval(interval);
  }, []);

  return (
    <div
      className="pointer-events-none fixed inset-0 z-[998] overflow-hidden"
      aria-hidden="true"
    >
      {/* Primary scanline */}
      <div className="animate-scanline-thick absolute left-0 w-full h-[4px] bg-neon/20 blur-[2px]" />

      {/* Secondary fast scanline */}
      <div
        className="animate-scanline absolute left-0 w-full h-px bg-glitch/10"
        style={{ animationDuration: '3s' }}
      />

      {/* Third even faster scanline */}
      <div
        className="animate-scanline absolute left-0 w-full h-[2px] bg-destructive/5"
        style={{ animationDuration: '2s', animationDelay: '1s' }}
      />

      {/* Horizontal interference lines */}
      <div
        className="absolute left-0 w-full h-[2px] bg-neon/[0.04] animate-drift"
        style={{ top: '25%' }}
      />
      <div
        className="absolute left-0 w-full h-[1px] bg-glitch/[0.06] animate-drift"
        style={{ top: '55%', animationDelay: '3s' }}
      />
      <div
        className="absolute left-0 w-full h-[3px] bg-destructive/[0.03] animate-drift"
        style={{ top: '78%', animationDelay: '1.5s' }}
      />

      {/* Subliminal text flash */}
      {subliminal && (
        <div className="absolute inset-0 flex items-center justify-center">
          <span
            className={`${subliminalSize} font-bold font-mono text-foreground/[0.04] tracking-[0.5em] uppercase select-none`}
            style={{
              textShadow: '0 0 60px hsl(var(--foreground) / 0.05)',
              transform: `rotate(${Math.random() * 6 - 3}deg)`,
            }}
          >
            {subliminal}
          </span>
        </div>
      )}

      {/* Screen flash */}
      {screenFlash && <div className="absolute inset-0 bg-foreground/10" />}

      {/* Heavy vignette */}
      <div
        className="absolute inset-0"
        style={{
          background: `radial-gradient(ellipse at center, transparent 35%, hsl(var(--background) / 0.6) 70%, hsl(var(--background) / 0.95) 100%)`,
        }}
      />

      {/* Corner darkness overlays */}
      <div
        className="absolute inset-0"
        style={{
          background: `
            radial-gradient(circle at 0% 0%, hsl(var(--background) / 0.9) 0%, transparent 25%),
            radial-gradient(circle at 100% 0%, hsl(var(--background) / 0.9) 0%, transparent 25%),
            radial-gradient(circle at 0% 100%, hsl(var(--background) / 0.9) 0%, transparent 25%),
            radial-gradient(circle at 100% 100%, hsl(var(--background) / 0.9) 0%, transparent 25%)
          `,
        }}
      />

      {/* Vertical color aberration lines at edges */}
      <div
        className="absolute top-0 bottom-0 left-[2%] w-px animate-flicker"
        style={{ background: 'hsl(var(--neon) / 0.03)' }}
      />
      <div
        className="absolute top-0 bottom-0 right-[3%] w-px animate-flicker"
        style={{
          background: 'hsl(var(--glitch) / 0.03)',
          animationDelay: '2s',
        }}
      />
    </div>
  );
}
