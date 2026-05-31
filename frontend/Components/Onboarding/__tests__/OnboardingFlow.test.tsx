import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { cleanup, fireEvent, render, screen } from '@testing-library/react';
import OnboardingFlow from '@/Components/Onboarding/OnboardingFlow';
import { useOnboardingStore } from '@/lib/stores/onboardingStore';
import { FINAL_STEP_INDEX } from '@/lib/data/onboardingSteps';

beforeEach(() => {
  localStorage.clear();
  useOnboardingStore.getState().resetOnboarding();
  useOnboardingStore.getState().openOnboarding();
});

afterEach(cleanup);

describe('OnboardingFlow (modal)', () => {
  it('isOpen=false 면 null 을 반환한다', () => {
    useOnboardingStore.getState().closeOnboarding();
    const { container } = render(<OnboardingFlow />);
    expect(container.firstChild).toBeNull();
  });

  it('hasSeenOnboarding=true 면 null 을 반환한다 (재방문)', () => {
    useOnboardingStore.setState({ hasSeenOnboarding: true });
    const { container } = render(<OnboardingFlow />);
    expect(container.firstChild).toBeNull();
  });

  it('open 시 STEP 1 / 5 배지를 보여준다', () => {
    render(<OnboardingFlow />);
    expect(screen.getByText('STEP 1 / 5')).toBeInTheDocument();
    expect(screen.getByText('환영한다, 새로운 철학자여')).toBeInTheDocument();
  });

  it('section 에 id="onboarding-section" 이 부착되어 있다', () => {
    const { container } = render(<OnboardingFlow />);
    expect(container.querySelector('#onboarding-section')).not.toBeNull();
  });

  it('section 에 entrance 애니메이션 클래스가 부착되어 있다', () => {
    const { container } = render(<OnboardingFlow />);
    const shell = container.querySelector('#onboarding-section');
    expect(shell?.className).toContain('onb-shell-enter');
  });

  it('"다음" 클릭 시 다음 스텝으로 이동', () => {
    render(<OnboardingFlow />);
    fireEvent.click(screen.getByRole('button', { name: /다음/ }));
    expect(screen.getByText('투기장의 규칙')).toBeInTheDocument();
  });

  it('스텝 전환 시 슬라이드 컨테이너 key 가 currentStep 따라 갱신된다', () => {
    const { container } = render(<OnboardingFlow />);
    const stepWrapper1 = container.querySelector('.onb-step-slide');
    expect(stepWrapper1).not.toBeNull();
    fireEvent.click(screen.getByRole('button', { name: /다음/ }));
    expect(container.querySelector('.onb-step-slide')).not.toBeNull();
  });

  it('마지막 스텝의 버튼은 "입장하기"', () => {
    useOnboardingStore.getState().goToStep(FINAL_STEP_INDEX);
    render(<OnboardingFlow />);
    expect(
      screen.getByRole('button', { name: '입장하기' })
    ).toBeInTheDocument();
  });

  it('"입장하기" 클릭 시 completeOnboarding + onComplete 콜백', () => {
    useOnboardingStore.getState().goToStep(FINAL_STEP_INDEX);
    const onComplete = vi.fn();
    render(<OnboardingFlow onComplete={onComplete} />);
    fireEvent.click(screen.getByRole('button', { name: '입장하기' }));
    expect(onComplete).toHaveBeenCalledOnce();
    expect(useOnboardingStore.getState().hasSeenOnboarding).toBe(true);
    expect(useOnboardingStore.getState().isOpen).toBe(false);
  });

  it('"건너뛰기" 클릭 시 skipOnboarding + onComplete 콜백 (동일 종착)', () => {
    const onComplete = vi.fn();
    render(<OnboardingFlow onComplete={onComplete} />);
    fireEvent.click(screen.getByRole('button', { name: /건너뛰기/ }));
    expect(onComplete).toHaveBeenCalledOnce();
    expect(useOnboardingStore.getState().hasSeenOnboarding).toBe(true);
    expect(useOnboardingStore.getState().isOpen).toBe(false);
  });

  it('첫 스텝에서 "이전" 버튼은 비활성화', () => {
    render(<OnboardingFlow />);
    expect(screen.getByRole('button', { name: /이전/ })).toBeDisabled();
  });

  it('backdrop 클릭해도 모달이 닫히지 않는다 (명시 버튼만 닫힘)', () => {
    const onComplete = vi.fn();
    const { container } = render(<OnboardingFlow onComplete={onComplete} />);
    const backdrop = container.querySelector('[role="dialog"]');
    expect(backdrop).not.toBeNull();
    fireEvent.click(backdrop as Element);
    expect(useOnboardingStore.getState().isOpen).toBe(true);
    expect(useOnboardingStore.getState().hasSeenOnboarding).toBe(false);
    expect(onComplete).not.toHaveBeenCalled();
  });

  it('Escape 키를 눌러도 모달이 닫히지 않는다', () => {
    const onComplete = vi.fn();
    render(<OnboardingFlow onComplete={onComplete} />);
    fireEvent.keyDown(window, { key: 'Escape' });
    expect(useOnboardingStore.getState().isOpen).toBe(true);
    expect(useOnboardingStore.getState().hasSeenOnboarding).toBe(false);
    expect(onComplete).not.toHaveBeenCalled();
  });

  it('shell 컨테이너에 레이아웃 안정화 클래스(onb-shell-stable) 가 부착되어 있다', () => {
    const { container } = render(<OnboardingFlow />);
    const shell = container.querySelector('#onboarding-section');
    expect(shell?.className).toContain('onb-shell-stable');
  });
});

describe('OnboardingFlow — 스크롤 잠금', () => {
  // jsdom 은 layout 측정을 안 하므로 prototype 으로 mock.
  // 이 describe 블록 안에서만 적용 → 기존 14개 테스트 회귀 안전.
  const originalDescriptors: Record<string, PropertyDescriptor | undefined> = {};

  beforeEach(() => {
    originalDescriptors.scrollHeight = Object.getOwnPropertyDescriptor(
      HTMLElement.prototype,
      'scrollHeight'
    );
    originalDescriptors.clientHeight = Object.getOwnPropertyDescriptor(
      HTMLElement.prototype,
      'clientHeight'
    );
    originalDescriptors.scrollTop = Object.getOwnPropertyDescriptor(
      HTMLElement.prototype,
      'scrollTop'
    );
  });

  afterEach(() => {
    for (const [key, desc] of Object.entries(originalDescriptors)) {
      if (desc) {
        Object.defineProperty(HTMLElement.prototype, key, desc);
      } else {
        delete (HTMLElement.prototype as unknown as Record<string, unknown>)[
          key
        ];
      }
    }
  });

  it('콘텐츠가 길어 스크롤 가능한 STEP 은 "다음" 버튼이 비활성', () => {
    Object.defineProperty(HTMLElement.prototype, 'scrollHeight', {
      configurable: true,
      value: 1000,
    });
    Object.defineProperty(HTMLElement.prototype, 'clientHeight', {
      configurable: true,
      value: 400,
    });
    Object.defineProperty(HTMLElement.prototype, 'scrollTop', {
      configurable: true,
      writable: true,
      value: 0,
    });
    render(<OnboardingFlow />);
    expect(screen.getByRole('button', { name: /다음/ })).toBeDisabled();
  });

  it('스크롤이 끝까지 도달하면 "다음" 버튼이 활성', () => {
    Object.defineProperty(HTMLElement.prototype, 'scrollHeight', {
      configurable: true,
      value: 1000,
    });
    Object.defineProperty(HTMLElement.prototype, 'clientHeight', {
      configurable: true,
      value: 400,
    });
    Object.defineProperty(HTMLElement.prototype, 'scrollTop', {
      configurable: true,
      writable: true,
      value: 0,
    });
    const { container } = render(<OnboardingFlow />);
    const slide = container.querySelector('.onb-step-slide') as HTMLDivElement;
    Object.defineProperty(slide, 'scrollTop', {
      configurable: true,
      value: 600,
    });
    fireEvent.scroll(slide);
    expect(screen.getByRole('button', { name: /다음/ })).not.toBeDisabled();
  });

  it('콘텐츠가 짧아 스크롤 불필요한 STEP 은 마운트 즉시 "다음" 버튼 활성', () => {
    Object.defineProperty(HTMLElement.prototype, 'scrollHeight', {
      configurable: true,
      value: 300,
    });
    Object.defineProperty(HTMLElement.prototype, 'clientHeight', {
      configurable: true,
      value: 400,
    });
    Object.defineProperty(HTMLElement.prototype, 'scrollTop', {
      configurable: true,
      writable: true,
      value: 0,
    });
    render(<OnboardingFlow />);
    expect(screen.getByRole('button', { name: /다음/ })).not.toBeDisabled();
  });
});
