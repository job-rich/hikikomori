'use client';

import { useEffect, useState } from 'react';
import { generateNickname } from '@/lib/utils/nickname';
import { generateSnowflakeId } from '@/lib/utils/snowflake';
import { useUserStore } from '@/lib/stores/userStore';
import ArenaTheme from './ArenaTheme';

interface UserValues {
  nickname: string;
  snowflakeId: string;
}

function generateValues(): UserValues {
  return {
    nickname: generateNickname(),
    snowflakeId: generateSnowflakeId(),
  };
}

export default function NicknameGenerator() {
  const { setUser, nicknameModalOpen, closeNicknameModal } = useUserStore();
  const [values, setValues] = useState<UserValues | null>(null);

  useEffect(() => {
    if (nicknameModalOpen) {
      setValues(generateValues());
      document.body.style.overflow = 'hidden';
    }
    return () => {
      document.body.style.overflow = '';
    };
  }, [nicknameModalOpen]);

  if (!nicknameModalOpen || !values) return null;

  const handleRegenerate = () => {
    setValues(generateValues());
  };

  const handleConfirm = () => {
    setUser(values.nickname, values.snowflakeId);
  };

  return (
    <ArenaTheme
      nickname={values.nickname}
      onConfirm={handleConfirm}
      onRegenerate={handleRegenerate}
      onClose={closeNicknameModal}
    />
  );
}
