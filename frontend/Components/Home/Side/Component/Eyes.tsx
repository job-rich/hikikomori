'use client';

import { useEffect, useState } from 'react';

export default function Eyes() {
  const [eyeOpen, setEyeOpen] = useState(true);

  useEffect(() => {
    const interval = setInterval(
      () => {
        setEyeOpen(false);
        setTimeout(() => setEyeOpen(true), 150 + Math.random() * 200);
      },
      3000 + Math.random() * 4000
    );
    return () => clearInterval(interval);
  }, []);

  return (
    <div className="border border-border bg-card p-4 flex flex-col items-center gap-2 relative overflow-hidden">
      <div className="relative w-16 h-10 flex items-center justify-center">
        <div
          className="relative transition-all duration-200"
          style={{
            width: '48px',
            height: eyeOpen ? '24px' : '2px',
            borderRadius: '50%',
            border: '1.5px solid hsl(var(--neon) / 0.6)',
            overflow: 'hidden',
            transition: 'height 0.15s ease',
          }}
        >
          {eyeOpen && (
            <div className="absolute inset-0 flex items-center justify-center">
              <div
                className="w-3 h-3 rounded-full bg-red-500 animate-eye-look"
                style={{ boxShadow: '0 0 12px hsl(var(--neon) / 0.6)' }}
              >
                <div className="w-1.5 h-1.5 rounded-full bg-background mx-auto mt-[3px]" />
              </div>
            </div>
          )}
        </div>
      </div>

      <div
        className="absolute inset-0 pointer-events-none animate-breathe"
        style={{
          background:
            'radial-gradient(circle at center, hsl(var(--neon) / 0.03) 0%, transparent 60%)',
        }}
      />
    </div>
  );
}
