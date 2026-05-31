import { beforeEach, describe, expect, it } from 'vitest';
import { useOnboardingStore } from '@/lib/stores/onboardingStore';
import { FINAL_STEP_INDEX } from '@/lib/data/onboardingSteps';

beforeEach(() => {
  localStorage.clear();
  useOnboardingStore.getState().resetOnboarding();
});

describe('onboardingStore', () => {
  it('초기값이 올바르다', () => {
    const state = useOnboardingStore.getState();
    expect(state.isOpen).toBe(false);
    expect(state.hasSeenOnboarding).toBe(false);
    expect(state.currentStep).toBe(0);
  });

  it('openOnboarding 은 모달을 열고 currentStep 을 0 으로 리셋한다', () => {
    useOnboardingStore.setState({ currentStep: 3 });
    useOnboardingStore.getState().openOnboarding();
    const state = useOnboardingStore.getState();
    expect(state.isOpen).toBe(true);
    expect(state.currentStep).toBe(0);
  });

  it('goToNextStep / goToPreviousStep 은 경계를 넘지 않는다', () => {
    const state = useOnboardingStore.getState();
    state.goToPreviousStep();
    expect(useOnboardingStore.getState().currentStep).toBe(0);
    for (let i = 0; i < FINAL_STEP_INDEX + 5; i += 1) state.goToNextStep();
    expect(useOnboardingStore.getState().currentStep).toBe(FINAL_STEP_INDEX);
  });

  it('goToStep 은 범위 밖이면 무시한다', () => {
    const state = useOnboardingStore.getState();
    state.goToStep(99);
    expect(useOnboardingStore.getState().currentStep).toBe(0);
    state.goToStep(2);
    expect(useOnboardingStore.getState().currentStep).toBe(2);
    state.goToStep(-1);
    expect(useOnboardingStore.getState().currentStep).toBe(2);
  });

  it('skipOnboarding 과 completeOnboarding 모두 hasSeenOnboarding 을 true 로 둔다 (예측가능성)', () => {
    useOnboardingStore.getState().skipOnboarding();
    expect(useOnboardingStore.getState().hasSeenOnboarding).toBe(true);
    expect(useOnboardingStore.getState().isOpen).toBe(false);

    useOnboardingStore.getState().resetOnboarding();
    useOnboardingStore.getState().completeOnboarding();
    expect(useOnboardingStore.getState().hasSeenOnboarding).toBe(true);
    expect(useOnboardingStore.getState().isOpen).toBe(false);
  });

  it('persist key 가 hikikomori-onboarding 으로 분리되어 있다', () => {
    useOnboardingStore.getState().completeOnboarding();
    const raw = localStorage.getItem('hikikomori-onboarding');
    expect(raw).not.toBeNull();
    const parsed = JSON.parse(raw as string);
    expect(parsed.state.hasSeenOnboarding).toBe(true);
  });
});
