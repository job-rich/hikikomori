import { describe, it, expect } from 'vitest';
import { generateNickname } from '@/lib/utils/nickname';
import { modifiers, famousPeople } from '@/lib/data/nicknameDictionaries';

describe('generateNickname', () => {
  it('"수식어 유명인" 형식의 문자열을 반환해야 한다', () => {
    const nickname = generateNickname();
    const parts = nickname.split(' ');
    expect(parts).toHaveLength(2);
  });

  it('수식어가 dictionary에 포함된 값이어야 한다', () => {
    const nickname = generateNickname();
    const [modifier] = nickname.split(' ');
    expect(modifiers).toContain(modifier);
  });

  it('유명인이 dictionary에 포함된 값이어야 한다', () => {
    const nickname = generateNickname();
    const [, person] = nickname.split(' ');
    expect(famousPeople).toContain(person);
  });

  it('여러 번 호출해도 항상 유효한 닉네임을 반환해야 한다', () => {
    for (let i = 0; i < 50; i++) {
      const nickname = generateNickname();
      const parts = nickname.split(' ');
      expect(parts).toHaveLength(2);
      expect(modifiers).toContain(parts[0]);
      expect(famousPeople).toContain(parts[1]);
    }
  });
});
