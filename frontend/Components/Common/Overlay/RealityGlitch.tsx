'use client';

import { useEffect, useState } from 'react';

// Random screen-wide distortion events: screen tear, color inversion, static burst, fake BSOD
export function RealityGlitch() {
  const [activeGlitch, setActiveGlitch] = useState<string | null>(null);
  const [tearPos, setTearPos] = useState(40);

  useEffect(() => {
    const glitchTypes = ['tear', 'invert', 'static', 'shift', 'zoom'];

    const trigger = () => {
      const delay = 2000 + Math.random() * 8000;
      setTimeout(() => {
        const type =
          glitchTypes[Math.floor(Math.random() * glitchTypes.length)];
        setTearPos(20 + Math.random() * 60);
        setActiveGlitch(type);
        setTimeout(() => setActiveGlitch(null), 100 + Math.random() * 250);
        trigger();
      }, delay);
    };
    trigger();
  }, []);

  if (!activeGlitch) return null;

  return (
    <div
      className="pointer-events-none fixed inset-0 z-[995]"
      aria-hidden="true"
    >
      {/* Screen tear - horizontal slice displaced */}
      {activeGlitch === 'tear' && (
        <>
          <div
            className="absolute left-0 right-0 h-[3px] overflow-hidden"
            style={{
              top: `${tearPos}%`,
              background: `linear-gradient(90deg, transparent 10%, hsl(var(--neon) / 0.4) 30%, hsl(var(--glitch) / 0.6) 50%, hsl(var(--destructive) / 0.4) 70%, transparent 90%)`,
            }}
          />
          <div
            className="absolute left-0 right-0 h-[30px] bg-background/50"
            style={{
              top: `${tearPos}%`,
              transform: 'translateX(8px)',
              backdropFilter: 'hue-rotate(90deg) saturate(3)',
            }}
          />
          <div
            className="absolute left-0 right-0 h-[20px] bg-background/30"
            style={{
              top: `${tearPos + 2}%`,
              transform: 'translateX(-12px)',
              backdropFilter: 'hue-rotate(-45deg) brightness(1.5)',
            }}
          />
        </>
      )}

      {/* Color inversion flash */}
      {activeGlitch === 'invert' && (
        <div
          className="absolute inset-0"
          style={{ backdropFilter: 'invert(0.8) hue-rotate(180deg)' }}
        />
      )}

      {/* Static burst */}
      {activeGlitch === 'static' && (
        <div
          className="absolute inset-0 animate-static-noise"
          style={{
            backgroundImage: `url("data:image/svg+xml,%3Csvg viewBox='0 0 256 256' xmlns='http://www.w3.org/2000/svg'%3E%3Cfilter id='n'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='0.9' numOctaves='3' stitchTiles='stitch'/%3E%3C/filter%3E%3Crect width='100%25' height='100%25' filter='url(%23n)' opacity='0.15'/%3E%3C/svg%3E")`,
            opacity: 0.7,
          }}
        />
      )}

      {/* Channel shift - RGB split */}
      {activeGlitch === 'shift' && (
        <>
          <div
            className="absolute inset-0"
            style={{
              backdropFilter: 'none',
              boxShadow:
                'inset 3px 0 0 hsl(var(--neon) / 0.3), inset -3px 0 0 hsl(var(--glitch) / 0.3)',
            }}
          />
        </>
      )}

      {/* Zoom distort */}
      {activeGlitch === 'zoom' && (
        <div
          className="absolute inset-0"
          style={{
            backdropFilter: 'blur(1px)',
            background:
              'radial-gradient(circle at center, transparent 30%, hsl(var(--background) / 0.4) 100%)',
          }}
        />
      )}
    </div>
  );
}
