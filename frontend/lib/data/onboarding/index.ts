import type { OnboardingStep } from '@/lib/types/onboarding';
import { step1 } from './steps/step1';
import { step2 } from './steps/step2';
import { step3 } from './steps/step3';
import { step4 } from './steps/step4';
import { step5 } from './steps/step5';

export { TOTAL_STEPS, FINAL_STEP_INDEX } from './constants';

export const onboardingSteps: OnboardingStep[] = [
  step1,
  step2,
  step3,
  step4,
  step5,
];
