import { create } from 'zustand';
import { useShallow } from 'zustand/react/shallow';
import { persist, createJSONStorage } from 'zustand/middleware';
import type { OnboardingState } from '@/lib/types/onboarding';
import { FINAL_STEP_INDEX, TOTAL_STEPS } from '@/lib/data/onboarding';

export const useOnboardingStore = create<OnboardingState>()(
  persist(
    (set, get) => ({
      isOpen: false,
      hasSeenOnboarding: false,
      currentStep: 0,
      openOnboarding: () => set({ isOpen: true, currentStep: 0 }),
      closeOnboarding: () => set({ isOpen: false }),
      goToNextStep: () => {
        const currentStepIndex = get().currentStep;
        if (currentStepIndex >= FINAL_STEP_INDEX) return;
        set({ currentStep: currentStepIndex + 1 });
      },
      goToPreviousStep: () => {
        const currentStepIndex = get().currentStep;
        if (currentStepIndex <= 0) return;
        set({ currentStep: currentStepIndex - 1 });
      },
      goToStep: (step) => {
        if (step < 0 || step >= TOTAL_STEPS) return;
        set({ currentStep: step });
      },
      skipOnboarding: () => set({ isOpen: false, hasSeenOnboarding: true }),
      completeOnboarding: () =>
        set({ isOpen: false, hasSeenOnboarding: true, currentStep: 0 }),
      resetOnboarding: () =>
        set({ isOpen: false, hasSeenOnboarding: false, currentStep: 0 }),
    }),
    {
      name: 'hikikomori-onboarding',
      storage: createJSONStorage(() => localStorage),
      // hasSeenOnboarding 만 영속 — currentStep / isOpen 은 재방문 시 무의미.
      partialize: (state) => ({ hasSeenOnboarding: state.hasSeenOnboarding }),
    }
  )
);

export const useOnboardingProgress = () =>
  useOnboardingStore(
    useShallow((s) => ({
      isOpen: s.isOpen,
      hasSeenOnboarding: s.hasSeenOnboarding,
      currentStep: s.currentStep,
      isFirstStep: s.currentStep === 0,
      isLastStep: s.currentStep === FINAL_STEP_INDEX,
    }))
  );

export const useOnboardingActions = () =>
  useOnboardingStore(
    useShallow((s) => ({
      goToNextStep: s.goToNextStep,
      goToPreviousStep: s.goToPreviousStep,
      skipOnboarding: s.skipOnboarding,
      completeOnboarding: s.completeOnboarding,
    }))
  );
