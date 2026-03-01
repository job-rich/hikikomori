'use client';

import dynamic from 'next/dynamic';
import Home from '@/Components/Home/Home';

const NicknameGenerator = dynamic(
  () => import('@/Components/NicknameGenerator/NicknameGenerator'),
  { ssr: false }
);

export default function Page() {
  return (
    <>
      <Home />
      <NicknameGenerator />
    </>
  );
}
