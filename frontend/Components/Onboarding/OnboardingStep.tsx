import type { OnboardingStep as Step } from '@/lib/types/onboarding';
import StepBadge from './StepBadge';

interface OnboardingStepProps {
  step: Step;
}

export default function OnboardingStep({ step }: OnboardingStepProps) {
  return (
    <article aria-label={step.titleKorean}>
      <div className="onb-step-head">
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
      <div className="onb-content">
        <div className="onb-intro">{step.intro}</div>
        {step.body}
      </div>
    </article>
  );
}
