'use client';

import { useEffect, useRef, useState } from 'react';
import { getProfile, type UserProfile } from '@/lib/api/user';
import { useUserStore } from '@/lib/stores/userStore';

export default function Profile() {
  const { snowflakeId, nickname, setUser } = useUserStore();
  const [profile, setProfile] = useState<UserProfile | null>(null);
  const fileRef = useRef<HTMLInputElement>(null);

  useEffect(() => {
    if (snowflakeId == null) return;
    getProfile(Number(snowflakeId))
      .then(setProfile)
      .catch(() => setProfile(null));
  }, [snowflakeId]);

  const exportId = () => {
    const data = JSON.stringify({ snowflakeId, nickName: nickname }, null, 2);
    const blob = new Blob([data], { type: 'application/json' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `hikikomori-id-${snowflakeId}.json`;
    a.click();
    URL.revokeObjectURL(url);
  };

  const importId = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;
    try {
      const json = JSON.parse(await file.text());
      if (json.snowflakeId && json.nickName) {
        setUser(json.nickName, String(json.snowflakeId));
        window.alert('ID를 복원했습니다.');
      }
    } catch {
      window.alert('잘못된 ID 파일입니다.');
    }
  };

  return (
    <div className="mx-auto max-w-2xl px-4 py-6">
      <h1 className="mb-4 text-xl font-bold">🪪 프로필</h1>
      {profile ? (
        <div className="rounded-lg border border-border bg-card p-6">
          <p className="text-2xl font-black">{profile.nickName}</p>
          <div className="mt-3 flex gap-4 text-sm">
            <span>⚔ 전투력 {profile.power.toLocaleString()}</span>
            <span>랭킹 #{profile.rank}</span>
            {profile.banned && <span className="text-destructive">제재됨</span>}
          </div>
          <div className="mt-3 text-xs text-muted-foreground">
            받은 추천 {profile.voteNet} · 받은 신고 {profile.reports}
          </div>
        </div>
      ) : (
        <p className="text-sm text-muted-foreground">활동 기록이 없습니다.</p>
      )}

      <div className="mt-4 rounded border border-dashed border-border p-4">
        <p className="mb-2 text-xs text-muted-foreground">스노우플레이크 ID</p>
        <code className="break-all text-sm">{snowflakeId}</code>
        <div className="mt-3 flex gap-2">
          <button
            type="button"
            onClick={exportId}
            className="rounded border border-border px-3 py-1.5 text-xs"
          >
            .json 내보내기
          </button>
          <button
            type="button"
            onClick={() => fileRef.current?.click()}
            className="rounded border border-border px-3 py-1.5 text-xs"
          >
            가져오기
          </button>
          <input
            ref={fileRef}
            type="file"
            accept="application/json"
            className="hidden"
            onChange={importId}
          />
        </div>
        <p className="mt-2 text-[10px] text-muted-foreground">
          ⚠ 세션이 초기화돼도 ID를 가져오면 전투력이 복원됩니다.
        </p>
      </div>
    </div>
  );
}
