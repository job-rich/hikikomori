import { modifiers, famousPeople } from '@/lib/data/nicknameDictionaries';

export function generateNickname(): string {
  const modifier = modifiers[Math.floor(Math.random() * modifiers.length)];
  const person = famousPeople[Math.floor(Math.random() * famousPeople.length)];
  return `${modifier} ${person}`;
}
