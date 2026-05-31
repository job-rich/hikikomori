import type { ReactNode } from 'react';

export interface OnboardingStep {
  fileNumber: string;
  clearance: string;
  badge: string;
  icon: string;
  titleKorean: string;
  subtitleEnglish: string;
  intro: ReactNode;
  body: ReactNode;
}

export interface OnboardingState {
  isOpen: boolean;
  hasSeenOnboarding: boolean;
  currentStep: number;
  openOnboarding: () => void;
  closeOnboarding: () => void;
  goToNextStep: () => void;
  goToPreviousStep: () => void;
  goToStep: (step: number) => void;
  skipOnboarding: () => void;
  completeOnboarding: () => void;
  resetOnboarding: () => void;
}
