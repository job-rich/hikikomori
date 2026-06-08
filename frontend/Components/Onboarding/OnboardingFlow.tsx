'use client';

import Modal from '@/Components/Common/Modal/Modal';
import {
  useOnboardingActions,
  useOnboardingProgress,
} from '@/lib/stores/onboardingStore';
import { onboardingSteps } from '@/lib/data/onboarding';
import { useScrolledToBottom } from '@/lib/hooks/useScrolledToBottom';
import BriefingHeader from './BriefingHeader';
import OnboardingStepHeader from './OnboardingStepHeader';
import OnboardingStep from './OnboardingStep';
import ClassifiedStamps from './ClassifiedStamps';
import StepIndicator from './StepIndicator';
import NavigationButtons from './NavigationButtons';
import './onboarding.css';

interface OnboardingFlowProps {
  onComplete?: () => void;
}

export default function OnboardingFlow({ onComplete }: OnboardingFlowProps) {
  const { isOpen, hasSeenOnboarding, currentStep, isFirstStep, isLastStep } =
    useOnboardingProgress();
  const { goToNextStep, goToPreviousStep, skipOnboarding, completeOnboarding } =
    useOnboardingActions();
  const { ref, isBottom, onScroll } = useScrolledToBottom<HTMLDivElement>(
    currentStep
  );

  if (!isOpen || hasSeenOnboarding) return null;

  const step = onboardingSteps[currentStep];

  const handleSkip = () => {
    skipOnboarding();
    onComplete?.();
  };

  const handleNext = () => {
    if (isLastStep) {
      completeOnboarding();
      onComplete?.();
      return;
    }
    goToNextStep();
  };

  return (
    <Modal
      open={isOpen}
      onClose={handleSkip}
      closeOnBackdrop={false}
      closeOnEscape={false}
      ariaLabel="신규 요원 기밀 브리핑"
      backdropClassName="onb-backdrop bg-black/50"
      className="onb-modal-content"
    >
      <section
        id="onboarding-section"
        className="onb-shell onb-shell-stable onb-shell-enter"
        aria-label="신규 요원 기밀 브리핑"
      >
        <ClassifiedStamps />
        <BriefingHeader onSkip={handleSkip} />
        <OnboardingStepHeader step={step} />
        <div
          key={currentStep}
          ref={ref}
          onScroll={onScroll}
          className="onb-step-slide"
        >
          <OnboardingStep step={step} />
        </div>
        <StepIndicator current={currentStep} />
        <NavigationButtons
          isFirst={isFirstStep}
          isLast={isLastStep}
          isNextDisabled={!isBottom}
          onPrev={goToPreviousStep}
          onNext={handleNext}
        />
      </section>
    </Modal>
  );
}
