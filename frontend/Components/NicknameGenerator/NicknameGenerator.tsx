'use client';

import { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { generateNickname } from '@/lib/utils/nickname';
import { generateSnowflakeId } from '@/lib/utils/snowflake';

export default function NicknameGenerator() {
  const router = useRouter();
  const [nickname, setNickname] = useState('');
  const [snowflakeId, setSnowflakeId] = useState('');

  useEffect(() => {
    const name = generateNickname();
    const id = generateSnowflakeId();
    setNickname(name);
    setSnowflakeId(id);
    if (process.env.NODE_ENV === 'development') {
      console.log(`닉네임: ${name}, id: ${id}`);
    }
  }, []);

  const handleRegenerate = () => {
    const name = generateNickname();
    const id = generateSnowflakeId();
    setNickname(name);
    setSnowflakeId(id);
    if (process.env.NODE_ENV === 'development') {
      console.log(`닉네임: ${name}, id: ${id}`);
    }
  };

  const handleConfirm = () => {
    router.push('/home');
  };

  return (
    <div className="flex min-h-screen items-center justify-center bg-zinc-50 dark:bg-black">
      <div className="flex w-full max-w-md flex-col items-center gap-6 rounded-lg border border-zinc-200 bg-white px-8 py-12 dark:border-zinc-800 dark:bg-zinc-950">
        <p className="text-2xl font-bold text-zinc-900 dark:text-zinc-100">
          {nickname}
        </p>
        <div className="flex gap-3">
          <button
            onClick={handleConfirm}
            className="cursor-pointer rounded-md bg-zinc-900 px-6 py-2 text-sm font-medium text-white hover:bg-zinc-800 dark:bg-zinc-100 dark:text-zinc-900 dark:hover:bg-zinc-200"
          >
            확인
          </button>
          <button
            onClick={handleRegenerate}
            className="cursor-pointer rounded-md border border-zinc-300 bg-white px-6 py-2 text-sm font-medium text-zinc-700 hover:bg-zinc-50 dark:border-zinc-700 dark:bg-zinc-900 dark:text-zinc-300 dark:hover:bg-zinc-800"
          >
            닉네임 재생성
          </button>
        </div>
      </div>
    </div>
  );
}
