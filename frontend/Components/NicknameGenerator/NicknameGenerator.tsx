'use client';

import { useEffect, useRef } from 'react';
import { useRouter } from 'next/navigation';
import { generateNickname } from '@/lib/utils/nickname';
import { generateSnowflakeId } from '@/lib/utils/snowflake';
import { useUserStore } from '@/lib/stores/userStore';
import { useReducer } from 'react';
import CenteredLayout from '@/Components/Common/Layout/CenteredLayout';
import Card from '@/Components/Common/Card/Card';

interface UserValues {
  nickname: string;
  snowflakeId: string;
}

function generateValues(): UserValues {
  if (useUserStore.getState().isLoggedIn()) {
    return { nickname: '', snowflakeId: '' };
  }
  const values = {
    nickname: generateNickname(),
    snowflakeId: generateSnowflakeId(),
  };
  if (process.env.NODE_ENV === 'development') {
    console.log(`닉네임: ${values.nickname}, id: ${values.snowflakeId}`);
  }
  return values;
}

function valuesReducer(_state: UserValues, action: 'regenerate'): UserValues {
  if (action === 'regenerate') {
    return generateValues();
  }
  return _state;
}

export default function NicknameGenerator() {
  const router = useRouter();
  const { setUser } = useUserStore();
  const [values, dispatch] = useReducer(valuesReducer, null, generateValues);
  const redirected = useRef(false);

  useEffect(() => {
    if (redirected.current) return;
    if (useUserStore.getState().isLoggedIn()) {
      redirected.current = true;
      router.replace('/home');
    }
  }, [router]);

  const handleRegenerate = () => {
    dispatch('regenerate');
  };

  const handleConfirm = () => {
    setUser(values.nickname, values.snowflakeId);
    router.push('/home');
  };

  return (
    <CenteredLayout>
      <Card>
        <h1 className="text-3xl font-semibold text-zinc-900 dark:text-zinc-100">
          명예로운 이름을 선정하세요
        </h1>
        <p className="text-2xl font-bold text-zinc-900 dark:text-zinc-100">
          {values.nickname}
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
      </Card>
    </CenteredLayout>
  );
}
