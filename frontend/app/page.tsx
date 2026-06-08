'use client';

import dynamic from 'next/dynamic';
import { useEffect } from 'react';
import Home from '@/Components/Home/Home';
import { useUserStore } from '@/lib/stores/userStore';
import { useOnboardingStore } from '@/lib/stores/onboardingStore';

const NicknameGenerator = dynamic(
  () => import('@/Components/NicknameGenerator/NicknameGenerator'),
  { ssr: false }
);

const OnboardingFlow = dynamic(
  () => import('@/Components/Onboarding/OnboardingFlow'),
  { ssr: false }
);

export default function Page() {
  const isLoggedIn = useUserStore((s) => s.isLoggedIn);
  const openNicknameModal = useUserStore((s) => s.openNicknameModal);
  const hasSeenOnboarding = useOnboardingStore((s) => s.hasSeenOnboarding);
  const openOnboarding = useOnboardingStore((s) => s.openOnboarding);

  // 첫 방문(닉네임 없음 + 온보딩 미경험) → 온보딩 모달 자동 노출.
  // 온보딩이 이미 본 상태라면 곧장 NicknameGenerator 로.
  useEffect(() => {
    if (isLoggedIn()) return;
    if (hasSeenOnboarding) {
      openNicknameModal();
      return;
    }
    openOnboarding();
  }, [isLoggedIn, hasSeenOnboarding, openOnboarding, openNicknameModal]);

  const handleOnboardingComplete = () => {
    if (!isLoggedIn()) openNicknameModal();
  };

  return (
    <>
      <Home />
      <OnboardingFlow onComplete={handleOnboardingComplete} />
      <NicknameGenerator />
    </>
  );
}
