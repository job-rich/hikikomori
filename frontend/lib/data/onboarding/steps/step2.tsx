import type { OnboardingStep } from '@/lib/types/onboarding';
import Highlight from '@/Components/Common/Highlight/Highlight';

const RULE_DEFINITIONS: Array<{
  number: string;
  title: string;
  description: string;
  accentColor: '#ef4444' | '#f59e0b';
}> = [
  {
    number: '01',
    title: '완전한 익명',
    description:
      ' — 개인정보 노출 금지. 이곳에 이름은 없다. 오직 사유만이 존재한다.',
    accentColor: '#ef4444',
  },
  {
    number: '02',
    title: '자정의 소각',
    description:
      ' — 매일 00:00, 전일의 모든 글과 댓글이 완전 삭제된다. 어떤 기록도 남지 않는다.',
    accentColor: '#f59e0b',
  },
  {
    number: '03',
    title: '논리로 싸워라',
    description:
      ' — 인신공격, 혐오 발언은 즉시 삭제. 상대의 논리를 공격하되, 인격은 건드리지 마라.',
    accentColor: '#ef4444',
  },
  {
    number: '04',
    title: '도망은 기록된다',
    description:
      ' — 글을 삭제하면 도주율이 올라간다. 자신의 주장에 책임을 져라.',
    accentColor: '#f59e0b',
  },
  {
    number: '05',
    title: '한 줄 감상 금지',
    description:
      ' — 최소한의 논거를 제시하라. 깊이 없는 글은 투기장의 수준을 떨어뜨린다.',
    accentColor: '#ef4444',
  },
];

export const step2: OnboardingStep = {
  fileNumber: 'FILE NO. 002',
  clearance: 'CLEARANCE: LEVEL 1',
  badge: 'STEP 2 / 5',
  icon: '⚔️',
  titleKorean: '투기장의 규칙',
  subtitleEnglish: 'RULES OF THE ARENA',
  intro: (
    <p className="onb-body-paragraph">
      이 투기장에는 반드시 지켜야 할 규칙이 있다. 위반 시{' '}
      <Highlight>영구 추방</Highlight>될 수 있다.
    </p>
  ),
  body: (
    <ul className="onb-list">
      {RULE_DEFINITIONS.map((rule) => (
        <li key={rule.number} className="onb-list-item">
          <span className="onb-list-num">{rule.number}</span>
          <span className="onb-list-text">
            <Highlight color={rule.accentColor}>{rule.title}</Highlight>
            <span>{rule.description}</span>
          </span>
        </li>
      ))}
    </ul>
  ),
};
