# 온보딩 구성 정책

> 방구석 철학자들 — 첫 방문 신규 사용자 대상 5단계 기밀 브리핑 온보딩의 구성 정책 및 규약.
> 최종 갱신: 2026-06-07

## 1. 개요

첫 방문 신규 사용자에게 **5단계 기밀 브리핑 모달**을 노출한다. 재방문 시에는 노출하지 않는다.

진입 흐름은 다음과 같다.

```
첫 방문
  → 온보딩 5스텝(또는 건너뛰기)
  → NicknameGenerator 닉네임 발급
  → 일반 사용
```

본 문서는 이 흐름을 구현하는 구성 요소들의 정책·규약을 기술한다. 사용자 노출 UI 스텝 내용이 아닌, 개발자 대상 구성 정책서다.

## 2. 진입·흐름 컨트롤러 정책

| 항목 | 값 |
|------|-----|
| 진입점 | `app/page.tsx` |
| 온보딩 노출 조건 | `isLoggedIn() === false` AND `hasSeenOnboarding === false` |
| 노출 트리거 방식 | `useEffect` 내에서 `openOnboarding()` 호출 |

### 2.1 두 store 직접 cross-call 금지

`onboardingStore.complete()` 안에서 `userStore.openNicknameModal()`을 호출하지 않는다. store 간 직접 의존은 결합도를 높여 독립 테스트를 방해한다.

**페이지(`app/page.tsx`)가 두 store를 조율한다.** 온보딩 완료/건너뛰기 콜백(`onComplete`)을 수신한 페이지가 닉네임 모달을 연다. store는 자신의 도메인만 담당한다.

## 3. 컴포넌트 구성 정책 (응집도)

온보딩 관련 컴포넌트는 `Components/Onboarding/` 한 디렉토리에 집중한다.

| 파일 | 역할 |
|------|------|
| `OnboardingFlow` | 셸 및 store 구독 단일 지점 |
| `OnboardingStep` | 스텝 시각 틀(dumb) |
| `BriefingHeader` | 상단 브리핑 헤더 |
| `NavigationButtons` | 이전/다음/건너뛰기 버튼 |
| `StepIndicator` | 단계 표시 탭 |
| `StepBadge` | 스텝 번호 배지 |
| `ClassifiedStamps` | 장식용 스탬프 |
| `onboarding.css` | 온보딩 전용 스타일 |

### 3.1 store 구독 단일화

store 구독은 `OnboardingFlow` 한 곳에서만 수행한다. 자식 컴포넌트는 dumb component — props만 받는다. grouped selector `useOnboardingProgress` / `useOnboardingActions` + `useShallow`를 사용한다.

### 3.2 공통 인프라 재사용

| 컴포넌트 | 제공 기능 |
|----------|-----------|
| `Components/Common/Modal` | esc/scroll-lock/backdrop/aria 위임, `closeOnBackdrop`/`closeOnEscape` 부분 잠금 |
| `Common/Highlight` | 텍스트 강조 |
| `Common/Button` | 공통 버튼 |

## 4. 스텝 콘텐츠 데이터 정책

콘텐츠 데이터는 `lib/data/onboarding/` 폴더에 위치한다.

| 파일 | 내용 |
|------|------|
| `constants.ts` | `TOTAL_STEPS=5`, `FINAL_STEP_INDEX=4` 단일 export |
| `steps/step1.tsx` ~ `steps/step5.tsx` | 스텝별 콘텐츠 1파일 |
| `index.ts` | `onboardingSteps` 배열 + constants 재export |

### 4.1 매직 넘버 단일 출처

매직 넘버(5, 4)는 `constants.ts` 한 곳에서만 정의한다. store, indicator, 테스트가 이 파일을 import한다. 코드 내 리터럴 `5` / `4` 중복 정의를 금지한다.

### 4.2 콘텐츠·뷰 분리

- 콘텐츠(데이터): `step.body: ReactNode` 주입
- 뷰(틀): `OnboardingStep`이 담당

`OnboardingStep`은 콘텐츠를 모르고 틀만 제공한다. 보조 데이터(`RULE_DEFINITIONS`, `HALL_OF_FAME` 등)는 해당 데이터를 사용하는 스텝 파일에 co-locate한다.

## 5. 스텝 헤더 고정 정책

긴 스텝 본문 스크롤 중에도 현재 단계 title 인지를 유지하기 위해 스텝 헤더를 스크롤 컨테이너 **밖**에 두어 고정한다. (sticky 방식은 스크롤 시작 시 title 이 살짝 밀리는 느낌이 있어 구조 분리로 대체.)

| 항목 | 값 |
|------|-----|
| 고정 헤더 컴포넌트 | `OnboardingStepHeader` (`onb-step-head`: meta + header + divider) |
| 스크롤 컨테이너 | `.onb-step-slide` — **헤더의 형제**, 헤더를 포함하지 않음 |
| 고정 방식 | 구조 분리 — 헤더는 shell 의 flex 자식(`flex-shrink:0`), 스크롤 컨테이너 밖 |
| 스크롤 대상 | `OnboardingStep` = `onb-content` (본문 영역만) |
| 배경 | 불투명 — shell 과 동일(`#0d0d0d`) |

헤더가 스크롤 컨테이너의 자손이 아니므로 본문 스크롤 시 title 은 전혀 밀리지 않는다. `useScrolledToBottom` 는 본문 컨테이너(`.onb-step-slide`) 기준으로 "끝 도달"을 측정하며 게이팅 수학은 불변.

## 6. 다음 버튼 게이팅 정책

스텝 콘텐츠를 끝까지 스크롤해야 "다음 »" 버튼이 활성화된다.

| 항목 | 값 |
|------|-----|
| hook | `useScrolledToBottom(currentStep)` |
| tolerance | viewport 6% |
| 잠금 회귀 조건 | `currentStep` 변경 시 자동 잠금 |
| race-free 보장 | `unlockKey === resetKey` derive |

step 전환 시 버튼은 자동으로 잠금 상태로 되돌아간다.

## 7. 강제 진행(부주의 dismiss 방지) 정책

온보딩 Modal은 배경 클릭과 ESC 키로 닫히지 않는다.

| 설정 | 값 |
|------|----|
| `closeOnBackdrop` | `false` |
| `closeOnEscape` | `false` |
| 종료 방법 | 명시 버튼(건너뛰기 / 입장하기)만 허용 |

배경·키보드 dismiss로 온보딩이 중단되는 것을 방지한다.

## 8. 영속 정책

`onboardingStore`는 Zustand persist 미들웨어를 사용한다.

| 항목 | 값 |
|------|-----|
| persist key | `hikikomori-onboarding` |
| 영속 항목 | `hasSeenOnboarding` (partialize) |
| 제외 항목 | `currentStep`, `isOpen` — 재방문 시 무의미 |

`skip()` ≡ `complete()` — 둘 다 `hasSeenOnboarding=true`로 수렴한다. 건너뛴 사용자도 재방문 시 온보딩이 재노출되지 않는다.

## 9. 접근성

| 요소 | 적용 |
|------|------|
| Modal | `role="dialog"` + `aria-modal` + `aria-label` |
| StepIndicator | `role="tablist"` / `aria-selected` |
| ClassifiedStamps | `aria-hidden` (장식 요소) |

### 9.1 알려진 한계 (향후 과제)

- focus trap 미구현
- Portal 미사용
- focus 복원 미흡 (자작 Modal 한계)

shadcn Dialog 마이그레이션 시 보강 예정 (별도 PR).

## 10. 테스트 정책

| 항목 | 값 |
|------|-----|
| 테스트 위치 | 대상 파일 옆 `__tests__/` |
| 프레임워크 | vitest + RTL + jsdom |
| 현재 커버리지 | 온보딩 관련 14파일 113테스트 green |

### 10.1 jsdom 레이아웃 한계 대응

jsdom은 실제 레이아웃(스크롤, sticky)을 계산하지 않는다. 아래 두 방식으로 대응한다.

- **scroll mock**: `HTMLElement.prototype` scroll 속성 일시 mock → `afterEach` 복원
- **구조 검증**: 클래스명·DOM 위치 존재 여부로 sticky 적용 여부를 검증
