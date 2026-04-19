import { TAGS, type Tag } from '@/lib/utils/tagColors';

/** UI/폼에서 쓰는 한글 태그 → Spring `PostTag` JSON(enum 이름) */
const KO_TO_API: Record<Tag, string> = {
  철학: 'PHILOSOPHY',
  사회: 'SOCIETY',
  정치: 'POLITICS',
  경제: 'ECONOMY',
  문화: 'CULTURE',
  일상: 'DAILY',
  기타: 'ETC',
};

const API_TO_KO: Record<string, Tag> = {
  PHILOSOPHY: '철학',
  SOCIETY: '사회',
  POLITICS: '정치',
  ECONOMY: '경제',
  CULTURE: '문화',
  DAILY: '일상',
  ETC: '기타',
  VOID: '기타',
};

/** 요청 본문에 넣을 태그 문자열 (한글 또는 이미 API 형식이면 그대로) */
export function toApiTag(tag: string): string {
  return KO_TO_API[tag as Tag] ?? tag;
}

/** 응답의 `PostTag` enum 이름 → 화면용 한글 */
export function displayTagFromApi(tag: string): Tag {
  if (tag in API_TO_KO) return API_TO_KO[tag];
  if ((TAGS as readonly string[]).includes(tag)) return tag as Tag;
  return '기타';
}
