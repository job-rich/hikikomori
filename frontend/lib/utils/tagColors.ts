export const TAGS = [
  '철학',
  '사회',
  '정치',
  '경제',
  '문화',
  '일상',
  '기타',
] as const;

export type Tag = (typeof TAGS)[number];

export const TAG_STYLES: Record<string, string> = {
  철학: 'bg-rose-500 text-white',
  사회: 'bg-blue-500 text-white',
  정치: 'bg-purple-500 text-white',
  경제: 'bg-emerald-500 text-white',
  문화: 'bg-amber-500 text-white',
  일상: 'bg-pink-400 text-white',
  기타: 'bg-gray-500 text-white',
};

export const TAG_DOT_COLORS: Record<string, string> = {
  철학: 'bg-rose-500',
  사회: 'bg-blue-500',
  정치: 'bg-purple-500',
  경제: 'bg-emerald-500',
  문화: 'bg-amber-500',
  일상: 'bg-pink-400',
  기타: 'bg-gray-500',
};
