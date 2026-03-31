# Backend Refactoring V2 - Facade 아키텍처 설계

## 개요

Facade 기반 계층형 아키텍처를 완성하기 위한 백엔드 리팩토링.
기존 동작을 유지하면서 각 계층의 책임을 명확히 분리한다.

## 아키텍처

```
Controller ←── DTO ──── Facade ←── Entity ──── RepositoryImpl ←── JpaRepository
                         ↕DTO
                      Service (가공/변환/검증, Entity 반환)
```

### 핵심 규칙

- **Service는 Repository를 모른다** — Facade가 데이터를 가져와 Service에 넘기면, Service는 가공/검증만 수행
- **Facade 인터페이스 없음** — 구체 클래스로 직접 구현
- **RepositoryImpl이 JpaRepository를 감싼다** — DB 예외를 도메인 예외로 변환
- **트랜잭션은 Facade에서 관리** — `@Transactional`은 Facade에만 선언
- **Entity는 Facade 밖으로 나가지 않는다** — Controller에는 항상 DTO 반환

### 계층별 책임

| 계층 | 책임 | 의존 |
|------|------|------|
| Controller | HTTP 요청/응답, 형식 검증(`@Valid`) | Facade |
| Facade | 오케스트레이션, 복합 검증, 트랜잭션, DTO↔Entity 변환 | Service + RepositoryImpl |
| Service | 비즈니스 로직 (가공, 변환, 단일 관심사 검증) | 없음 (DTO record만 수신) |
| RepositoryImpl | DB 접근, DB 예외 → 도메인 예외 변환 | JpaRepository |

### 예외 처리 전략

| 계층 | 예외 책임 |
|------|----------|
| Service | 단일 관심사 검증 (소유권, 중첩 깊이 등) |
| Facade | 여러 Service/DB 결과를 조합한 복합 검증 |
| RepositoryImpl | DB 예외 → 도메인 예외 변환 (`getById` 등) |
| Controller | `@ControllerAdvice`로 HTTP 응답 변환 |

## DTO 설계

도메인 단위로 하나의 클래스에 inner record로 통합.

```java
public class PostDto {
    public record CreateRequest(
        @NotNull Long userId,
        @NotBlank String nickName,
        @NotBlank String title,
        @NotBlank String content,
        @NotBlank String tag
    ) {}

    public record UpdateRequest(
        @NotNull Long userId,
        @NotBlank String title,
        @NotBlank String content,
        @NotBlank String tag
    ) {}

    public record Response(UUID id, Long userId, String nickName,
                           String title, String content, String tag,
                           LocalDateTime createdAt, LocalDateTime updatedAt) {
        public static Response from(Post post) { ... }
    }
}
```

CommentDto도 동일 패턴 (CreateRequest, UpdateRequest, Response).

## VO 설계

기존 `service/vo/`의 PostCreate, PostUpdate, CommentCreate는 **삭제**.
VO는 Post + Comment를 묶어 "콘텐츠"로 반환할 때 사용.
배치 위치는 `dto/` 내 Response 또는 별도 `vo/` — 구현 시 결정.

## Repository 설계

JPA 인터페이스를 리네임하고, RepositoryImpl을 신규 생성.

```java
// PostJpaRepository — 쿼리만 (기존 PostRepository 리네임)
public interface PostJpaRepository extends JpaRepository<Post, UUID> {
    Page<Post> findByUserId(Long userId, Pageable pageable);
    long deleteByCreatedAtBetween(LocalDateTime startAt, LocalDateTime endAt);
}

// PostRepositoryImpl — DB 예외 처리
@Repository
@RequiredArgsConstructor
public class PostRepositoryImpl {
    private final PostJpaRepository jpaRepository;

    public Post getById(UUID id) {
        return jpaRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다: " + id));
    }
    public Post save(Post post) { return jpaRepository.save(post); }
    public Page<Post> findAll(Pageable pageable) { return jpaRepository.findAll(pageable); }
    public Page<Post> findByUserId(Long userId, Pageable pageable) { ... }
    public void deleteById(UUID id) { jpaRepository.deleteById(id); }
}
```

CommentRepositoryImpl도 동일 패턴.

## Service 설계

DTO record를 직접 수신. 검증 메서드는 `check` 네이밍 (Service에서 예외 발생).

```java
@Service
public class PostService {
    public Post buildPost(PostDto.CreateRequest request) {
        return Post.builder()
            .userId(request.userId()).nickName(request.nickName())
            .title(request.title()).content(request.content()).tag(request.tag())
            .build();
    }

    public void checkOwnership(Post post, Long userId, String operation) {
        if (!post.getUserId().equals(userId)) {
            throw new IllegalArgumentException("본인의 게시글만 " + operation + "할 수 있습니다");
        }
    }

    public void applyUpdate(Post post, PostDto.UpdateRequest request) {
        post.update(request.title(), request.content(), request.tag());
    }
}
```

CommentService: `buildComment`, `checkOwnership`, `checkNestingDepth`, `applyUpdate`, `applySoftDelete`.

## Facade 설계

```java
@Service
@RequiredArgsConstructor
public class PostFacade {
    private final PostService postService;
    private final CommentService commentService;
    private final PostRepositoryImpl postRepository;
    private final CommentRepositoryImpl commentRepository;

    public PostDto.Response createPost(PostDto.CreateRequest request) {
        Post post = postService.buildPost(request);
        return PostDto.Response.from(postRepository.save(post));
    }

    public void updatePost(UUID postId, PostDto.UpdateRequest request) {
        Post post = postRepository.getById(postId);
        postService.checkOwnership(post, request.userId(), "수정");
        postService.applyUpdate(post, request);
        postRepository.save(post);
    }

    @Transactional
    public void deletePost(UUID postId, Long userId) {
        Post post = postRepository.getById(postId);
        postService.checkOwnership(post, userId, "삭제");
        commentRepository.deleteAllByPostId(postId);
        postRepository.deleteById(postId);
    }
    // ... 기타 메서드
}
```

## 테스트 전략

### 기존 테스트 수정
- DTO/VO import 변경에 맞춰 모든 테스트 수정
- Service 테스트: DTO record 직접 전달, check 네이밍 반영
- Facade 테스트: RepositoryImpl mock으로 변경 (JpaRepository → RepositoryImpl)
- Controller 테스트: DTO import 변경

### ArchUnit 테스트 추가
build.gradle에 `com.tngtech.archunit:archunit-junit5` 의존성 추가.

검증 규칙:
1. **Controller → Facade만 의존** (Service, Repository 직접 접근 금지)
2. **Service는 Repository를 모른다** (repository 패키지 import 금지)
3. **Facade 밖으로 Entity 노출 금지** (Controller가 domain 패키지 import 금지)
4. **RepositoryImpl만 JpaRepository 사용** (Facade/Service가 JpaRepository 직접 접근 금지, batch 제외)

## 변경 파일 목록

### 삭제
- `dto/request/PostCreateRequest.java`
- `dto/request/PostUpdateRequest.java`
- `dto/request/CommentCreateRequest.java`
- `dto/request/CommentUpdateRequest.java`
- `dto/response/PostResponse.java`
- `dto/response/CommentResponse.java`
- `service/vo/PostCreate.java`
- `service/vo/PostUpdate.java`
- `service/vo/CommentCreate.java`

### 신규
- `dto/PostDto.java`
- `dto/CommentDto.java`
- `repository/PostJpaRepository.java` (리네임)
- `repository/CommentJpaRepository.java` (리네임)
- `repository/PostRepositoryImpl.java`
- `repository/CommentRepositoryImpl.java`
- ArchUnit 테스트 클래스

### 수정
- `facade/PostFacade.java`
- `service/PostService.java`
- `service/CommentService.java`
- `controller/PostController.java`
- `batch/tasklet/*.java` (JpaRepository import 변경)
- `build.gradle` (ArchUnit 의존성 추가)
- 기존 테스트 파일 전체

## 범위 제외
- Batch 로직 변경 (import 변경만)
- Domain 엔티티 변경
- API 스펙 변경 (기존 동작 유지)
