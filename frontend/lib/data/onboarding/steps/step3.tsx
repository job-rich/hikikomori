import type { OnboardingStep } from '@/lib/types/onboarding';
import Highlight from '@/Components/Common/Highlight/Highlight';

export const step3: OnboardingStep = {
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
        <p className="onb-formula-eq">전투력 = (추천 × 2) + 댓글 수 + 글 수</p>
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
        그리고 <Highlight color="#f59e0b">도주율</Highlight>이 존재한다. 자신이
        쓴 글을 삭제하면 <Highlight>도주율</Highlight>이 상승한다.
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
};
