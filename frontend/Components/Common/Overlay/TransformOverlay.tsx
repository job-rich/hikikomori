'use client';

import { useEffect, useState } from 'react';

export function TransformOverlay() {
  const [activeTransform, setActiveTransform] = useState<string | null>(null);
  const [tearPos, setTearPos] = useState(40);

  useEffect(() => {
    const glitchTypes = ['tear', 'invert', 'static', 'shift', 'zoom'];

    const trigger = () => {
      // 딜레이 생성 (1~3초 사이에서 랜덤으로) => 주기 조절 가능
      const delay = 1000 + Math.random() * 2000;
      setTimeout(() => {
        // 위에 타입 랜덤
        const type =
          glitchTypes[Math.floor(Math.random() * glitchTypes.length)];
        // tear 위치 스타일 수치 (중간 어딘가 보여야 하기 때문)
        setTearPos(20 + Math.random() * 60);
        setActiveTransform(type);
        //(0.1~0.35초) 후 비활성화
        setTimeout(() => setActiveTransform(null), 100 + Math.random() * 250);
        // 다음 효과 예약
        trigger();
      }, delay);
    };

    trigger();
  }, []);

  if (!activeTransform) return null;

  return (
    <div
      className="pointer-events-none fixed inset-0 z-[995]"
      aria-hidden="true"
    >
      {/* tear => 화면이 찢어진 듯한 수평 이펙트 */}
      {activeTransform === 'tear' && (
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

      {/* invert => 색상을 반전시키는 효과 */}
      {activeTransform === 'invert' && (
        <div
          className="absolute inset-0"
          style={{ backdropFilter: 'invert(0.8) hue-rotate(180deg)' }}
        />
      )}

      {/* static => TV 노이즈처럼 화면이 흐트러지는 효과 */}
      {activeTransform === 'static' && (
        <div
          className="absolute inset-0 animate-static-noise"
          style={{
            // 못 만들어서 퍼옴,,
            backgroundImage: `url("data:image/svg+xml,%3Csvg viewBox='0 0 256 256' xmlns='http://www.w3.org/2000/svg'%3E%3Cfilter id='n'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='0.9' numOctaves='3' stitchTiles='stitch'/%3E%3C/filter%3E%3Crect width='100%25' height='100%25' filter='url(%23n)' opacity='0.3'/%3E%3C/svg%3E")`,
            opacity: 0.9,
          }}
        />
      )}

      {/* shift => 화면 일부가 좌우로 살짝 어긋나는 효과 */}
      {activeTransform === 'shift' && (
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

      {/* zoom => 화면이 순식간에 확대/축소되는 효과 */}
      {activeTransform === 'zoom' && (
        <div
          className="absolute inset-0"
          style={{
            backdropFilter: 'blur(5px)',
            background:
              'radial-gradient(circle at center, transparent 30%, hsl(var(--background) / 0.8) 100%)',
          }}
        />
      )}
    </div>
  );
}
