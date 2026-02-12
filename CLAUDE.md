# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Hikikomori는 백엔드/프론트엔드가 분리된 모노레포 구조의 커뮤니티 애플리케이션이다. 커밋 메시지는 한국어를 사용한다.

하위 디렉토리별 상세 가이드:
- **Backend:** [`backend/CLAUDE.md`](backend/CLAUDE.md)
- **Stress Test:** [`infra/stress-tool/CLAUDE.md`](infra/stress-tool/CLAUDE.md)

## Tech Stack

- **Backend:** Spring Boot 4.1.0-SNAPSHOT, Java 25, Gradle 9.3.0, PostgreSQL, Spring Data JPA, Lombok
- **Frontend:** Next.js 16.1.6, React 19, TypeScript (strict mode), Tailwind CSS v4, PostCSS
- **Stress Test:** Locust (Python), Docker, uv

## Development Commands

### Backend (`/backend`)

```bash
# 빌드
./backend/gradlew -p backend build

# 테스트 실행
./backend/gradlew -p backend test

# 애플리케이션 실행 (Spring Boot Docker Compose가 PostgreSQL 자동 시작)
./backend/gradlew -p backend bootRun
```

### Frontend (`/frontend`)

```bash
# Install dependencies
cd frontend && npm install

# Dev server (port 3000)
npm run dev --prefix frontend

# Production build & start
npm run build --prefix frontend
npm start --prefix frontend

# Lint & format
npm run lint --prefix frontend
npm run format --prefix frontend
npm run format:check --prefix frontend
```

### Stress Test (`/infra/stress-tool`)

```bash
# 스트레스 테스트 실행 (사전에 루트 docker compose up 필요, Web UI: http://localhost:8089)
docker compose -f infra/stress-tool/docker-compose.yml up --build

# Worker 수 조절
docker compose -f infra/stress-tool/docker-compose.yml up --build --scale worker=4
```

## Architecture

### Backend (Spring Boot MVC)

Layered architecture: **Controller → Service → Repository → Entity**
- Package root: `org.hikikomori.community`
- 상세 아키텍처 및 컨벤션은 [`backend/CLAUDE.md`](backend/CLAUDE.md) 참고

### Frontend (Next.js App Router)

- Uses App Router (not Pages Router) — components in `app/` directory
- Client components marked with `'use client'` directive
- Component organization: `Components/{Feature}/{Component}.tsx`
- Path alias: `@/*` maps to project root
- Prettier config: single quotes, semicolons, trailing commas (es5), 2-space tabs, 80 char width
