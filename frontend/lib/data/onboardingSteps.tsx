import type { OnboardingStep } from '@/lib/types/onboarding';
import Highlight from '@/Components/Common/Highlight/Highlight';

export const TOTAL_STEPS = 5;
export const FINAL_STEP_INDEX = TOTAL_STEPS - 1;

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

const HALL_OF_FAME: Array<{
  rank: number;
  title: React.ReactNode;
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

export const onboardingSteps: OnboardingStep[] = [
  {
    fileNumber: 'FILE NO. 001',
    clearance: 'CLEARANCE: LEVEL 0',
    badge: 'STEP 1 / 5',
    icon: '🏛️',
    titleKorean: '환영한다, 새로운 철학자여',
    subtitleEnglish: 'WELCOME, NEW PHILOSOPHER',
    intro: (
      <>
        <p className="onb-intro-line">
          이곳은 <Highlight>방구석 철학자들</Highlight>의 투기장이다.
        </p>
        <p className="onb-intro-sub">이름을 버리고, 생각만으로 싸우는 곳.</p>
        <p className="onb-intro-line">
          매일 자정, 모든 기록은 <Highlight color="#f59e0b">소멸</Highlight>
          한다.
        </p>
        <p className="onb-intro-sub">오직 강한 사유만이 살아남는다.</p>
      </>
    ),
    body: (
      <>
        <p className="onb-body-paragraph">
          당신은 지금부터 익명의 철학자가 된다. 학벌도, 나이도, 지위도 존재하지
          않는다. 오직 당신의 <Highlight>논리</Highlight>와{' '}
          <Highlight>사유</Highlight>만이 무기가 될 것이다.
        </p>
        <div className="onb-warning">⚠ 이 브리핑은 최초 1회만 제공됩니다.</div>
      </>
    ),
  },
  {
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
  },
  {
    fileNumber: 'FILE NO. 003',
    clearance: 'CLEARANCE: LEVEL 2',
    badge: 'STEP 3 / 5',
    icon: '💀',
    titleKorean: '전투력 시스템',
    subtitleEnglish: 'BATTLE POWER SYSTEM',
    intro: (
      <p className="onb-body-paragraph">
        모든 철학자에겐 <Highlight>전투력</Highlight>이 부여된다. 이것은 당신의
        사유가 얼마나 치명적인지를 수치화한 것이다.
      </p>
    ),
    body: (
      <>
        <div className="onb-formula">
          <p className="onb-formula-caption">BATTLE POWER FORMULA</p>
          <p className="onb-formula-eq">
            전투력 = (추천 × 2) + 댓글 수 + 글 수
          </p>
          <p className="onb-formula-note">
            추천이 가장 높은 가중치를 받는다. 양보다 질이 중요하다.
          </p>
        </div>
        <div className="onb-grid">
          <div className="onb-grid-cell">
            <p className="onb-grid-emoji">👍 ×2</p>
            <p className="onb-grid-caption">추천 가중치</p>
          </div>
          <div className="onb-grid-cell">
            <p className="onb-grid-emoji">💬 ×1</p>
            <p className="onb-grid-caption">댓글 가중치</p>
          </div>
        </div>
        <p className="onb-body-paragraph">
          그리고 <Highlight color="#f59e0b">도주율</Highlight>이 존재한다.
          자신이 쓴 글을 삭제하면 <Highlight>도주율</Highlight>이
          상승한다.
        </p>
        <div className="onb-formula">
          <p className="onb-formula-caption">FLEE RATE</p>
          <p className="onb-formula-eq">도주율 = 삭제 글 / 전체 글 × 100%</p>
          <p className="onb-formula-note">
            도주율이 높을수록 당신의 신뢰도는 바닥을 친다.
          </p>
        </div>
        <div className="onb-warning">
          ⚠ 전투력은 매일 자정 리셋됩니다. 오늘의 싸움은 오늘 끝납니다.
        </div>
      </>
    ),
  },
  {
    fileNumber: 'FILE NO. 004',
    clearance: 'CLEARANCE: LEVEL 3',
    badge: 'STEP 4 / 5',
    icon: '🔥',
    titleKorean: '오늘의 논쟁',
    subtitleEnglish: 'DAILY PHILOSOPHICAL BATTLE',
    intro: (
      <p className="onb-body-paragraph">
        매일 하나의 철학적 주제가 <Highlight>VS 토론</Highlight>으로 제시된다.
        두 진영으로 나뉘어 논리적 사투를 벌여라.
      </p>
    ),
    body: (
      <>
        <div className="onb-vs">
          <div className="onb-vs-side">
            <p className="onb-vs-label">찬성</p>
            <p className="onb-vs-text" style={{ color: '#3b82f6' }}>
              자유의지는
              <br />
              존재한다
            </p>
          </div>
          <p className="onb-vs-divider">VS</p>
          <div className="onb-vs-side">
            <p className="onb-vs-label">반대</p>
            <p className="onb-vs-text" style={{ color: '#f31260' }}>
              모든 것은
              <br />
              결정되어 있다
            </p>
          </div>
        </div>
        <p className="onb-body-paragraph">
          투표와 댓글로 진영을 지원하라. 24시간이 끝나면 승리 진영이 결정된다.
        </p>
        <ul className="onb-list">
          <li className="onb-list-item">
            <span className="onb-list-num">①</span>
            <span className="onb-list-text">
              진영 선택 후 <Highlight color="#f59e0b">변경 불가</Highlight> —
              신념을 가져라
            </span>
          </li>
          <li className="onb-list-item">
            <span className="onb-list-num">②</span>
            <span className="onb-list-text">
              양쪽 모두의 논거를 읽어라 — 적을 알아야 이긴다
            </span>
          </li>
          <li className="onb-list-item">
            <span className="onb-list-num">③</span>
            <span className="onb-list-text">
              승리 진영은 <Highlight>추가 전투력</Highlight>을 획득한다
            </span>
          </li>
        </ul>
      </>
    ),
  },
  {
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
  },
];
