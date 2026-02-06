import { uniqueNamesGenerator } from 'unique-names-generator';
import { modifiers, famousPeople } from '@/lib/data/nicknameDictionaries';

export function generateNickname(): string {
  return uniqueNamesGenerator({
    dictionaries: [modifiers, famousPeople],
    separator: ' ',
    length: 2,
  });
}
