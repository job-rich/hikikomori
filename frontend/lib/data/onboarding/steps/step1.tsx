import type { OnboardingStep } from '@/lib/types/onboarding';
import Highlight from '@/Components/Common/Highlight/Highlight';

export const step1: OnboardingStep = {
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
};
