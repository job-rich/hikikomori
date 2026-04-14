package org.hikikomori.community.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.UUID;
import org.hikikomori.community.domain.PostTag;
import org.hikikomori.community.dto.CommentDto;
import org.hikikomori.community.dto.PostDto;
import org.hikikomori.community.facade.PostFacade;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
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
    void getMyPosts() throws Exception {
        // given
        Long userId = 12345L;
        List<PostDto.Response> posts = List.of(
                new PostDto.Response(null, userId, "유저", "제목1", "내용1", PostTag.ETC, 0L, null, null),
                new PostDto.Response(null, userId, "유저", "제목2", "내용2", PostTag.ETC, 0L, null, null)
        );
        given(postFacade.getMyPosts(eq(userId), any(Pageable.class)))
                .willReturn(new PageImpl<>(posts));

        // when & then
        mockMvc.perform(get("/api/posts/my/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].userId").value(userId))
                .andExpect(jsonPath("$.content[0].title").value("제목1"));
    }

    @Test
    @DisplayName("POST /api/posts - 게시글 생성 시 201 반환")
    void createPost() throws Exception {
        // given
        UUID postId = UUID.randomUUID();
        PostDto.Response response = new PostDto.Response(postId, 1L, "테스터", "제목", "내용", PostTag.ETC, 0L, null, null);
        given(postFacade.createPost(any(PostDto.CreateRequest.class))).willReturn(response);

        String requestBody = """
                {
                    "title": "제목",
                    "content": "내용",
                    "tag": "ETC",
                    "userId": 1,
                    "nickName": "테스터"
                }
                """;

        // when & then
        mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.title").value("제목"))
                .andExpect(jsonPath("$.nickName").value("테스터"));
    }

    @Test
    @DisplayName("GET /api/posts/{id} - 게시글 단건 조회 시 200 반환")
    void findById() throws Exception {
        // given
        UUID postId = UUID.randomUUID();
        PostDto.Response response = new PostDto.Response(postId, 1L, "테스터", "제목", "내용", PostTag.ETC, 0L, null, null);
        given(postFacade.getPost(postId)).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/posts/{id}", postId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(postId.toString()))
                .andExpect(jsonPath("$.title").value("제목"))
                .andExpect(jsonPath("$.userId").value(1));
    }

    @Test
    @DisplayName("GET /api/posts - 게시글 전체 조회 시 200 반환")
    void findAll() throws Exception {
        // given
        List<PostDto.Response> posts = List.of(
                new PostDto.Response(null, 1L, "유저1", "제목1", "내용1", PostTag.ETC, 0L, null, null),
                new PostDto.Response(null, 2L, "유저2", "제목2", "내용2", PostTag.ETC, 0L, null, null)
        );
        given(postFacade.getPosts(any(Pageable.class))).willReturn(new PageImpl<>(posts));

        // when & then
        mockMvc.perform(get("/api/posts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2));
    }

    @Test
    @DisplayName("PATCH /api/posts/{id} - 게시글 수정 시 204 반환")
    void updatePost() throws Exception {
        // given
        UUID postId = UUID.randomUUID();

        String requestBody = """
                {
                    "userId": 1,
                    "title": "수정된 제목",
                    "content": "수정된 내용",
                    "tag": "CULTURE"
                }
                """;

        // when & then
        mockMvc.perform(patch("/api/posts/{id}", postId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isNoContent());

        verify(postFacade).updatePost(eq(postId), any(PostDto.UpdateRequest.class));
    }

    @Test
    @DisplayName("DELETE /api/posts/{id} - 게시글 삭제 시 204 반환")
    void deletePost() throws Exception {
        // given
        UUID postId = UUID.randomUUID();
        Long userId = 1L;

        // when & then
        mockMvc.perform(delete("/api/posts/{id}", postId)
                        .param("userId", userId.toString()))
                .andExpect(status().isNoContent());

        verify(postFacade).deletePost(postId, userId);
    }

    @Test
    @DisplayName("GET /api/posts/{id}/comments - 댓글 목록 조회 시 200 반환")
    void findComments() throws Exception {
        // given
        UUID postId = UUID.randomUUID();
        List<CommentDto.Response> comments = List.of(
                new CommentDto.Response(null, 1L, "댓글러1", "댓글1", null, null, null, List.of()),
                new CommentDto.Response(null, 2L, "댓글러2", "댓글2", null, null, null, List.of())
        );
        given(postFacade.getComments(postId)).willReturn(comments);

        // when & then
        mockMvc.perform(get("/api/posts/{id}/comments", postId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].content").value("댓글1"))
                .andExpect(jsonPath("$[1].content").value("댓글2"));
    }

    @Test
    @DisplayName("POST /api/posts/{id}/comments - 댓글 생성 시 201 반환")
    void createComment() throws Exception {
        // given
        UUID postId = UUID.randomUUID();
        UUID commentId = UUID.randomUUID();
        CommentDto.Response response = new CommentDto.Response(commentId, 2L, "댓글러", "댓글 내용", null, null, null, List.of());
        given(postFacade.createComment(eq(postId), any(CommentDto.CreateRequest.class))).willReturn(response);

        String requestBody = """
                {
                    "content": "댓글 내용",
                    "userId": 2,
                    "nickName": "댓글러"
                }
                """;

        // when & then
        mockMvc.perform(post("/api/posts/{id}/comments", postId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(2))
                .andExpect(jsonPath("$.content").value("댓글 내용"))
                .andExpect(jsonPath("$.nickName").value("댓글러"));
    }
}
