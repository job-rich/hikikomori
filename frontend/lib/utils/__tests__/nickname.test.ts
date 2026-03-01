import { describe, it, expect, vi, afterEach } from 'vitest';
import { generateNickname } from '@/lib/utils/nickname';
import {
  modifiers,
  famousPeople,
  animals,
} from '@/lib/data/nicknameDictionaries';

const allNouns = [...famousPeople, ...animals];

function parseNickname(nickname: string) {
  for (const modifier of modifiers) {
    if (nickname.startsWith(modifier + ' ')) {
      const noun = nickname.slice(modifier.length + 1);
      return { modifier, noun };
    }
  }
  return null;
}

describe('generateNickname', () => {
  afterEach(() => {
    vi.restoreAllMocks();
  });

  it('"수식어 명사" 형식의 문자열을 반환해야 한다', () => {
    const nickname = generateNickname();
    const parsed = parseNickname(nickname);
    expect(parsed).not.toBeNull();
  });

  it('수식어가 dictionary에 포함된 값이어야 한다', () => {
    for (let i = 0; i < 50; i++) {
      const nickname = generateNickname();
      const parsed = parseNickname(nickname);
      expect(parsed).not.toBeNull();
      expect(modifiers).toContain(parsed!.modifier);
    }
  });

  it('명사가 유명인 또는 동물 dictionary에 포함된 값이어야 한다', () => {
    for (let i = 0; i < 50; i++) {
      const nickname = generateNickname();
      const parsed = parseNickname(nickname);
      expect(parsed).not.toBeNull();
      expect(allNouns).toContain(parsed!.noun);
    }
  });

  it('Math.random < 0.5이면 유명인이 선택된다', () => {
    vi.spyOn(Math, 'random')
      .mockReturnValueOnce(0) // modifier index
      .mockReturnValueOnce(0.1) // < 0.5 → famousPeople
      .mockReturnValueOnce(0); // noun index

    const nickname = generateNickname();
    const parsed = parseNickname(nickname);
    expect(parsed).not.toBeNull();
    expect(famousPeople).toContain(parsed!.noun);
  });

  it('Math.random >= 0.5이면 동물이 선택된다', () => {
    vi.spyOn(Math, 'random')
      .mockReturnValueOnce(0) // modifier index
      .mockReturnValueOnce(0.7) // >= 0.5 → animals
      .mockReturnValueOnce(0); // noun index

    const nickname = generateNickname();
    const parsed = parseNickname(nickname);
    expect(parsed).not.toBeNull();
    expect(animals).toContain(parsed!.noun);
  });
});
