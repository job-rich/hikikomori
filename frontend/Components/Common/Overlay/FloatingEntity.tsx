'use client';

import { useEffect, useState, useCallback } from 'react';

// An eerie floating eye/face that lazily follows your cursor
export function FloatingEntity() {
  const [pos, setPos] = useState({ x: 0, y: 0 });
  const [target, setTarget] = useState({ x: 0, y: 0 });
  const [visible, setVisible] = useState(false);
  const [phase, setPhase] = useState<'watching' | 'approaching' | 'gone'>(
    'watching'
  );
  const [pupilOffset, setPupilOffset] = useState({ x: 0, y: 0 });

  // Lazy follow cursor
  const handleMouseMove = useCallback((e: MouseEvent) => {
    setTarget({ x: e.clientX, y: e.clientY });
    // Pupil direction
    const cx = window.innerWidth / 2;
    const cy = window.innerHeight / 2;
    setPupilOffset({
      x: Math.max(-4, Math.min(4, (e.clientX - cx) / 80)),
      y: Math.max(-3, Math.min(3, (e.clientY - cy) / 80)),
    });
  }, []);

  useEffect(() => {
    window.addEventListener('mousemove', handleMouseMove);
    return () => window.removeEventListener('mousemove', handleMouseMove);
  }, [handleMouseMove]);

  // Lazy follow with delay
  useEffect(() => {
    const interval = setInterval(() => {
      setPos((prev) => ({
        x: prev.x + (target.x - prev.x) * 0.02,
        y: prev.y + (target.y - prev.y) * 0.02,
      }));
    }, 30);
    return () => clearInterval(interval);
  }, [target]);

  // Appear/disappear randomly
  useEffect(() => {
    const cycle = () => {
      const delay = 15000 + Math.random() * 25000;
      setTimeout(() => {
        setVisible(true);
        setPhase('watching');
        // After watching, maybe approach
        setTimeout(
          () => {
            if (Math.random() > 0.5) {
              setPhase('approaching');
              setTimeout(() => {
                setPhase('gone');
                setTimeout(() => setVisible(false), 500);
              }, 3000);
            } else {
              setPhase('gone');
              setTimeout(() => setVisible(false), 500);
            }
          },
          5000 + Math.random() * 5000
        );
        cycle();
      }, delay);
    };
    // First appearance after shorter delay
    setTimeout(() => {
      setVisible(true);
      setPhase('watching');
      setTimeout(() => {
        setPhase('gone');
        setTimeout(() => setVisible(false), 500);
      }, 4000);
      cycle();
    }, 1500);
  }, []);

  if (!visible) return null;

  const offsetX = phase === 'approaching' ? 0 : 120;
  const offsetY = phase === 'approaching' ? 0 : -80;

  return (
    <div
      className="pointer-events-none fixed z-[996] transition-opacity duration-1000"
      style={{
        left: `${pos.x + offsetX}px`,
        top: `${pos.y + offsetY}px`,
        opacity: phase === 'gone' ? 0 : phase === 'approaching' ? 0.6 : 0.15,
        transform: `scale(${phase === 'approaching' ? 1.5 : 1})`,
        transition: 'opacity 1s, transform 2s ease-in-out',
      }}
      aria-hidden="true"
    >
      {/* The entity - an abstract eye/face shape */}
      <div className="relative animate-float-entity">
        {/* Outer glow */}
        <div
          className="absolute -inset-6 rounded-full"
          style={{
            background: `radial-gradient(circle, hsl(var(--neon) / 0.08) 0%, transparent 70%)`,
          }}
        />
        {/* Eye shape */}
        <svg
          width="60"
          height="36"
          viewBox="0 0 60 36"
          className="animate-breathe"
        >
          {/* Eye outline */}
          <path
            d="M2 18 Q15 2 30 2 Q45 2 58 18 Q45 34 30 34 Q15 34 2 18Z"
            fill="none"
            stroke="hsl(var(--neon))"
            strokeWidth="0.8"
            opacity="0.6"
          />
          {/* Inner glow */}
          <circle
            cx={30 + pupilOffset.x}
            cy={18 + pupilOffset.y}
            r="8"
            fill="hsl(var(--neon))"
            opacity="0.15"
          />
          {/* Pupil */}
          <circle
            cx={30 + pupilOffset.x}
            cy={18 + pupilOffset.y}
            r="4"
            fill="hsl(var(--neon))"
            opacity="0.4"
          />
          {/* Pupil center */}
          <circle
            cx={30 + pupilOffset.x}
            cy={18 + pupilOffset.y}
            r="2"
            fill="hsl(var(--background))"
            opacity="0.9"
          />
          {/* Light reflection */}
          <circle
            cx={32 + pupilOffset.x}
            cy={16 + pupilOffset.y}
            r="1"
            fill="hsl(var(--neon))"
            opacity="0.5"
          />
        </svg>
        {/* Tiny text below */}
        {phase === 'approaching' && (
          <div className="absolute -bottom-5 left-1/2 -translate-x-1/2 text-[7px] font-mono text-neon/30 whitespace-nowrap animate-flicker">
            i see you
          </div>
        )}
      </div>
    </div>
  );
}
