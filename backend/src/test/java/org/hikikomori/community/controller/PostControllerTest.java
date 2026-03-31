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
