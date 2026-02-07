'use client';

import dynamic from 'next/dynamic';

const NicknameGenerator = dynamic(
  () => import('@/Components/NicknameGenerator/NicknameGenerator'),
  { ssr: false }
);

export default function Page() {
  return (
    <>
      <NicknameGenerator />
    </>
  );
}
