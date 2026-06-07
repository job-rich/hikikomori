# CLAUDE.md

이 파일은 Claude Code (claude.ai/code)가 이 저장소의 코드를 다룰 때 참고하는 가이드이다.

## 빌드 및 테스트 명령어

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

## 기술 스택

- Spring Boot 4.1.0-SNAPSHOT, Java 25, Gradle 9.3.0
- PostgreSQL (localhost:5432/community, user: postgres, pw: postgres)
- Spring Data JPA (ddl-auto: update), Spring Batch, Lombok
- springdoc-openapi 3.0.1 (Swagger UI: `/swagger-ui.html`)
- com.fasterxml.uuid:java-uuid-generator:5.2.0 (TimeBasedEpoch UUID)
- 테스트: H2 인메모리 DB (create-drop), ArchUnit 1.4.1

## 아키텍처

Facade 기반 계층형 아키텍처: **Controller → Facade → Service + RepositoryImpl → JpaRepository → Entity** + Batch

```
Controller
  └→ Facade (구체 클래스, 오케스트레이션 + 트랜잭션 경계)
      ├→ Service (구체 클래스, 가공/변환/검증, Repository 모름)
      └→ RepositoryImpl (구체 클래스, DB 접근 + 모든 DB 예외 처리)
           └→ JpaRepository (Spring Data JPA interface)
```

**핵심 규칙:**
- **Service는 Repository를 모른다** — Facade가 Repository에서 데이터를 가져와 Service에 넘기면, Service는 가공만 수행
- **Facade 인터페이스는 사용하지 않는다** — 구체 클래스로 직접 구현
- **RepositoryImpl이 JpaRepository를 감싼다** — DB 예외를 도메인 예외로 변환하는 책임
- **트랜잭션은 Facade에서 관리** — `@Transactional`은 Facade 구현체에만 선언

### 계층별 책임

| 계층 | 책임 | 의존 |
|------|------|------|
| Controller | HTTP 요청/응답, 형식 검증(`@Valid`) | Facade |
| Facade | 오케스트레이션(호출·조합), 트랜잭션 경계 — **직접 예외를 던지지 않음** | Service + RepositoryImpl |
| Service | 비즈니스 로직 (가공/변환/검증·예외, 엔티티 빌드, 외부 의존 없음) | DTO·값 수신 |
| RepositoryImpl | DB 접근(가져오기만), 데이터 없으면 도메인 예외 | JpaRepository |

### 패키지 구조

```
org.hikikomori.community
├── controller/           # REST API (@RestController)
├── dto/
│   ├── PostDto.java      # Post DTO 통합 (CreateRequest, UpdateRequest, Response)
│   └── CommentDto.java   # Comment DTO 통합 (CreateRequest, UpdateRequest, Response)
├── facade/               # Facade (구체 클래스, 오케스트레이션)
├── service/              # Service (가공/변환/검증, DTO 직접 수신)
├── repository/
│   ├── *JpaRepository    # Spring Data JPA interface (쿼리만)
│   └── *RepositoryImpl   # DB 접근 + 예외 처리
├── domain/               # JPA 엔티티
├── util/                 # 유틸리티 (UUIDGenerator)
└── batch/                # 배치 (Scheduler, Job, Tasklet)
```

## API 엔드포인트

| 메서드 | 경로 | 설명 | 상태 코드 |
|--------|------|------|----------|
| GET | `/api/posts` | 게시글 목록 (페이징, 기본 6개) | 200 |
| GET | `/api/posts/{id}` | 게시글 단건 조회 | 200 |
| GET | `/api/posts/my/{userId}` | 내 게시글 목록 (페이징) | 200 |
| POST | `/api/posts` | 게시글 생성 | 201 |
| PATCH | `/api/posts/{id}` | 게시글 수정 | 204 |
| DELETE | `/api/posts/{id}` | 게시글 삭제 | 204 |
| GET | `/api/posts/{id}/comments` | 댓글 목록 (루트 댓글만) | 200 |
| POST | `/api/posts/{id}/comments` | 댓글/대댓글 생성 | 201 |
| PATCH | `/api/posts/{id}/comments/{commentId}` | 댓글 수정 | 204 |
| DELETE | `/api/posts/{id}/comments/{commentId}` | 댓글 삭제 (소프트) | 204 |
| POST | `/api/batch/purge?startDate=&endDate=` | 배치 정리 작업 수동 실행 | 200 |

## 컨벤션

- **DTO:** 도메인 단위 통합 (`PostDto`, `CommentDto`), inner record로 CreateRequest/UpdateRequest/Response 포함. Response에 `static from(Entity)` 팩토리 메서드
- **계층 간 전달:** DTO record를 Service에 직접 전달 (별도 VO 없음)
- **DI:** 생성자 주입 (Lombok `@RequiredArgsConstructor`)
- **트랜잭션:** Facade에 `@Transactional` (Service와 Repository는 트랜잭션 무관)
- **엔티티:** Lombok `@Getter`, `@NoArgsConstructor`, `@Builder` 사용. ID는 UUID (TimeBasedEpoch 자동 생성)
- **예외 처리 전략:**
  - **Service: 모든 비즈니스 검증·예외를 담당** (소유권, 자기신고, 중복, 밴, 작성자 일치 등) — `check` 네이밍, 예외 throw
  - RepositoryImpl: **값을 가져오기만 하고, 데이터가 없으면** 도메인 예외 (`getById`/`getVisibleById` → `IllegalArgumentException`)
  - **Facade는 직접 예외를 던지지 않는다** — Repository에서 값(존재 여부 등)을 조회해 Service에 넘기고, 검증·예외 판단은 Service에 위임
  - Controller: 예외는 `@ResponseStatus`를 단 도메인 예외(또는 `@ControllerAdvice`)로 HTTP 상태 매핑
- **검증:**
  - 형식 검증(`@NotBlank`, `@Size`): Controller/DTO (`@Valid`)
  - 비즈니스 검증(중복·존재 여부 포함): **Service**. DB 의존 검증도 Facade가 Repository에서 boolean/값을 조회해 Service 검증 메서드에 넘긴다
- **엔티티 생성:** Service의 `buildXxx`에서 조립 (예: `buildPost`, `buildReport`, `buildBan`). Facade는 빌드하지 않는다
- **로깅:** Lombok `@Slf4j`, 한국어 로그 메시지
- **테스트:**
  - Service: 순수 단위 테스트 (mock 불필요, 입력→출력 검증)
  - RepositoryImpl: 단위 테스트 (JpaRepository mock)
  - Facade: 조합 테스트 (RepositoryImpl mock)
  - Controller: API 테스트 (Facade mock, `@WebMvcTest`)
  - ArchUnit: 아키텍처 규칙 검증 (계층 의존성)
  - 테스트명/DisplayName 한국어, BDD 스타일(`given/when/then`)
- **API 경로:** `/api/{resource}` 패턴
- **복잡도 관리:** Facade에 Service/Repository 주입이 10개 이상이면 도메인 분리 신호

## 도메인 모델

- **Post:** id(UUID), userId(Long), nickName(String), title(String), content(TEXT), tag(PostTag enum), commentCount(@Formula, 읽기 전용), createdAt, updatedAt
  - **commentCount:** `@Formula("(SELECT COUNT(*) FROM comment c WHERE c.post_id = id)")` — DB 서브쿼리로 자동 계산, 스냅샷 아님. 정렬: `?sort=commentCount,desc`
- **PostTag:** PHILOSOPHY(철학), SOCIETY(사회), POLITICS(정치), ECONOMY(경제), CULTURE(문화), DAILY(일상), ETC(기타) — `@Enumerated(EnumType.STRING)`
- **Comment:** id(UUID), userId(Long), nickName(String), content(TEXT), createdAt, updatedAt, deletedAt, post(ManyToOne LAZY), parent(self-referencing ManyToOne LAZY), children(OneToMany CASCADE ALL) — 최대 3단계 중첩(댓글 → 대댓글 → 대대댓글)
  - **소프트 삭제:** `deletedAt`만 설정, content 원본 유지. Response에서 삭제된 댓글의 content는 null로 치환 (DB 원본 보장, 패킷 노출 방지)

## 배치 처리

- **스케줄:** 매일 자정 (`cron="0 0 0 * * *"`)
- **Job:** cleanupJob — 2개 Step (Comment 먼저 삭제 → Post 삭제, 외래키 제약 고려)
- **기준:** `today.atStartOfDay()` 이전 데이터 삭제
- **수동 실행:** `POST /api/batch/purge?startDate=YYYY-MM-DD&endDate=YYYY-MM-DD`
- **설정:** `spring.batch.job.enabled=false` (자동 실행 비활성화, 스케줄러 통해서만 실행)

## 알려진 이슈

- compose.yaml의 DB 자격증명(myuser/secret/mydatabase)과 application.yaml(postgres/postgres/community)이 불일치 — Spring Boot Docker Compose 통합이 자동 연결하므로 현재 동작에는 영향 없음
