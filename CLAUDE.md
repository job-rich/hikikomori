# CLAUDE.md

이 파일은 Claude Code (claude.ai/code)가 이 저장소의 코드를 다룰 때 참고하는 가이드이다.

## 프로젝트 개요

Hikikomori는 백엔드/프론트엔드가 분리된 모노레포 구조의 커뮤니티 애플리케이션이다. 커밋 메시지는 한국어를 사용한다.

하위 디렉토리별 상세 가이드:
- **Backend:** [`backend/CLAUDE.md`](backend/CLAUDE.md)
- **Frontend:** [`frontend/CLAUDE.md`](frontend/CLAUDE.md)
- **Stress Test:** [`infra/stress-tool/CLAUDE.md`](infra/stress-tool/CLAUDE.md)

## 기술 스택

- **Backend:** Spring Boot 4.1.0-SNAPSHOT, Java 25, Gradle 9.3.0, PostgreSQL, Spring Data JPA, Spring Batch, Lombok, springdoc-openapi
- **Frontend:** Next.js 16.1.6, React 19.2.3, TypeScript (strict mode), Tailwind CSS v4, Zustand 5, lucide-react
- **Stress Test:** Locust (Python 3.14), Docker, uv

## 개발 명령어

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
# 의존성 설치
cd frontend && npm install

# 개발 서버 (포트 3000)
npm run dev --prefix frontend

# 프로덕션 빌드 및 실행
npm run build --prefix frontend
npm start --prefix frontend

# 린트 및 포맷팅
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

### Docker Compose (전체 서비스)

```bash
# 전체 서비스 빌드 및 실행 (postgres + backend + frontend)
docker compose up --build

# 백그라운드 실행
docker compose up --build -d

# 특정 서비스만 실행
docker compose up --build frontend

# 종료
docker compose down

# 볼륨 포함 종료 (DB 데이터 삭제)
docker compose down -v
```

- **postgres**: `localhost:5432` (DB: community, User: postgres)
- **backend**: `localhost:8080` (Swagger UI: `http://localhost:8080/swagger-ui.html`)
- **frontend**: `localhost:3000` (standalone 모드)

## 아키텍처

### Backend (Spring Boot MVC)

계층형 아키텍처: **Controller → Service → Repository → Entity** + Batch
- 패키지 루트: `org.hikikomori.community`
- 배치 정리 작업: 매일 자정 전일 데이터 자동 삭제 (Comment → Post 순서)
- 상세 아키텍처 및 컨벤션은 [`backend/CLAUDE.md`](backend/CLAUDE.md) 참고

### Frontend (Next.js App Router)

- App Router 사용 (Pages Router 아님) — 컴포넌트는 `app/` 디렉토리에 위치
- 클라이언트 컴포넌트는 `'use client'` 지시어로 표시
- 컴포넌트 구조: `Components/{Feature}/{Component}.tsx`
- 전역 상태: Zustand + persist 미들웨어 (localStorage 영속화)
- 경로 별칭: `@/*`는 프로젝트 루트에 매핑
- Prettier 설정: single quotes, semicolons, trailing commas (es5), 2-space 들여쓰기, 80자 너비

## CI/CD

- **트리거:** main 브랜치 Pull Request
- **frontend-test:** Node 22, `npm ci` → `npm test` (chore 브랜치 제외)
- **backend-test:** Java 25 (temurin), Gradle 캐시, `./gradlew test` (chore 브랜치 제외)
