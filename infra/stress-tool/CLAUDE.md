# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this directory.

## Overview

Locust 기반 Hikikomori Community API 부하/스트레스 테스트 도구. Docker Compose로 master + worker 분산 테스트를 실행한다.

## Tech Stack

- **Runtime:** Python 3.14
- **Load Testing:** Locust >= 2.32.0
- **Package Manager:** uv
- **Container:** Docker, Docker Compose

## Commands

```bash
# 의존성 설치 (로컬)
uv sync

# Docker 실행 (사전에 루트 docker compose up 필요)
docker compose up --build

# Worker 수 조절
docker compose up --build --scale worker=4

# 로컬 실행
uv run locust -f locustfile.py --host http://localhost:8080

# 태그 필터링
uv run locust --tags post       # 게시글만
uv run locust --tags comment    # 댓글만
uv run locust --tags read       # 읽기만
uv run locust --tags write      # 쓰기만
```

## Architecture

### 상속 구조

```
HttpUser (Locust 내장)
├── PostUser (abstract)           ← scenarios/post_scenarios.py
│   └── PostOnlyUser              ← locustfile.py
└── CommentUser (abstract)        ← scenarios/comment_scenarios.py
    └── CommentOnlyUser           ← locustfile.py

CommunityUser(PostUser, CommentUser)  ← 다중 상속으로 양쪽 task 통합
```

- `scenarios/` — `abstract = True`, 행동(task)만 정의하는 부품
- `locustfile.py` — `abstract = False`, 부품을 조합한 실행 가능한 가상 사용자

### 가상 사용자 비율 (weight)

| Class | Weight | 100명 기준 |
|-------|--------|-----------|
| CommunityUser | 6 (60%) | 60명 — 게시글 + 댓글 통합 |
| PostOnlyUser | 2 (20%) | 20명 — 게시글 전용 |
| CommentOnlyUser | 2 (20%) | 20명 — 댓글 전용 |

### Task 실행 비중

```
PostUser:    list_posts(5) > get_post(3) > create_post(2)       읽기 80% / 쓰기 20%
CommentUser: list_comments(5) > create_comment(3) > create_reply(2)  읽기 50% / 쓰기 50%
```

### 데이터 흐름

```
CommentUser.on_start() → 게시글 생성 → target_post_ids에 저장
create_post()          → created_post_ids에 ID 축적 → get_post()에서 조회
create_comment()       → created_comment_ids에 ID 축적 → create_reply()에서 대댓글
```

### Docker 네트워크

루트 `docker-compose.yml`의 `hikikomori_default` 네트워크에 외부 연결하여 `backend:8080`으로 통신.

## 대상 API

| Method | Path | Description |
|--------|------|-------------|
| GET | /api/posts | 게시글 목록 (페이징) |
| GET | /api/posts/{id} | 게시글 단건 조회 |
| POST | /api/posts | 게시글 생성 |
| GET | /api/posts/{id}/comments | 댓글 목록 조회 |
| POST | /api/posts/{id}/comments | 댓글/대댓글 생성 |

## 새 API 추가 시

1. `scenarios/` 에 새 시나리오 파일 생성 또는 기존 파일에 `@task` 메서드 추가
2. `scenarios/__init__.py`에 export 추가
3. 필요 시 `locustfile.py`에 새 User 클래스 정의

## Configuration

- 대상 호스트: `http://backend:8080` (docker-compose.yml에 상수로 정의)
- Worker 수: 기본 2 (docker-compose.yml의 `replicas`), `--scale worker=N`으로 조절
- Web UI: http://localhost:8089 에서 사용자 수, 생성 속도 실시간 조절
