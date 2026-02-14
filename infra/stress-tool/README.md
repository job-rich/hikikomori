# Locust 스트레스 테스트

Hikikomori Community API 부하 테스트 도구.

## 사전 조건

루트 `docker-compose.yml`로 백엔드가 실행 중이어야 합니다:

```bash
# 프로젝트 루트에서
docker compose up -d
```

## 실행 방법

### Docker Compose (권장)

```bash
cd infra/stress-tool

# 기본 실행 (master 1 + worker 2)
docker compose up --build

# Worker 수 조절
docker compose up --build --scale worker=4
```

Web UI: http://localhost:8089

### 로컬 실행 (uv)

```bash
cd infra/stress-tool
uv sync
uv run locust -f locustfile.py --host http://localhost:8080
```

## 동작 원리

### 상속 구조

```
HttpUser (Locust 내장)
├── PostUser (abstract)           ← scenarios/post_scenarios.py
│   └── PostOnlyUser              ← locustfile.py
└── CommentUser (abstract)        ← scenarios/comment_scenarios.py
    └── CommentOnlyUser           ← locustfile.py

CommunityUser(PostUser, CommentUser)  ← 다중 상속으로 양쪽 task 통합
```

- `scenarios/` 의 클래스는 `abstract = True` — **행동(task)만 정의**하는 부품
- `locustfile.py` 의 클래스는 `abstract = False` — 부품을 조합한 **실행 가능한 가상 사용자**

### 가상 사용자 비율

100명 생성 시:

```
CommunityUser  (weight 6) → 60명  게시글 + 댓글 모두
PostOnlyUser   (weight 2) → 20명  게시글만
CommentOnlyUser(weight 2) → 20명  댓글만
```

### Task 실행 비중

각 사용자가 반복할 때마다 비중에 따라 task를 랜덤 선택합니다:

```
PostUser 의 task:
  list_posts(5)  ████████████████████  50%  GET  /api/posts
  get_post(3)    ████████████          30%  GET  /api/posts/{id}
  create_post(2) ████████              20%  POST /api/posts

CommentUser 의 task:
  list_comments(5)  ████████████████████  50%  GET  /api/posts/{id}/comments
  create_comment(3) ████████████          30%  POST /api/posts/{id}/comments
  create_reply(2)   ████████              20%  POST /api/posts/{id}/comments (parentId)
```

### 데이터 흐름

```
1. CommentUser.on_start()
   └→ POST /api/posts (게시글 생성) → target_post_ids에 저장

2. 테스트 루프
   ├→ create_post()    → created_post_ids에 ID 축적
   ├→ get_post()       → created_post_ids에서 랜덤 선택하여 조회
   ├→ create_comment() → created_comment_ids에 ID 축적
   └→ create_reply()   → created_comment_ids에서 랜덤 선택하여 대댓글
```

### 태그 필터링

`@tag` 데코레이터로 **메서드 단위** 필터링:

```bash
uv run locust --tags post       # @tag("post") 붙은 메서드만
uv run locust --tags comment    # @tag("comment") 붙은 메서드만
uv run locust --tags read       # @tag("read") 붙은 메서드만
uv run locust --tags write      # @tag("write") 붙은 메서드만
```

## 구조

```
stress-tool/
├── locustfile.py            # 메인 — 실행 가능한 User 클래스 (조합)
├── scenarios/
│   ├── post_scenarios.py    # PostUser: 게시글 목록/단건/생성
│   └── comment_scenarios.py # CommentUser: 댓글 조회/생성/대댓글
├── docker-compose.yml       # master + worker 분산 구성
├── Dockerfile               # Python 3.12 + uv + Locust
└── pyproject.toml           # uv 의존성 관리
```
