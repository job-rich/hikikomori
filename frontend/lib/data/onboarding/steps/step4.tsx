import type { OnboardingStep } from '@/lib/types/onboarding';
import Highlight from '@/Components/Common/Highlight/Highlight';

export const step4: OnboardingStep = {
  fileNumber: 'FILE NO. 004',
  clearance: 'CLEARANCE: LEVEL 3',
  badge: 'STEP 4 / 5',
  icon: '🔥',
  titleKorean: '오늘의 논쟁',
  subtitleEnglish: 'DAILY PHILOSOPHICAL BATTLE',
  intro: (
    <p className="onb-body-paragraph">
      매일 하나의 철학적 주제가 <Highlight>VS 토론</Highlight>으로 제시된다. 두
      진영으로 나뉘어 논리적 사투를 벌여라.
    </p>
  ),
  body: (
    <>
      <div className="onb-vs">
        <div className="onb-vs-side">
          <p className="onb-vs-label">찬성</p>
          <p className="onb-vs-text" style={{ color: '#3b82f6' }}>
            자유의지는
            <br />
            존재한다
          </p>
        </div>
        <p className="onb-vs-divider">VS</p>
        <div className="onb-vs-side">
          <p className="onb-vs-label">반대</p>
          <p className="onb-vs-text" style={{ color: '#f31260' }}>
            모든 것은
            <br />
            결정되어 있다
          </p>
        </div>
      </div>
      <p className="onb-body-paragraph">
        투표와 댓글로 진영을 지원하라. 24시간이 끝나면 승리 진영이 결정된다.
      </p>
      <ul className="onb-list">
        <li className="onb-list-item">
          <span className="onb-list-num">①</span>
          <span className="onb-list-text">
            진영 선택 후 <Highlight color="#f59e0b">변경 불가</Highlight> —
            신념을 가져라
          </span>
        </li>
        <li className="onb-list-item">
          <span className="onb-list-num">②</span>
          <span className="onb-list-text">
            양쪽 모두의 논거를 읽어라 — 적을 알아야 이긴다
          </span>
        </li>
        <li className="onb-list-item">
          <span className="onb-list-num">③</span>
          <span className="onb-list-text">
            승리 진영은 <Highlight>추가 전투력</Highlight>을 획득한다
          </span>
        </li>
      </ul>
    </>
  ),
};
