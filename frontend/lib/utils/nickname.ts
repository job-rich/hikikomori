import {
  modifiers,
  famousPeople,
  animals,
} from '@/lib/data/nicknameDictionaries';

export function generateNickname(): string {
  const modifier = modifiers[Math.floor(Math.random() * modifiers.length)];
  const nouns = Math.random() < 0.5 ? famousPeople : animals;
  const noun = nouns[Math.floor(Math.random() * nouns.length)];
  return `${modifier} ${noun}`;
}
