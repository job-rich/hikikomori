'use client';

import { useEffect, useState } from 'react';
import { getRanking, type RankingEntry } from '@/lib/api/user';

export default function Ranking() {
  const [rows, setRows] = useState<RankingEntry[]>([]);

  useEffect(() => {
    getRanking(0, 20)
      .then((p) => setRows(p.content))
      .catch(() => setRows([]));
  }, []);

  return (
    <div className="mx-auto max-w-2xl px-4 py-6">
      <h1 className="mb-4 text-xl font-bold">🏆 랭킹 보드</h1>
      <ol className="space-y-2">
        {rows.map((r, i) => (
          <li
            key={r.userId}
            className="flex items-center gap-3 rounded border border-border bg-card px-4 py-2"
          >
            <span className="w-6 font-bold">{i + 1}</span>
            <span className="flex-1">{r.nickName}</span>
            {r.banned && <span className="text-xs text-destructive">제재</span>}
            <span className="font-mono">⚔ {r.power.toLocaleString()}</span>
          </li>
        ))}
        {rows.length === 0 && (
          <p className="text-sm text-muted-foreground">랭킹이 없습니다.</p>
        )}
      </ol>
    </div>
  );
}
