import type { OnboardingStep as Step } from '@/lib/types/onboarding';

interface OnboardingStepProps {
  step: Step;
}

// 스크롤되는 본문 영역만 책임진다. 헤더(title)는 OnboardingStepHeader 가
// 스크롤 컨테이너 밖에서 고정 렌더한다.
export default function OnboardingStep({ step }: OnboardingStepProps) {
  return (
    <div className="onb-content">
      <div className="onb-intro">{step.intro}</div>
      {step.body}
    </div>
  );
}
