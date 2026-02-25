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
- 테스트: H2 인메모리 DB (create-drop)

## 아키텍처

계층형 아키텍처: **Controller → Service → Repository → Entity** + Batch

```
org.hikikomori.community
├── CommunityApplication.java  # @SpringBootApplication, @EnableScheduling
├── controller/
│   ├── PostController.java    # REST API (@RestController, /api/posts)
│   ├── GlobalController.java  # 배치 관리 API (/api/global/cleanup)
│   └── data/                  # DTO (Java 클래스, from() 팩토리 메서드 패턴)
├── domain/
│   ├── Post.java              # JPA 엔티티
│   ├── Comment.java           # JPA 엔티티 (자기참조 관계)
│   └── UUIDGenerator.java     # TimeBasedEpoch UUID 생성 유틸리티
├── repository/                # Spring Data JPA Repository
├── service/
│   ├── PostService.java       # 게시글/댓글 비즈니스 로직
│   └── BatchService.java      # 배치 작업 실행
└── batch/
    ├── BatchScheduler.java    # @Scheduled (매일 자정 cron)
    ├── job/
    │   └── CleanupJobConfig.java  # 배치 Job (2 Step: Comment → Post 순서 삭제)
    └── tasklet/
        ├── PostCleanupTasklet.java     # 전일 이전 게시글 삭제
        └── CommentCleanupTasklet.java  # 전일 이전 댓글 삭제
```

## API 엔드포인트

| 메서드 | 경로 | 설명 | 상태 코드 |
|--------|------|------|----------|
| GET | `/api/posts` | 게시글 목록 (페이징, 기본 20개) | 200 |
| GET | `/api/posts/{id}` | 게시글 단건 조회 | 200 |
| POST | `/api/posts` | 게시글 생성 | 201 |
| GET | `/api/posts/{id}/comments` | 댓글 목록 (루트 댓글만) | 200 |
| POST | `/api/posts/{id}/comments` | 댓글/대댓글 생성 | 201 |
| POST | `/api/global/cleanup` | 배치 정리 작업 수동 실행 | 200 |

## 컨벤션

- **DTO:** Request/Response는 Java 클래스 사용 (@Getter, @Builder), Response에 `static from(Entity)` 팩토리 메서드
- **DI:** 생성자 주입 (Lombok `@RequiredArgsConstructor`)
- **트랜잭션:** Service의 쓰기 메서드에 `@Transactional`, 읽기 메서드는 트랜잭션 없음
- **엔티티:** Lombok `@Getter`, `@NoArgsConstructor`, `@Builder` 사용. ID는 UUID (TimeBasedEpoch 자동 생성)
- **예외:** `IllegalArgumentException` + 한국어 메시지 (예: `"게시글을 찾을 수 없습니다"`, `"대댓글에는 답글을 달 수 없습니다"`)
- **검증:** Jakarta Validation (`@NotBlank`) + 컨트롤러에서 `@Valid`
- **로깅:** Lombok `@Slf4j`, 한국어 로그 메시지
- **테스트:** 단위 테스트는 Mockito + BDD 스타일(`given/when/then`), 통합 테스트는 `@DataJpaTest` (H2), 테스트명/DisplayName 한국어
- **API 경로:** `/api/{resource}` 패턴

## 도메인 모델

- **Post:** id(UUID), userId(Long), nickName(String), title(String), content(String), createdAt(LocalDateTime)
- **Comment:** id(UUID), userId(Long), nickName(String), content(String), createdAt(LocalDateTime), post(ManyToOne LAZY), parent(self-referencing ManyToOne LAZY), children(OneToMany CASCADE ALL) — 최대 2단계 중첩(댓글 + 대댓글)

## 배치 처리

- **스케줄:** 매일 자정 (`cron="0 0 0 * * *"`)
- **Job:** cleanupJob — 2개 Step (Comment 먼저 삭제 → Post 삭제, 외래키 제약 고려)
- **기준:** `today.atStartOfDay()` 이전 데이터 삭제
- **수동 실행:** `POST /api/global/cleanup`
- **설정:** `spring.batch.job.enabled=false` (자동 실행 비활성화, 스케줄러 통해서만 실행)

## 알려진 이슈

- compose.yaml의 DB 자격증명(myuser/secret/mydatabase)과 application.yaml(postgres/postgres/community)이 불일치 — Spring Boot Docker Compose 통합이 자동 연결하므로 현재 동작에는 영향 없음
