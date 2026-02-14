'use client';

import { useEffect, useState } from 'react';

// 타이틀 잔상 처리 유니코드 문자열 정리한거 (위 아래 좌 우 에 대한 문자열 코드)
const ZALGO_CHARS = [
  '\u0300',
  '\u0301',
  '\u0302',
  '\u0303',
  '\u0304',
  '\u0305',
  '\u0306',
  '\u0307',
  '\u0308',
  '\u0309',
  '\u030A',
  '\u030B',
  '\u030C',
  '\u030D',
  '\u030E',
  '\u030F',
  '\u0310',
  '\u0311',
  '\u0312',
  '\u0313',
  '\u0314',
  '\u0315',
  '\u031A',
  '\u031B',
  '\u033D',
  '\u033E',
  '\u033F',
  '\u0340',
  '\u0341',
  '\u0342',
  '\u0343',
  '\u0344',
  '\u0346',
  '\u034A',
  '\u034B',
  '\u034C',
];

function addZalgo(text: string, intensity: number = 3): string {
  return text
    .split('')
    .map((char) => {
      let result = char;
      for (let i = 0; i < intensity; i++) {
        result += ZALGO_CHARS[Math.floor(Math.random() * ZALGO_CHARS.length)];
      }
      return result;
    })
    .join('');
}

export default function Navbar() {
  const [isTitle, setIsTitle] = useState(false);
  const [zalgoTitle, setZalgoTitle] = useState('방구석 철학자 ');

  useEffect(() => {
    const doGlitch = () => {
      setIsTitle(true);
      setZalgoTitle(addZalgo('방구석 철학자 ', 5));
      setTimeout(
        () => {
          setIsTitle(false);
          setZalgoTitle('방구석 철학자 ');
        },
        150 + Math.random() * 350
      );
    };

    // 2-6초 간격 임의로 설정 => 변경해도 됨
    const interval = setInterval(doGlitch, Math.random() * 4000 + 2000);
    return () => clearInterval(interval);
  }, []);

  return (
    <header className="sticky top-0 z-50 border-b border-border bg-background/90 backdrop-blur-sm">
      <div className="mx-auto flex max-w-5xl items-center justify-between px-4 py-3">
        <div className="flex items-center gap-4">
          <div className="relative">
            <div
              className={`glitch-text-heavy text-xl font-bold tracking-[0.3em] uppercase ${isTitle ? 'animate-vhs-tracking' : ''}`}
              data-text={isTitle ? zalgoTitle : '방구석 철학자 '}
              style={{
                color: isTitle ? 'hsl(var(--destructive))' : 'hsl(var(--neon))',
                textShadow: isTitle
                  ? '0 0 15px hsl(var(--destructive)), 3px 0 hsl(var(--neon) / 0.8), -3px 0 hsl(var(--glitch) / 0.8)'
                  : '0 0 10px hsl(var(--neon) / 0.5)',
              }}
            >
              {isTitle ? zalgoTitle : '방구석 철학자 '}
            </div>
          </div>
        </div>
      </div>
    </header>
  );
}
