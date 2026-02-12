# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Test Commands

```bash
# 빌드
./backend/gradlew -p backend build

# 테스트 전체 실행
./backend/gradlew -p backend test

# 단일 테스트 클래스 실행
./backend/gradlew -p backend test --tests "org.hikikomori.community.service.PostServiceTest"

# 단일 테스트 메서드 실행
./backend/gradlew -p backend test --tests "org.hikikomori.community.service.PostServiceTest.게시글을_생성할_수_있다"

# 애플리케이션 실행 (Spring Boot Docker Compose가 PostgreSQL 자동 시작)
./backend/gradlew -p backend bootRun

# PostgreSQL만 별도 실행
docker-compose -f backend/compose.yaml up

# Docker로 실행 (루트 디렉토리에서)
docker compose up --build backend
```

### Docker

- `Dockerfile`: 멀티스테이지 빌드 (build → runtime), eclipse-temurin:25 기반
- 빌드 시 테스트 스킵 (`-x test`), bootJar로 실행 가능 JAR 생성
- `docker-compose.yml`에서 postgres 의존성 설정 (healthcheck 대기 후 기동)

## Tech Stack

- Spring Boot 4.1.0-SNAPSHOT, Java 25, Gradle 9.3.0
- PostgreSQL (localhost:5432/community, user: postgres, pw: postgres)
- Spring Data JPA (ddl-auto: update), Lombok

## Architecture

Layered architecture: **Controller → Service → Repository → Entity**

```
org.hikikomori.community
├── controller/          # REST 컨트롤러 (@RestController)
│   └── data/            # DTO (Java record로 구현, from() 팩토리 메서드 패턴)
├── domain/              # JPA 엔티티 (@Entity, Lombok @Builder/@Getter)
├── repository/          # Spring Data JPA Repository
└── service/             # 비즈니스 로직 (@Service, @Transactional)
```

## Conventions

- **DTO:** Request/Response는 Java record 사용, Response에 `static from(Entity)` 팩토리 메서드
- **DI:** 생성자 주입 (Lombok `@RequiredArgsConstructor`)
- **트랜잭션:** Service의 쓰기 메서드에 `@Transactional`
- **엔티티:** Lombok `@Getter`, `@NoArgsConstructor`, `@Builder` 사용. ID는 `GenerationType.IDENTITY`
- **예외 메시지:** 한국어 사용 (예: `"게시글을 찾을 수 없습니다."`)
- **테스트:** 단위 테스트는 Mockito + BDD 스타일(`given/when/then`), 통합 테스트는 `@SpringBootTest`, 테스트명/DisplayName 한국어
- **API 경로:** `/api/{resource}` 패턴 (예: `/api/posts`, `/api/posts/{id}/comments`)

## Domain Model

- **Post:** id, title, content, createdAt
- **Comment:** id, content, createdAt, post(ManyToOne), parent(self-referencing ManyToOne), children(OneToMany) — 최대 2단계 중첩(댓글 + 대댓글)

## Known Issues

- compose.yaml의 DB 자격증명(myuser/secret/mydatabase)과 application.yaml(postgres/postgres/community)이 불일치 — Spring Boot Docker Compose 통합이 자동 연결하므로 현재 동작에는 영향 없음
