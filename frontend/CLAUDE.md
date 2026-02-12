# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Development Commands

```bash
# Install dependencies
npm install

# Dev server (port 3000)
npm run dev

# Production build & start
npm run build
npm start

# Lint (ESLint 9 flat config)
npm run lint

# Format (Prettier)
npm run format
npm run format:check

# Test (Vitest + React Testing Library)
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

## Architecture

### Next.js App Router

- **Next.js 16.1.6** with App Router — all routes live in `app/`
- Root layout (`app/layout.tsx`) loads Geist Sans/Mono fonts via `next/font/google`
- `/` (`app/page.tsx`) — 닉네임 생성 화면 (`NicknameGenerator`)
- `/home` (`app/home/page.tsx`) — 메인 Home 화면 (`Navbar` + `Body` + `Footer`)

### Component Organization

```
Components/
  {Feature}/          # Feature-specific components
    {Component}.tsx
  Common/             # Shared/reusable components
    {ComponentName}/
      {ComponentName}.tsx
```

- `Components/Home/` — Home feature (`Home.tsx`, `Navbar/`, `Body/`, `Footer/`)
- `Components/NicknameGenerator/` — 닉네임 생성 화면
- `Components/Common/` — Shared components (`Button/`, `Modals/`)
- Server Components by default; only add `'use client'` when React hooks or browser APIs are needed (e.g., `Body.tsx` uses `useState` for modal state)

### Utilities & Data

- `lib/utils/` — 유틸리티 함수 (`nickname.ts`, `snowflake.ts`)
- `lib/data/` — 정적 데이터 (`nicknameDictionaries.ts`)
- `lib/types/` — 공유 타입 정의 (`button.ts`)
- Import with `@/lib/...`

### Testing

- **Vitest** + **React Testing Library** + **jsdom**
- 설정: `vitest.config.ts`, `vitest.setup.ts`
- 테스트 파일 위치: 대상 파일과 동일 경로의 `__tests__/` 디렉토리
  - `lib/utils/__tests__/snowflake.test.ts`
  - `lib/utils/__tests__/nickname.test.ts`
  - `Components/NicknameGenerator/__tests__/NicknameGenerator.test.tsx`
- TDD 방식: 테스트 먼저 작성 → Red(실패) → 구현 → Green(통과)

### Import Conventions

- Path alias: `@/*` maps to project root (`./`)
- Always use absolute imports: `@/Components/...`, `@/lib/types/...`

## Styling

- **Tailwind CSS v4** — configured via `@import "tailwindcss"` in `app/globals.css` (no `tailwind.config.js`)
- Theme customization uses `@theme inline` directive and CSS custom properties in `globals.css`
- Dark mode via `prefers-color-scheme` media query
- PostCSS plugin: `@tailwindcss/postcss`

## Code Style

- TypeScript strict mode enabled
- Prettier: single quotes, semicolons, trailing commas (es5), 2-space indent, 80 char width
- ESLint: `eslint-config-next/core-web-vitals` + `eslint-config-next/typescript` (flat config format)
