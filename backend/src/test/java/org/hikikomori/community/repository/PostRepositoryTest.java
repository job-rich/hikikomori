package org.hikikomori.community.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import org.hikikomori.community.domain.Comment;
import org.hikikomori.community.domain.Post;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

@DataJpaTest
class PostRepositoryTest {

    @Autowired
    private TestEntityManager testEntityManager;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private CommentRepository commentRepository;

    @BeforeEach
    void setUp() {
        commentRepository.deleteAll();
        postRepository.deleteAll();
    }

    @Test
    @DisplayName("게시글 저장")
    void save() {
        Post post = Post.builder().title("제목").content("내용").build();

        Post saved = postRepository.save(post);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getTitle()).isEqualTo("제목");
        assertThat(saved.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("게시글 목록 조회 - 페이징")
    void findAllWithPaging() {
        for (int i = 1; i <= 25; i++) {
            postRepository.save(Post.builder().title("제목" + i).content("내용" + i).build());
        }

        Page<Post> page = postRepository.findAll(PageRequest.of(0, 10));

        assertThat(page.getContent()).hasSize(10);
        assertThat(page.getTotalElements()).isEqualTo(25);
        assertThat(page.getTotalPages()).isEqualTo(3);
    }

    @Test
    @DisplayName("게시글 단건 조회")
    void findById() {
        Post saved = postRepository.save(Post.builder().title("제목").content("내용").build());

        Post found = postRepository.findById(saved.getId()).orElseThrow();

        assertThat(found.getTitle()).isEqualTo("제목");
    }

    @Test
    @DisplayName("댓글 저장")
    void saveComment() {
        Post post = postRepository.save(Post.builder().title("제목").content("내용").build());
        Comment comment = Comment.builder().content("댓글").post(post).build();

        Comment saved = commentRepository.save(comment);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getPost().getId()).isEqualTo(post.getId());
    }

    @Test
    @DisplayName("대댓글 저장")
    void saveReply() {
        Post post = postRepository.save(Post.builder().title("제목").content("내용").build());
        Comment parent = commentRepository.save(Comment.builder().content("댓글").post(post).build());
        Comment reply = Comment.builder().content("대댓글").post(post).parent(parent).build();

        Comment saved = commentRepository.save(reply);

        assertThat(saved.getParent().getId()).isEqualTo(parent.getId());
    }

    @Test
    @DisplayName("게시글의 루트 댓글만 조회")
    void findByPostIdAndParentIsNull() {
        Post post = postRepository.save(Post.builder().title("제목").content("내용").build());
        Comment parent = commentRepository.save(Comment.builder().content("댓글").post(post).build());
        commentRepository.save(Comment.builder().content("대댓글").post(post).parent(parent).build());

        List<Comment> comments = commentRepository.findByPostIdAndParentIsNull(post.getId());

        assertThat(comments).hasSize(1);
        assertThat(comments.get(0).getContent()).isEqualTo("댓글");
    }

    @Test
    @DisplayName("기간 내 게시글만 삭제됨")
    void deletePostByCreatedAtBetween() {
        LocalDateTime yesterday = LocalDate.now().minusDays(1).atStartOfDay();
        LocalDateTime today = LocalDate.now().atStartOfDay();

        Post oldPost = Post.builder().title("오래된 게시글").content("내용").build();
        ReflectionTestUtils.setField(oldPost, "createdAt", yesterday);
        postRepository.save(oldPost);
        postRepository.save(Post.builder().title("오늘 게시글").content("내용").build());

        testEntityManager.flush();
        testEntityManager.getEntityManager().clear();

        long deleted = postRepository.deleteByCreatedAtBetween(yesterday, today);

        testEntityManager.getEntityManager().clear();

        assertThat(deleted).isEqualTo(1);
        assertThat(postRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("기간 내 댓글만 삭제됨")
    void deleteCommentByCreatedAtBetween() {
        LocalDateTime yesterday = LocalDate.now().minusDays(1).atStartOfDay();
        LocalDateTime today = LocalDate.now().atStartOfDay();

        Post post = postRepository.save(Post.builder().title("게시글").content("내용").build());

        Comment oldComment = Comment.builder().content("오래된 댓글").post(post).build();
        ReflectionTestUtils.setField(oldComment, "createdAt", yesterday);
        commentRepository.save(oldComment);
        commentRepository.save(Comment.builder().content("오늘 댓글").post(post).build());

        testEntityManager.flush();
        testEntityManager.getEntityManager().clear();

        long deleted = commentRepository.deleteByCreatedAtBetween(yesterday, today);

        testEntityManager.getEntityManager().clear();

        assertThat(deleted).isEqualTo(1);
        assertThat(commentRepository.count()).isEqualTo(1);
    }
}
