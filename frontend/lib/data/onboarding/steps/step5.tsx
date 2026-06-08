import type { ReactNode } from 'react';
import type { OnboardingStep } from '@/lib/types/onboarding';
import Highlight from '@/Components/Common/Highlight/Highlight';

const HALL_OF_FAME: Array<{
  rank: number;
  title: ReactNode;
  meta: string;
}> = [
  {
    rank: 1,
    title: '"존재는 본질에 선행한다 — 사르트르는 틀렸다"',
    meta: '👍 847 · 💬 312 · 2026.03.15',
  },
  {
    rank: 2,
    title: '"정의란 강자의 이익이 아니라 약자의 권리"',
    meta: '👍 623 · 💬 198 · 2026.03.14',
  },
  {
    rank: 3,
    title: '"AI는 철학할 수 있는가? 불가능의 증명"',
    meta: '👍 551 · 💬 267 · 2026.03.13',
  },
];

export const step5: OnboardingStep = {
  fileNumber: 'FILE NO. 005',
  clearance: 'CLEARANCE: LEVEL 4 — FINAL',
  badge: 'STEP 5 / 5 — FINAL',
  icon: '🏆',
  titleKorean: '영원히 기억되는 법',
  subtitleEnglish: 'HALL OF ETERNAL GLORY',
  intro: (
    <>
      <p className="onb-body-paragraph">
        자정의 소각에서도 살아남는 방법이 딱 하나 있다. 바로{' '}
        <Highlight color="#f59e0b">명예의 전당</Highlight>에 오르는 것이다.
      </p>
      <p className="onb-body-paragraph">
        매일 가장 높은 추천을 받은 글은 소각 대상에서 제외되어{' '}
        <Highlight>영구 보존</Highlight>된다. 당신의 사유가 충분히 강렬하다면,
        시간마저 이길 수 있다.
      </p>
    </>
  ),
  body: (
    <>
      <ul className="onb-hall">
        {HALL_OF_FAME.map((entry) => (
          <li key={entry.rank} className="onb-hall-item">
            <span className="onb-hall-rank">{entry.rank}</span>
            <span className="onb-hall-body">
              <span className="onb-hall-title">{entry.title}</span>
              <span className="onb-hall-meta">{entry.meta}</span>
            </span>
            <span className="onb-hall-tag">영구 보존</span>
          </li>
        ))}
      </ul>
      <div className="onb-warning">
        ⚠ 명예의 전당 선정 기준은 일일 최다 추천입니다. 양질의 사유를
        펼치십시오.
      </div>
    </>
  ),
};
