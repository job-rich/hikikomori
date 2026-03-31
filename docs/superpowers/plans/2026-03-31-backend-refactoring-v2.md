# Backend Refactoring V2 구현 계획

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Facade 기반 계층형 아키텍처 완성 — DTO 통합, RepositoryImpl 도입, Service 책임 명확화, ArchUnit 검증

**Architecture:** Controller → Facade → Service(가공/변환/검증) + RepositoryImpl(DB 예외 처리) → JpaRepository. Entity는 Facade 밖으로 나가지 않고, 계층 간 데이터 전달은 DTO record.

**Tech Stack:** Spring Boot 4.1.0-SNAPSHOT, Java 25, ArchUnit, H2 (test)

**Spec:** `docs/superpowers/specs/2026-03-31-backend-refactoring-v2-design.md`

---

## 파일 구조 맵

### 신규 생성
| 파일 | 책임 |
|------|------|
| `dto/PostDto.java` | Post DTO 통합 (CreateRequest, UpdateRequest, Response) |
| `dto/CommentDto.java` | Comment DTO 통합 (CreateRequest, UpdateRequest, Response) |
| `repository/PostJpaRepository.java` | 기존 PostRepository 리네임 |
| `repository/CommentJpaRepository.java` | 기존 CommentRepository 리네임 |
| `repository/PostRepositoryImpl.java` | Post DB 접근 + 예외 처리 |
| `repository/CommentRepositoryImpl.java` | Comment DB 접근 + 예외 처리 |
| `test/.../architecture/ArchitectureTest.java` | ArchUnit 아키텍처 규칙 검증 |

### 삭제
| 파일 | 사유 |
|------|------|
| `dto/request/PostCreateRequest.java` | PostDto.CreateRequest로 통합 |
| `dto/request/PostUpdateRequest.java` | PostDto.UpdateRequest로 통합 |
| `dto/request/CommentCreateRequest.java` | CommentDto.CreateRequest로 통합 |
| `dto/request/CommentUpdateRequest.java` | CommentDto.UpdateRequest로 통합 |
| `dto/response/PostResponse.java` | PostDto.Response로 통합 |
| `dto/response/CommentResponse.java` | CommentDto.Response로 통합 |
| `service/vo/PostCreate.java` | 삭제 (VO 용도 변경) |
| `service/vo/PostUpdate.java` | 삭제 |
| `service/vo/CommentCreate.java` | 삭제 |
| `repository/PostRepository.java` | PostJpaRepository로 리네임 |
| `repository/CommentRepository.java` | CommentJpaRepository로 리네임 |

### 수정
| 파일 | 변경 내용 |
|------|----------|
| `build.gradle` | ArchUnit 의존성 추가 |
| `service/PostService.java` | DTO 직접 수신, check 네이밍, VO 의존 제거 |
| `service/CommentService.java` | 동일 |
| `facade/PostFacade.java` | RepositoryImpl 주입, 통합 DTO 사용 |
| `controller/PostController.java` | 통합 DTO import |
| `batch/.../PostPurgeTasklet.java` | JpaRepository import 변경 |
| `batch/.../CommentPurgeTasklet.java` | JpaRepository import 변경 |
| 테스트 파일 전체 (8개) | 위 변경에 맞춰 수정 |

---

## Task 1: build.gradle — ArchUnit 의존성 추가

**Files:**
- Modify: `backend/build.gradle:41`

- [ ] **Step 1: ArchUnit 의존성 추가**

`build.gradle`의 dependencies 블록에 추가:
```groovy
testImplementation 'com.tngtech.archunit:archunit-junit5:1.4.0'
```

- [ ] **Step 2: 의존성 다운로드 확인**

Run: `./backend/gradlew -p backend dependencies --configuration testCompileClasspath | grep archunit`
Expected: `com.tngtech.archunit:archunit-junit5:1.4.0`

- [ ] **Step 3: 커밋**

```bash
git add backend/build.gradle
git commit -m "build: ArchUnit 의존성 추가"
```

---

## Task 2: DTO 통합 — PostDto, CommentDto

**Files:**
- Create: `backend/src/main/java/org/hikikomori/community/dto/PostDto.java`
- Create: `backend/src/main/java/org/hikikomori/community/dto/CommentDto.java`

- [ ] **Step 1: PostDto.java 생성**

```java
package org.hikikomori.community.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;
import org.hikikomori.community.domain.Post;

public class PostDto {

    public record CreateRequest(
            @NotBlank String title,
            @NotBlank String content,
            @NotBlank String tag,
            @NotNull Long userId,
            @NotBlank String nickName
    ) {}

    public record UpdateRequest(
            @NotNull Long userId,
            @NotBlank String title,
            @NotBlank String content,
            @NotBlank String tag
    ) {}

    public record Response(
            UUID id,
            Long userId,
            String nickName,
            String title,
            String content,
            String tag,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        public static Response from(Post post) {
            return new Response(
                    post.getId(),
                    post.getUserId(),
                    post.getNickName(),
                    post.getTitle(),
                    post.getContent(),
                    post.getTag(),
                    post.getCreatedAt(),
                    post.getUpdatedAt()
            );
        }
    }
}
```

- [ ] **Step 2: CommentDto.java 생성**

```java
package org.hikikomori.community.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.hikikomori.community.domain.Comment;

public class CommentDto {

    public record CreateRequest(
            @NotBlank String content,
            UUID parentId,
            Long userId,
            String nickName
    ) {}

    public record UpdateRequest(
            @NotNull Long userId,
            @NotBlank String content
    ) {}

    public record Response(
            UUID id,
            Long userId,
            String nickName,
            String content,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            LocalDateTime deletedAt,
            List<Response> children
    ) {
        public static Response from(Comment comment) {
            List<Response> childResponses = comment.getChildren().stream()
                    .map(Response::from)
                    .toList();

            return new Response(
                    comment.getId(),
                    comment.getUserId(),
                    comment.getNickName(),
                    comment.getContent(),
                    comment.getCreatedAt(),
                    comment.getUpdatedAt(),
                    comment.getDeletedAt(),
                    childResponses
            );
        }
    }
}
```

- [ ] **Step 3: 컴파일 확인**

Run: `./backend/gradlew -p backend compileJava`
Expected: BUILD SUCCESSFUL (기존 코드는 아직 old DTO 참조 중이므로 새 파일만 컴파일 확인)

- [ ] **Step 4: 커밋**

```bash
git add backend/src/main/java/org/hikikomori/community/dto/PostDto.java \
       backend/src/main/java/org/hikikomori/community/dto/CommentDto.java
git commit -m "refactor: DTO 도메인 단위 통합 (PostDto, CommentDto)"
```

---

## Task 3: Repository 계층 리팩토링

**Files:**
- Create: `backend/src/main/java/org/hikikomori/community/repository/PostJpaRepository.java`
- Create: `backend/src/main/java/org/hikikomori/community/repository/CommentJpaRepository.java`
- Create: `backend/src/main/java/org/hikikomori/community/repository/PostRepositoryImpl.java`
- Create: `backend/src/main/java/org/hikikomori/community/repository/CommentRepositoryImpl.java`
- Delete: `backend/src/main/java/org/hikikomori/community/repository/PostRepository.java`
- Delete: `backend/src/main/java/org/hikikomori/community/repository/CommentRepository.java`

- [ ] **Step 1: PostJpaRepository.java 생성 (기존 PostRepository 복사 + 리네임)**

```java
package org.hikikomori.community.repository;

import java.time.LocalDateTime;
import java.util.UUID;
import org.hikikomori.community.domain.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface PostJpaRepository extends JpaRepository<Post, UUID> {

    Page<Post> findByUserId(Long userId, Pageable pageable);

    @Modifying
    @Query("DELETE FROM Post p WHERE p.createdAt >= :startAt AND p.createdAt < :endAt")
    long deleteByCreatedAtBetween(LocalDateTime startAt, LocalDateTime endAt);
}
```

- [ ] **Step 2: CommentJpaRepository.java 생성 (기존 CommentRepository 복사 + 리네임)**

```java
package org.hikikomori.community.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.hikikomori.community.domain.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface CommentJpaRepository extends JpaRepository<Comment, UUID> {

    List<Comment> findByPostIdAndParentIsNull(UUID postId);

    @Modifying
    @Query("DELETE FROM Comment c WHERE c.post.id = :postId")
    void deleteAllByPostId(UUID postId);

    @Modifying
    @Query("DELETE FROM Comment c WHERE c.createdAt >= :startAt AND c.createdAt < :endAt")
    long deleteByCreatedAtBetween(LocalDateTime startAt, LocalDateTime endAt);
}
```

- [ ] **Step 3: PostRepositoryImpl.java 생성**

```java
package org.hikikomori.community.repository;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.hikikomori.community.domain.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PostRepositoryImpl {

    private final PostJpaRepository jpaRepository;

    public Post getById(UUID id) {
        return jpaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다: " + id));
    }

    public Post save(Post post) {
        return jpaRepository.save(post);
    }

    public Page<Post> findAll(Pageable pageable) {
        return jpaRepository.findAll(pageable);
    }

    public Page<Post> findByUserId(Long userId, Pageable pageable) {
        return jpaRepository.findByUserId(userId, pageable);
    }

    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }
}
```

- [ ] **Step 4: CommentRepositoryImpl.java 생성**

```java
package org.hikikomori.community.repository;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.hikikomori.community.domain.Comment;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class CommentRepositoryImpl {

    private final CommentJpaRepository jpaRepository;

    public Comment getById(UUID id) {
        return jpaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다: " + id));
    }

    public Comment getParentById(UUID parentId) {
        return jpaRepository.findById(parentId)
                .orElseThrow(() -> new IllegalArgumentException("부모 댓글을 찾을 수 없습니다: " + parentId));
    }

    public Comment save(Comment comment) {
        return jpaRepository.save(comment);
    }

    public List<Comment> findByPostIdAndParentIsNull(UUID postId) {
        return jpaRepository.findByPostIdAndParentIsNull(postId);
    }

    public void deleteAllByPostId(UUID postId) {
        jpaRepository.deleteAllByPostId(postId);
    }
}
```

- [ ] **Step 5: 기존 PostRepository.java, CommentRepository.java 삭제**

```bash
rm backend/src/main/java/org/hikikomori/community/repository/PostRepository.java
rm backend/src/main/java/org/hikikomori/community/repository/CommentRepository.java
```

- [ ] **Step 6: 커밋**

```bash
git add backend/src/main/java/org/hikikomori/community/repository/
git commit -m "refactor: Repository 계층 분리 (JpaRepository + RepositoryImpl)"
```

---

## Task 4: Service 계층 리팩토링

**Files:**
- Modify: `backend/src/main/java/org/hikikomori/community/service/PostService.java`
- Modify: `backend/src/main/java/org/hikikomori/community/service/CommentService.java`
- Delete: `backend/src/main/java/org/hikikomori/community/service/vo/PostCreate.java`
- Delete: `backend/src/main/java/org/hikikomori/community/service/vo/PostUpdate.java`
- Delete: `backend/src/main/java/org/hikikomori/community/service/vo/CommentCreate.java`

- [ ] **Step 1: PostService.java 수정 — DTO 직접 수신, check 네이밍**

```java
package org.hikikomori.community.service;

import org.hikikomori.community.domain.Post;
import org.hikikomori.community.dto.PostDto;
import org.springframework.stereotype.Service;

@Service
public class PostService {

    public Post buildPost(PostDto.CreateRequest request) {
        return Post.builder()
                .userId(request.userId())
                .nickName(request.nickName())
                .title(request.title())
                .content(request.content())
                .tag(request.tag())
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

- [ ] **Step 2: CommentService.java 수정 — DTO 직접 수신, check 네이밍**

```java
package org.hikikomori.community.service;

import org.hikikomori.community.domain.Comment;
import org.hikikomori.community.domain.Post;
import org.hikikomori.community.dto.CommentDto;
import org.springframework.stereotype.Service;

@Service
public class CommentService {

    public Comment buildComment(CommentDto.CreateRequest request, Post post, Comment parent) {
        return Comment.builder()
                .content(request.content())
                .userId(request.userId())
                .nickName(request.nickName())
                .post(post)
                .parent(parent)
                .build();
    }

    public void checkOwnership(Comment comment, Long userId, String operation) {
        if (!comment.getUserId().equals(userId)) {
            throw new IllegalArgumentException("본인의 댓글만 " + operation + "할 수 있습니다");
        }
    }

    public void checkNestingDepth(Comment parent) {
        if (parent != null && parent.getParent() != null) {
            throw new IllegalArgumentException("대댓글에는 답글을 달 수 없습니다");
        }
    }

    public void applyUpdate(Comment comment, String content) {
        comment.updateContent(content);
    }

    public void applySoftDelete(Comment comment) {
        comment.softDelete();
    }
}
```

- [ ] **Step 3: VO 파일 삭제**

```bash
rm -r backend/src/main/java/org/hikikomori/community/service/vo/
```

- [ ] **Step 4: 커밋**

```bash
git add backend/src/main/java/org/hikikomori/community/service/
git commit -m "refactor: Service 계층 — DTO 직접 수신, check 네이밍, VO 삭제"
```

---

## Task 5: Facade 리팩토링

**Files:**
- Modify: `backend/src/main/java/org/hikikomori/community/facade/PostFacade.java`

- [ ] **Step 1: PostFacade.java 전체 수정**

```java
package org.hikikomori.community.facade;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.hikikomori.community.domain.Comment;
import org.hikikomori.community.domain.Post;
import org.hikikomori.community.dto.CommentDto;
import org.hikikomori.community.dto.PostDto;
import org.hikikomori.community.repository.CommentRepositoryImpl;
import org.hikikomori.community.repository.PostRepositoryImpl;
import org.hikikomori.community.service.CommentService;
import org.hikikomori.community.service.PostService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostFacade {

    private final PostService postService;
    private final CommentService commentService;
    private final PostRepositoryImpl postRepository;
    private final CommentRepositoryImpl commentRepository;

    public Page<PostDto.Response> findAllPosts(Pageable pageable) {
        return postRepository.findAll(pageable).map(PostDto.Response::from);
    }

    public Page<PostDto.Response> findMyPosts(Long userId, Pageable pageable) {
        return postRepository.findByUserId(userId, pageable).map(PostDto.Response::from);
    }

    public PostDto.Response findPostById(UUID id) {
        Post post = postRepository.getById(id);
        return PostDto.Response.from(post);
    }

    public PostDto.Response createPost(PostDto.CreateRequest request) {
        Post post = postService.buildPost(request);
        Post saved = postRepository.save(post);
        return PostDto.Response.from(saved);
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

    public List<CommentDto.Response> findCommentsByPostId(UUID postId) {
        return commentRepository.findByPostIdAndParentIsNull(postId).stream()
                .map(CommentDto.Response::from)
                .toList();
    }

    public CommentDto.Response createComment(UUID postId, CommentDto.CreateRequest request) {
        Post post = postRepository.getById(postId);
        Comment parent = request.parentId() != null
                ? commentRepository.getParentById(request.parentId())
                : null;

        commentService.checkNestingDepth(parent);
        Comment comment = commentService.buildComment(request, post, parent);
        Comment saved = commentRepository.save(comment);
        return CommentDto.Response.from(saved);
    }

    public void updateComment(UUID commentId, CommentDto.UpdateRequest request) {
        Comment comment = commentRepository.getById(commentId);
        commentService.checkOwnership(comment, request.userId(), "수정");
        commentService.applyUpdate(comment, request.content());
        commentRepository.save(comment);
    }

    public void deleteComment(UUID commentId, Long userId) {
        Comment comment = commentRepository.getById(commentId);
        commentService.checkOwnership(comment, userId, "삭제");
        commentService.applySoftDelete(comment);
        commentRepository.save(comment);
    }
}
```

- [ ] **Step 2: 커밋**

```bash
git add backend/src/main/java/org/hikikomori/community/facade/PostFacade.java
git commit -m "refactor: Facade — RepositoryImpl 주입, 통합 DTO 사용"
```

---

## Task 6: Controller & Batch import 수정

**Files:**
- Modify: `backend/src/main/java/org/hikikomori/community/controller/PostController.java`
- Modify: `backend/src/main/java/org/hikikomori/community/batch/domain/tasklet/PostPurgeTasklet.java`
- Modify: `backend/src/main/java/org/hikikomori/community/batch/domain/tasklet/CommentPurgeTasklet.java`

- [ ] **Step 1: PostController.java — DTO import 변경**

```java
package org.hikikomori.community.controller;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.hikikomori.community.dto.CommentDto;
import org.hikikomori.community.dto.PostDto;
import org.hikikomori.community.facade.PostFacade;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostFacade postFacade;

    @GetMapping
    public ResponseEntity<Page<PostDto.Response>> findAll(@PageableDefault(size = 6) Pageable pageable) {
        return ResponseEntity.ok(postFacade.findAllPosts(pageable));
    }

    @GetMapping("/my/{userId}")
    public ResponseEntity<Page<PostDto.Response>> findMyPosts(
            @PathVariable Long userId,
            @PageableDefault(size = 6) Pageable pageable
    ) {
        return ResponseEntity.ok(postFacade.findMyPosts(userId, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostDto.Response> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(postFacade.findPostById(id));
    }

    @PostMapping
    public ResponseEntity<PostDto.Response> create(@Valid @RequestBody PostDto.CreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(postFacade.createPost(request));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Void> update(
            @PathVariable UUID id,
            @Valid @RequestBody PostDto.UpdateRequest request) {
        postFacade.updatePost(id, request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id, @RequestParam Long userId) {
        postFacade.deletePost(id, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/comments")
    public ResponseEntity<List<CommentDto.Response>> findComments(@PathVariable UUID id) {
        return ResponseEntity.ok(postFacade.findCommentsByPostId(id));
    }

    @PostMapping("/{id}/comments")
    public ResponseEntity<CommentDto.Response> createComment(
            @PathVariable UUID id,
            @Valid @RequestBody CommentDto.CreateRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(postFacade.createComment(id, request));
    }

    @PatchMapping("/{id}/comments/{commentId}")
    public ResponseEntity<Void> updateComment(
            @PathVariable UUID id,
            @PathVariable UUID commentId,
            @Valid @RequestBody CommentDto.UpdateRequest request
    ) {
        postFacade.updateComment(commentId, request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable UUID id,
            @PathVariable UUID commentId,
            @RequestParam Long userId
    ) {
        postFacade.deleteComment(commentId, userId);
        return ResponseEntity.noContent().build();
    }
}
```

- [ ] **Step 2: PostPurgeTasklet.java — import 변경**

변경: `import org.hikikomori.community.repository.PostRepository` → `import org.hikikomori.community.repository.PostJpaRepository`
변경: `private final PostRepository postRepository` → `private final PostJpaRepository postRepository`

- [ ] **Step 3: CommentPurgeTasklet.java — import 변경**

변경: `import org.hikikomori.community.repository.CommentRepository` → `import org.hikikomori.community.repository.CommentJpaRepository`
변경: `private final CommentRepository commentRepository` → `private final CommentJpaRepository commentRepository`

- [ ] **Step 4: 기존 DTO 파일 삭제**

```bash
rm -r backend/src/main/java/org/hikikomori/community/dto/request/
rm -r backend/src/main/java/org/hikikomori/community/dto/response/
```

- [ ] **Step 5: main 소스 컴파일 확인**

Run: `./backend/gradlew -p backend compileJava`
Expected: BUILD SUCCESSFUL

- [ ] **Step 6: 커밋**

```bash
git add -A backend/src/main/
git commit -m "refactor: Controller, Batch import 수정 및 기존 DTO/VO 삭제"
```

---

## Task 7: 테스트 수정 — Service 테스트

**Files:**
- Modify: `backend/src/test/java/org/hikikomori/community/service/PostServiceTest.java`
- Modify: `backend/src/test/java/org/hikikomori/community/service/CommentServiceTest.java`

- [ ] **Step 1: PostServiceTest.java 수정**

```java
package org.hikikomori.community.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.hikikomori.community.domain.Post;
import org.hikikomori.community.dto.PostDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PostServiceTest {

    private final PostService postService = new PostService();

    @Test
    @DisplayName("게시글 엔티티 생성")
    void buildPost() {
        PostDto.CreateRequest request = new PostDto.CreateRequest("제목", "내용", "VOID", 1L, "테스터");

        Post post = postService.buildPost(request);

        assertThat(post.getUserId()).isEqualTo(1L);
        assertThat(post.getNickName()).isEqualTo("테스터");
        assertThat(post.getTitle()).isEqualTo("제목");
        assertThat(post.getContent()).isEqualTo("내용");
        assertThat(post.getId()).isNotNull();
        assertThat(post.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("게시글 소유자 검증 통과")
    void checkOwnershipSuccess() {
        Post post = Post.builder().userId(1L).nickName("테스터").title("제목").content("내용").build();

        assertThatCode(() -> postService.checkOwnership(post, 1L, "수정"))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("타인 게시글 수정 시 예외")
    void checkOwnershipFailOnUpdate() {
        Post post = Post.builder().userId(1L).nickName("테스터").title("제목").content("내용").build();

        assertThatThrownBy(() -> postService.checkOwnership(post, 2L, "수정"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("본인의 게시글만 수정할 수 있습니다");
    }

    @Test
    @DisplayName("타인 게시글 삭제 시 예외")
    void checkOwnershipFailOnDelete() {
        Post post = Post.builder().userId(1L).nickName("테스터").title("제목").content("내용").build();

        assertThatThrownBy(() -> postService.checkOwnership(post, 2L, "삭제"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("본인의 게시글만 삭제할 수 있습니다");
    }

    @Test
    @DisplayName("게시글 수정 적용")
    void applyUpdate() {
        Post post = Post.builder().userId(1L).nickName("테스터").title("구제목").content("구내용").tag("OLD").build();
        PostDto.UpdateRequest request = new PostDto.UpdateRequest(1L, "새제목", "새내용", "NEW");

        postService.applyUpdate(post, request);

        assertThat(post.getTitle()).isEqualTo("새제목");
        assertThat(post.getContent()).isEqualTo("새내용");
        assertThat(post.getTag()).isEqualTo("NEW");
        assertThat(post.getUpdatedAt()).isNotNull();
    }
}
```

- [ ] **Step 2: CommentServiceTest.java 수정**

```java
package org.hikikomori.community.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.hikikomori.community.domain.Comment;
import org.hikikomori.community.domain.Post;
import org.hikikomori.community.dto.CommentDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CommentServiceTest {

    private final CommentService commentService = new CommentService();

    @Test
    @DisplayName("댓글 엔티티 생성")
    void buildComment() {
        Post post = Post.builder().userId(1L).nickName("작성자").title("제목").content("내용").build();
        CommentDto.CreateRequest request = new CommentDto.CreateRequest("댓글", null, 2L, "댓글러");

        Comment comment = commentService.buildComment(request, post, null);

        assertThat(comment.getUserId()).isEqualTo(2L);
        assertThat(comment.getNickName()).isEqualTo("댓글러");
        assertThat(comment.getContent()).isEqualTo("댓글");
        assertThat(comment.getPost()).isEqualTo(post);
        assertThat(comment.getParent()).isNull();
        assertThat(comment.getId()).isNotNull();
    }

    @Test
    @DisplayName("대댓글 엔티티 생성")
    void buildReply() {
        Post post = Post.builder().userId(1L).nickName("작성자").title("제목").content("내용").build();
        Comment parent = Comment.builder().userId(2L).nickName("댓글러").content("댓글").post(post).build();
        CommentDto.CreateRequest request = new CommentDto.CreateRequest("대댓글", null, 3L, "대댓글러");

        Comment reply = commentService.buildComment(request, post, parent);

        assertThat(reply.getParent()).isEqualTo(parent);
    }

    @Test
    @DisplayName("댓글 소유자 검증 통과")
    void checkOwnershipSuccess() {
        Comment comment = Comment.builder().userId(2L).nickName("댓글러").content("댓글").build();

        assertThatCode(() -> commentService.checkOwnership(comment, 2L, "수정"))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("타인 댓글 수정 시 예외")
    void checkOwnershipFailOnUpdate() {
        Comment comment = Comment.builder().userId(2L).nickName("댓글러").content("댓글").build();

        assertThatThrownBy(() -> commentService.checkOwnership(comment, 3L, "수정"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("본인의 댓글만 수정할 수 있습니다");
    }

    @Test
    @DisplayName("타인 댓글 삭제 시 예외")
    void checkOwnershipFailOnDelete() {
        Comment comment = Comment.builder().userId(2L).nickName("댓글러").content("댓글").build();

        assertThatThrownBy(() -> commentService.checkOwnership(comment, 3L, "삭제"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("본인의 댓글만 삭제할 수 있습니다");
    }

    @Test
    @DisplayName("루트 댓글에 대댓글 허용")
    void checkNestingDepthRootComment() {
        Comment parent = Comment.builder().userId(2L).nickName("댓글러").content("댓글").build();

        assertThatCode(() -> commentService.checkNestingDepth(parent))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("null 부모 허용 (루트 댓글 생성)")
    void checkNestingDepthNullParent() {
        assertThatCode(() -> commentService.checkNestingDepth(null))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("대댓글에 답글 시 예외")
    void checkNestingDepthReplyToReply() {
        Post post = Post.builder().userId(1L).nickName("작성자").title("제목").content("내용").build();
        Comment parent = Comment.builder().userId(2L).nickName("댓글러").content("댓글").post(post).build();
        Comment reply = Comment.builder().userId(3L).nickName("대댓글러").content("대댓글").post(post).parent(parent).build();

        assertThatThrownBy(() -> commentService.checkNestingDepth(reply))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("대댓글에는 답글을 달 수 없습니다");
    }

    @Test
    @DisplayName("댓글 내용 수정")
    void applyUpdate() {
        Comment comment = Comment.builder().userId(2L).nickName("댓글러").content("원본").build();

        commentService.applyUpdate(comment, "수정됨");

        assertThat(comment.getContent()).isEqualTo("수정됨");
        assertThat(comment.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("댓글 소프트 삭제")
    void applySoftDelete() {
        Comment comment = Comment.builder().userId(2L).nickName("댓글러").content("댓글").build();

        commentService.applySoftDelete(comment);

        assertThat(comment.getDeletedAt()).isNotNull();
        assertThat(comment.getContent()).isNotEqualTo("댓글");
    }
}
```

- [ ] **Step 3: 커밋**

```bash
git add backend/src/test/java/org/hikikomori/community/service/
git commit -m "test: Service 테스트 — DTO 직접 수신, check 네이밍 반영"
```

---

## Task 8: 테스트 수정 — Facade, Controller, Repository, Batch 테스트

**Files:**
- Modify: `backend/src/test/java/org/hikikomori/community/facade/PostFacadeTest.java`
- Modify: `backend/src/test/java/org/hikikomori/community/controller/PostControllerTest.java`
- Modify: `backend/src/test/java/org/hikikomori/community/repository/PostRepositoryTest.java`
- Modify: `backend/src/test/java/org/hikikomori/community/batch/tasklet/PostPurgeTaskletTest.java`
- Modify: `backend/src/test/java/org/hikikomori/community/batch/tasklet/CommentPurgeTaskletTest.java`

- [ ] **Step 1: PostFacadeTest.java 수정**

주요 변경:
- `@Mock PostRepository` → `@Mock PostRepositoryImpl`
- `@Mock CommentRepository` → `@Mock CommentRepositoryImpl`
- `@Spy PostService` → `@Spy PostService` (변경 없음, 하지만 Spy 인스턴스는 유지)
- 모든 DTO import를 `PostDto.*`, `CommentDto.*`로 변경
- `findPostOrThrow` 관련 mock을 `postRepository.getById()`로 변경
- `findCommentOrThrow` → `commentRepository.getById()`
- `findParentComment` → `commentRepository.getParentById()`

```java
package org.hikikomori.community.facade;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.UUID;
import org.hikikomori.community.dto.CommentDto;
import org.hikikomori.community.dto.PostDto;
import org.hikikomori.community.domain.Comment;
import org.hikikomori.community.domain.Post;
import org.hikikomori.community.repository.CommentRepositoryImpl;
import org.hikikomori.community.repository.PostRepositoryImpl;
import org.hikikomori.community.service.CommentService;
import org.hikikomori.community.service.PostService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class PostFacadeTest {

    @InjectMocks
    private PostFacade postFacade;

    @Spy
    private PostService postService = new PostService();

    @Spy
    private CommentService commentService = new CommentService();

    @Mock
    private PostRepositoryImpl postRepository;

    @Mock
    private CommentRepositoryImpl commentRepository;

    private static final UUID POST_ID = UUID.randomUUID();
    private static final UUID COMMENT_ID = UUID.randomUUID();
    private static final UUID REPLY_ID = UUID.randomUUID();

    // === Post ===

    @Test
    @DisplayName("게시글 생성")
    void createPost() {
        Post post = Post.builder().userId(1L).nickName("테스터").title("제목").content("내용").tag("VOID").build();
        given(postRepository.save(any(Post.class))).willReturn(post);

        PostDto.CreateRequest request = new PostDto.CreateRequest("제목", "내용", "VOID", 1L, "테스터");
        PostDto.Response result = postFacade.createPost(request);

        assertThat(result.userId()).isEqualTo(1L);
        assertThat(result.nickName()).isEqualTo("테스터");
        assertThat(result.title()).isEqualTo("제목");
        verify(postRepository).save(any(Post.class));
    }

    @Test
    @DisplayName("게시글 목록 조회 - 페이징")
    void findAllPosts() {
        List<Post> posts = List.of(
                Post.builder().title("제목1").content("내용1").build(),
                Post.builder().title("제목2").content("내용2").build()
        );
        Page<Post> page = new PageImpl<>(posts);
        Pageable pageable = PageRequest.of(0, 10);
        given(postRepository.findAll(pageable)).willReturn(page);

        Page<PostDto.Response> result = postFacade.findAllPosts(pageable);

        assertThat(result.getContent()).hasSize(2);
        verify(postRepository).findAll(pageable);
    }

    @Test
    @DisplayName("게시글 단건 조회")
    void findPostById() {
        Post post = Post.builder().title("제목").content("내용").build();
        given(postRepository.getById(POST_ID)).willReturn(post);

        PostDto.Response result = postFacade.findPostById(POST_ID);

        assertThat(result.title()).isEqualTo("제목");
        verify(postRepository).getById(POST_ID);
    }

    @Test
    @DisplayName("존재하지 않는 게시글 조회 시 예외")
    void findPostByIdNotFound() {
        UUID notFoundId = UUID.randomUUID();
        given(postRepository.getById(notFoundId))
                .willThrow(new IllegalArgumentException("게시글을 찾을 수 없습니다: " + notFoundId));

        assertThatThrownBy(() -> postFacade.findPostById(notFoundId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("게시글을 찾을 수 없습니다");
    }

    @Test
    @DisplayName("게시글 수정")
    void updatePost() {
        Post post = Post.builder().userId(1L).nickName("테스터").title("제목").content("내용").tag("OLD").build();
        given(postRepository.getById(POST_ID)).willReturn(post);

        PostDto.UpdateRequest request = new PostDto.UpdateRequest(1L, "새제목", "새내용", "NEW");
        postFacade.updatePost(POST_ID, request);

        assertThat(post.getTitle()).isEqualTo("새제목");
        assertThat(post.getContent()).isEqualTo("새내용");
        assertThat(post.getUpdatedAt()).isNotNull();
        verify(postRepository).save(post);
    }

    @Test
    @DisplayName("게시글 수정 - 타인 게시글 수정 시 예외")
    void updatePostByOtherUserThrowsException() {
        Post post = Post.builder().userId(1L).nickName("테스터").title("제목").content("내용").tag("OLD").build();
        given(postRepository.getById(POST_ID)).willReturn(post);

        PostDto.UpdateRequest request = new PostDto.UpdateRequest(2L, "새제목", "새내용", "NEW");

        assertThatThrownBy(() -> postFacade.updatePost(POST_ID, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("본인의 게시글만 수정할 수 있습니다");
    }

    @Test
    @DisplayName("게시글 삭제")
    void deletePost() {
        Post post = Post.builder().userId(1L).nickName("테스터").title("제목").content("내용").tag("VOID").build();
        given(postRepository.getById(POST_ID)).willReturn(post);

        postFacade.deletePost(POST_ID, 1L);

        verify(commentRepository).deleteAllByPostId(POST_ID);
        verify(postRepository).deleteById(POST_ID);
    }

    @Test
    @DisplayName("게시글 삭제 - 타인 게시글 삭제 시 예외")
    void deletePostByOtherUserThrowsException() {
        Post post = Post.builder().userId(1L).nickName("테스터").title("제목").content("내용").tag("VOID").build();
        given(postRepository.getById(POST_ID)).willReturn(post);

        assertThatThrownBy(() -> postFacade.deletePost(POST_ID, 2L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("본인의 게시글만 삭제할 수 있습니다");
    }

    // === Comment ===

    @Test
    @DisplayName("댓글 목록 조회")
    void findCommentsByPostId() {
        List<Comment> comments = List.of(
                Comment.builder().content("댓글1").build(),
                Comment.builder().content("댓글2").build()
        );
        given(commentRepository.findByPostIdAndParentIsNull(POST_ID)).willReturn(comments);

        List<CommentDto.Response> result = postFacade.findCommentsByPostId(POST_ID);

        assertThat(result).hasSize(2);
        verify(commentRepository).findByPostIdAndParentIsNull(POST_ID);
    }

    @Test
    @DisplayName("댓글 생성")
    void createComment() {
        Post post = Post.builder().userId(1L).nickName("테스터").title("제목").content("내용").tag("VOID").build();
        Comment comment = Comment.builder().userId(2L).nickName("댓글러").content("댓글").post(post).build();
        given(postRepository.getById(POST_ID)).willReturn(post);
        given(commentRepository.save(any(Comment.class))).willReturn(comment);

        CommentDto.CreateRequest request = new CommentDto.CreateRequest("댓글", null, 2L, "댓글러");
        CommentDto.Response result = postFacade.createComment(POST_ID, request);

        assertThat(result.userId()).isEqualTo(2L);
        assertThat(result.nickName()).isEqualTo("댓글러");
        assertThat(result.content()).isEqualTo("댓글");
        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    @DisplayName("대댓글 생성")
    void createReply() {
        Post post = Post.builder().userId(1L).nickName("테스터").title("제목").content("내용").tag("VOID").build();
        Comment parent = Comment.builder().userId(2L).nickName("댓글러").content("댓글").post(post).build();
        Comment reply = Comment.builder().userId(3L).nickName("대댓글러").content("대댓글").post(post).parent(parent).build();
        given(postRepository.getById(POST_ID)).willReturn(post);
        given(commentRepository.getParentById(COMMENT_ID)).willReturn(parent);
        given(commentRepository.save(any(Comment.class))).willReturn(reply);

        CommentDto.CreateRequest request = new CommentDto.CreateRequest("대댓글", COMMENT_ID, 3L, "대댓글러");
        CommentDto.Response result = postFacade.createComment(POST_ID, request);

        assertThat(result.content()).isEqualTo("대댓글");
    }

    @Test
    @DisplayName("대댓글에 답글 달기 시 예외")
    void createReplyToReplyThrowsException() {
        Post post = Post.builder().userId(1L).nickName("테스터").title("제목").content("내용").tag("VOID").build();
        Comment parent = Comment.builder().userId(2L).nickName("댓글러").content("댓글").post(post).build();
        Comment reply = Comment.builder().userId(3L).nickName("대댓글러").content("대댓글").post(post).parent(parent).build();
        given(postRepository.getById(POST_ID)).willReturn(post);
        given(commentRepository.getParentById(REPLY_ID)).willReturn(reply);

        CommentDto.CreateRequest request = new CommentDto.CreateRequest("대대댓글", REPLY_ID, 4L, "대대댓글러");

        assertThatThrownBy(() -> postFacade.createComment(POST_ID, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("대댓글에는 답글을 달 수 없습니다");
    }

    @Test
    @DisplayName("존재하지 않는 게시글에 댓글 생성 시 예외")
    void createCommentOnNonExistentPost() {
        UUID notFoundId = UUID.randomUUID();
        given(postRepository.getById(notFoundId))
                .willThrow(new IllegalArgumentException("게시글을 찾을 수 없습니다: " + notFoundId));

        CommentDto.CreateRequest request = new CommentDto.CreateRequest("댓글", null, 2L, "댓글러");

        assertThatThrownBy(() -> postFacade.createComment(notFoundId, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("게시글을 찾을 수 없습니다");
    }

    @Test
    @DisplayName("댓글 수정")
    void updateComment() {
        Post post = Post.builder().userId(1L).nickName("테스터").title("제목").content("내용").tag("VOID").build();
        Comment comment = Comment.builder().userId(2L).nickName("댓글러").content("댓글").post(post).build();
        given(commentRepository.getById(COMMENT_ID)).willReturn(comment);

        CommentDto.UpdateRequest request = new CommentDto.UpdateRequest(2L, "수정된 댓글");
        postFacade.updateComment(COMMENT_ID, request);

        assertThat(comment.getContent()).isEqualTo("수정된 댓글");
        assertThat(comment.getUpdatedAt()).isNotNull();
        verify(commentRepository).save(comment);
    }

    @Test
    @DisplayName("댓글 수정 - 타인 댓글 수정 시 예외")
    void updateCommentByOtherUserThrowsException() {
        Post post = Post.builder().userId(1L).nickName("테스터").title("제목").content("내용").tag("VOID").build();
        Comment comment = Comment.builder().userId(2L).nickName("댓글러").content("댓글").post(post).build();
        given(commentRepository.getById(COMMENT_ID)).willReturn(comment);

        CommentDto.UpdateRequest request = new CommentDto.UpdateRequest(3L, "수정된 댓글");

        assertThatThrownBy(() -> postFacade.updateComment(COMMENT_ID, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("본인의 댓글만 수정할 수 있습니다");
    }

    @Test
    @DisplayName("댓글 삭제 - 소프트 삭제로 deletedAt 설정 및 내용 대체")
    void deleteComment() {
        Post post = Post.builder().userId(1L).nickName("테스터").title("제목").content("내용").tag("VOID").build();
        Comment comment = Comment.builder().userId(2L).nickName("댓글러").content("댓글").post(post).build();
        given(commentRepository.getById(COMMENT_ID)).willReturn(comment);

        postFacade.deleteComment(COMMENT_ID, 2L);

        assertThat(comment.getDeletedAt()).isNotNull();
        assertThat(comment.getContent()).isNotBlank();
        verify(commentRepository).save(comment);
    }

    @Test
    @DisplayName("댓글 삭제 - 타인 댓글 삭제 시 예외")
    void deleteCommentByOtherUserThrowsException() {
        Post post = Post.builder().userId(1L).nickName("테스터").title("제목").content("내용").tag("VOID").build();
        Comment comment = Comment.builder().userId(2L).nickName("댓글러").content("댓글").post(post).build();
        given(commentRepository.getById(COMMENT_ID)).willReturn(comment);

        assertThatThrownBy(() -> postFacade.deleteComment(COMMENT_ID, 3L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("본인의 댓글만 삭제할 수 있습니다");
    }
}
```

- [ ] **Step 2: PostControllerTest.java 수정**

```java
package org.hikikomori.community.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import org.hikikomori.community.dto.PostDto;
import org.hikikomori.community.facade.PostFacade;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(PostController.class)
class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PostFacade postFacade;

    @Test
    @DisplayName("GET /api/posts/my/{userId} - userId로 내 게시글 조회")
    void findMyPosts() throws Exception {
        Long userId = 12345L;
        List<PostDto.Response> posts = List.of(
                new PostDto.Response(null, userId, "유저", "제목1", "내용1", "VOID", null, null),
                new PostDto.Response(null, userId, "유저", "제목2", "내용2", "VOID", null, null)
        );
        given(postFacade.findMyPosts(eq(userId), any(Pageable.class)))
                .willReturn(new PageImpl<>(posts));

        mockMvc.perform(get("/api/posts/my/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].userId").value(userId))
                .andExpect(jsonPath("$.content[0].title").value("제목1"));
    }
}
```

- [ ] **Step 3: PostRepositoryTest.java — import 변경**

변경:
- `import org.hikikomori.community.repository.PostRepository` → `import org.hikikomori.community.repository.PostJpaRepository`
- `import org.hikikomori.community.repository.CommentRepository` → `import org.hikikomori.community.repository.CommentJpaRepository`
- `private PostRepository postRepository` → `private PostJpaRepository postRepository`
- `private CommentRepository commentRepository` → `private CommentJpaRepository commentRepository`

- [ ] **Step 4: PostPurgeTaskletTest.java — import 변경**

변경:
- `import org.hikikomori.community.repository.PostRepository` → `import org.hikikomori.community.repository.PostJpaRepository`
- `@Mock private PostRepository postRepository` → `@Mock private PostJpaRepository postRepository`

- [ ] **Step 5: CommentPurgeTaskletTest.java — import 변경**

변경:
- `import org.hikikomori.community.repository.CommentRepository` → `import org.hikikomori.community.repository.CommentJpaRepository`
- `@Mock private CommentRepository commentRepository` → `@Mock private CommentJpaRepository commentRepository`

- [ ] **Step 6: 전체 테스트 실행**

Run: `./backend/gradlew -p backend test`
Expected: BUILD SUCCESSFUL, 모든 테스트 통과

- [ ] **Step 7: 커밋**

```bash
git add backend/src/test/
git commit -m "test: Facade, Controller, Repository, Batch 테스트 리팩토링 반영"
```

---

## Task 9: ArchUnit 아키텍처 테스트

**Files:**
- Create: `backend/src/test/java/org/hikikomori/community/architecture/ArchitectureTest.java`

- [ ] **Step 1: ArchitectureTest.java 생성**

```java
package org.hikikomori.community.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

class ArchitectureTest {

    private static JavaClasses classes;

    @BeforeAll
    static void setUp() {
        classes = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("org.hikikomori.community");
    }

    @Test
    @DisplayName("Controller는 Facade만 의존한다 (Service, Repository 직접 접근 금지)")
    void controllerShouldOnlyDependOnFacade() {
        noClasses().that().resideInAPackage("..controller..")
                .and().areNotAnnotatedWith(org.springframework.context.annotation.Configuration.class)
                .should().dependOnClassesThat().resideInAPackage("..service..")
                .as("Controller는 Service에 직접 의존하면 안 됩니다")
                .check(classes);

        noClasses().that().resideInAPackage("..controller..")
                .should().dependOnClassesThat().resideInAPackage("..repository..")
                .as("Controller는 Repository에 직접 의존하면 안 됩니다")
                .check(classes);
    }

    @Test
    @DisplayName("Service는 Repository를 모른다")
    void serviceShouldNotDependOnRepository() {
        noClasses().that().resideInAPackage("..service..")
                .should().dependOnClassesThat().resideInAPackage("..repository..")
                .as("Service는 Repository에 의존하면 안 됩니다")
                .check(classes);
    }

    @Test
    @DisplayName("Controller는 Entity(domain)에 직접 의존하지 않는다")
    void controllerShouldNotDependOnDomain() {
        noClasses().that().resideInAPackage("..controller..")
                .should().dependOnClassesThat().resideInAPackage("..domain..")
                .as("Controller는 Entity에 직접 의존하면 안 됩니다")
                .check(classes);
    }

    @Test
    @DisplayName("Facade/Service는 JpaRepository에 직접 접근하지 않는다")
    void facadeAndServiceShouldNotUseJpaRepositoryDirectly() {
        noClasses().that().resideInAnyPackage("..facade..", "..service..")
                .should().dependOnClassesThat().haveSimpleNameEndingWith("JpaRepository")
                .as("Facade와 Service는 JpaRepository에 직접 의존하면 안 됩니다")
                .check(classes);
    }

    @Test
    @DisplayName("RepositoryImpl만 JpaRepository를 사용한다 (batch 제외)")
    void onlyRepositoryImplShouldUseJpaRepository() {
        classes().that().haveSimpleNameEndingWith("JpaRepository")
                .should().onlyBeAccessed().byClassesThat()
                .haveSimpleNameEndingWith("RepositoryImpl")
                .orShould().onlyBeAccessed().byClassesThat()
                .resideInAPackage("..batch..")
                .as("JpaRepository는 RepositoryImpl과 batch에서만 접근해야 합니다")
                .check(classes);
    }
}
```

- [ ] **Step 2: ArchUnit 테스트 실행**

Run: `./backend/gradlew -p backend test --tests "org.hikikomori.community.architecture.ArchitectureTest"`
Expected: 모든 5개 규칙 통과

- [ ] **Step 3: 커밋**

```bash
git add backend/src/test/java/org/hikikomori/community/architecture/ArchitectureTest.java
git commit -m "test: ArchUnit 아키텍처 규칙 테스트 추가"
```

---

## Task 10: 최종 검증

- [ ] **Step 1: 전체 빌드 + 테스트**

Run: `./backend/gradlew -p backend build`
Expected: BUILD SUCCESSFUL

- [ ] **Step 2: 삭제된 파일 잔존 확인**

```bash
# 아래 경로에 파일이 없어야 함
ls backend/src/main/java/org/hikikomori/community/dto/request/ 2>/dev/null && echo "FAIL: request 디렉토리 잔존" || echo "OK"
ls backend/src/main/java/org/hikikomori/community/dto/response/ 2>/dev/null && echo "FAIL: response 디렉토리 잔존" || echo "OK"
ls backend/src/main/java/org/hikikomori/community/service/vo/ 2>/dev/null && echo "FAIL: vo 디렉토리 잔존" || echo "OK"
ls backend/src/main/java/org/hikikomori/community/repository/PostRepository.java 2>/dev/null && echo "FAIL: 구 PostRepository 잔존" || echo "OK"
ls backend/src/main/java/org/hikikomori/community/repository/CommentRepository.java 2>/dev/null && echo "FAIL: 구 CommentRepository 잔존" || echo "OK"
```
Expected: 모두 OK

- [ ] **Step 3: CLAUDE.md 최종 업데이트**

`backend/CLAUDE.md`의 패키지 구조와 테스트 섹션이 현재 코드와 일치하는지 확인하고 필요시 수정.

- [ ] **Step 4: 최종 커밋**

```bash
git add -A
git commit -m "refactor: 백엔드 아키텍처 V2 리팩토링 완료"
```
