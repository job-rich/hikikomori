import type { OnboardingStep as Step } from '@/lib/types/onboarding';
import StepBadge from './StepBadge';

interface OnboardingStepHeaderProps {
  step: Step;
}

// 스크롤 컨테이너(.onb-step-slide) 밖에 렌더되는 고정 헤더 — 본문을 스크롤해도
// title 이 전혀 밀리지 않는다. 본문(.onb-content)만 스크롤된다.
export default function OnboardingStepHeader({
  step,
}: OnboardingStepHeaderProps) {
  return (
    <div className="onb-step-head" aria-label={step.titleKorean}>
      <div className="onb-meta">
        <span>{step.fileNumber}</span>
        <span>{step.clearance}</span>
      </div>
      <div className="onb-header">
        <StepBadge label={step.badge} />
        <span className="onb-step-icon" aria-hidden>
          {step.icon}
        </span>
        <h2 className="onb-step-title">{step.titleKorean}</h2>
        <p className="onb-step-subtitle">{step.subtitleEnglish}</p>
      </div>
      <div className="onb-divider" />
    </div>
  );
}
