import { useEffect, useState } from 'react';
import { vote, type VoteTargetType, type VoteValue } from '@/lib/api/vote';
import { useUserStore } from '@/lib/stores/userStore';

interface UseVoteParams {
  targetType: VoteTargetType;
  targetId: string;
  /** 대상 작성자 userId (자기추천 차단·요청 경로) */
  authorUserId: number | null | undefined;
  initialScore: number;
}

/**
 * 게시글/댓글 추천 토글 상태와 핸들러. Post-Card·Post-Detail·댓글에서 공유.
 * 자기추천 차단 + in-flight 가드(동시 더블클릭 race 방지) 포함.
 */
export function useVote({
  targetType,
  targetId,
  authorUserId,
  initialScore,
}: UseVoteParams) {
  const snowflakeId = useUserStore((s) => s.snowflakeId);
  const [score, setScore] = useState<number>(initialScore);
  const [myVote, setMyVote] = useState<VoteValue | null>(null);
  const [voting, setVoting] = useState(false);

  // 대상이 비동기 로드되는 경우(예: 게시글 상세) initialScore 변동을 반영
  useEffect(() => {
    setScore(initialScore);
  }, [initialScore]);

  const castVote = async (value: VoteValue) => {
    if (snowflakeId == null || authorUserId == null || voting) return;
    if (Number(snowflakeId) === authorUserId) return; // 자기추천 불가
    setVoting(true);
    try {
      const res = await vote(authorUserId, {
        voterId: Number(snowflakeId),
        targetType,
        targetId,
        value,
      });
      setScore(res.score);
      setMyVote(res.value);
    } catch (err) {
      console.error('추천 실패:', err);
    } finally {
      setVoting(false);
    }
  };

  return { score, myVote, voting, castVote };
}
