package org.hikikomori.community.repository;

import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import org.hikikomori.community.domain.Comment;
import org.hikikomori.community.domain.Post;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

@DataJpaTest
class PostRepositoryTest {

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
    @DisplayName("userId로 게시글 조회 - 해당 사용자의 게시글만 반환")
    void findByUserId() {
        Long targetUserId = 12345L;
        Long otherUserId = 99999L;

        postRepository.save(Post.builder().userId(targetUserId).nickName("유저1").title("내 글 1").content("내용1").tag("VOID").build());
        postRepository.save(Post.builder().userId(targetUserId).nickName("유저1").title("내 글 2").content("내용2").tag("VOID").build());
        postRepository.save(Post.builder().userId(otherUserId).nickName("유저2").title("남의 글").content("내용3").tag("VOID").build());

        Page<Post> result = postRepository.findByUserId(targetUserId, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).allMatch(post -> post.getUserId().equals(targetUserId));
        assertThat(result.getTotalElements()).isEqualTo(2);
    }

    @Test
    @DisplayName("userId로 게시글 조회 - 페이징 동작 확인")
    void findByUserIdWithPaging() {
        Long userId = 12345L;
        for (int i = 1; i <= 15; i++) {
            postRepository.save(Post.builder().userId(userId).nickName("유저").title("제목" + i).content("내용" + i).tag("VOID").build());
        }

        Page<Post> firstPage = postRepository.findByUserId(userId, PageRequest.of(0, 10));
        Page<Post> secondPage = postRepository.findByUserId(userId, PageRequest.of(1, 10));

        assertThat(firstPage.getContent()).hasSize(10);
        assertThat(secondPage.getContent()).hasSize(5);
        assertThat(firstPage.getTotalElements()).isEqualTo(15);
        assertThat(firstPage.getTotalPages()).isEqualTo(2);
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
}
