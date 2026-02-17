# CLAUDE.md

이 파일은 Claude Code (claude.ai/code)가 이 저장소의 코드를 다룰 때 참고하는 가이드이다.

## 개발 명령어

```bash
# 의존성 설치
npm install

# 개발 서버 (포트 3000)
npm run dev

# 프로덕션 빌드 및 실행
npm run build
npm start

# 린트 (ESLint 9 flat config)
npm run lint

# 포맷팅 (Prettier)
npm run format
npm run format:check

# 테스트 (Vitest + React Testing Library)
npm test              # 전체 테스트 실행
npm run test:watch    # watch 모드

# 단일 테스트 파일 실행
npx vitest run lib/utils/__tests__/snowflake.test.ts

# 패턴 매칭으로 실행
npx vitest run nickname

# Docker로 실행 (루트 디렉토리에서)
docker compose up --build frontend
```

### Docker

- `Dockerfile`: 멀티스테이지 빌드 (deps → build → runtime), node:22-alpine 기반
- `next.config.ts`의 `output: "standalone"` 설정으로 경량 프로덕션 이미지 생성
- `.dockerignore`: node_modules, .next, 테스트 파일 등 제외

## 아키텍처

### Next.js App Router

- **Next.js 16.1.6** App Router 기반 — 모든 라우트는 `app/` 디렉토리에 위치
- 루트 레이아웃 (`app/layout.tsx`)에서 `next/font/google`을 통해 Geist Sans/Mono 폰트 로드
- `/` (`app/page.tsx`) — 닉네임 생성 화면 (`NicknameGenerator`, dynamic import로 SSR 비활성화)
- `/home` (`app/home/page.tsx`) — 메인 Home 화면 (`Navbar` + `Side` + `Body` + `Footer`)

### 컴포넌트 구조

```
Components/
├── Home/                          # Home 기능
│   ├── Home.tsx                   # 메인 레이아웃
│   ├── Navbar/Navbar.tsx          # 상단 네비게이션 (Zalgo 글리치 효과)
│   ├── Body/Body.tsx              # 콘텐츠 영역 (PostForm, PostCard)
│   ├── Side/Side.tsx              # 사이드 패널 (lg 이상 표시)
│   ├── Side/Component/Eyes.tsx    # 눈 깜빡임 애니메이션
│   └── Footer/Footer.tsx         # 하단 푸터
├── NicknameGenerator/             # 닉네임 생성 화면
│   ├── NicknameGenerator.tsx      # 메인 로직 (useReducer, Zustand)
│   ├── ArenaTheme.tsx             # "투기장" 테마 UI
│   └── arena-theme.css            # 투기장 테마 애니메이션
└── Common/                        # 공유/재사용 컴포넌트
    ├── Button/Button.tsx          # 기본 버튼 (create, delete, update, cancel)
    ├── Card/Card.tsx              # 카드 래퍼
    ├── Post/Post-Form/Post-Form.tsx   # 게시물 작성 폼
    ├── Post/Post-Card/Post-Card.tsx   # 게시물 카드 (투표, 댓글, 신고)
    └── Modals/Create.tsx          # 글 추가 모달
```

- 기본적으로 Server Component 사용; React hooks나 브라우저 API가 필요할 때만 `'use client'` 추가

### 상태 관리

- **Zustand 5** + `persist` 미들웨어 — 전역 상태 관리 + localStorage 영속화
- `lib/stores/userStore.ts` — 사용자 상태 (nickname, snowflakeId)
- `lib/types/user.ts` — UserState 인터페이스 (setUser, clearUser, isLoggedIn)
- persist key: `hikikomori-user` (localStorage)
- 재방문 시 localStorage에 데이터가 있으면 닉네임 생성 화면 건너뛰고 `/home`으로 리다이렉트

### 유틸리티 및 데이터

- `lib/utils/nickname.ts` — 닉네임 생성 (modifier + famousPerson 랜덤 조합)
- `lib/utils/snowflake.ts` — Snowflake ID 생성 (BigInt, EPOCH: 2026-01-01, 42bit timestamp + 10bit random + 12bit sequence)
- `lib/data/nicknameDictionaries.ts` — modifiers (52개) + famousPeople (62개)
- `lib/types/button.ts` — ButtonProps 인터페이스
- `lib/types/user.ts` — UserState 인터페이스
- `lib/stores/userStore.ts` — Zustand 전역 상태
- import 시 `@/lib/...` 사용

### 테스트

- **Vitest 4** + **React Testing Library** + **jsdom**
- 설정: `vitest.config.ts` (`@` 별칭 포함), `vitest.setup.ts` (localStorage mock)
- 테스트 파일 위치: 대상 파일과 동일 경로의 `__tests__/` 디렉토리
  - `lib/utils/__tests__/snowflake.test.ts`
  - `lib/utils/__tests__/nickname.test.ts`
  - `Components/NicknameGenerator/__tests__/NicknameGenerator.test.tsx`
  - `lib/stores/__tests__/userStore.test.ts`
- TDD 방식: 테스트 먼저 작성 → Red(실패) → 구현 → Green(통과)

### import 규칙

- 경로 별칭: `@/*`는 프로젝트 루트 (`./`)에 매핑
- 항상 절대 경로 import 사용: `@/Components/...`, `@/lib/types/...`

## 스타일링

- **Tailwind CSS v4** — `app/globals.css`에서 `@import "tailwindcss"`로 설정 (`tailwind.config.js` 없음)
- 테마 커스터마이징은 `globals.css`에서 `@theme inline` 지시어와 CSS 커스텀 프로퍼티 사용
- 다크 모드는 `prefers-color-scheme` 미디어 쿼리 사용
- PostCSS 플러그인: `@tailwindcss/postcss`
- 아이콘: lucide-react

## 코드 스타일

- TypeScript strict mode 활성화 (target: ES2017, moduleResolution: bundler)
- Prettier: single quotes, semicolons, trailing commas (es5), 2-space 들여쓰기, 80자 너비
- ESLint 9: `next/core-web-vitals` + `next/typescript` (flat config 형식)
