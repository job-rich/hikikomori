package org.hikikomori.community.search.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.mockito.ArgumentCaptor;
import org.hikikomori.community.search.dto.SearchDto;
import org.hikikomori.community.search.facade.SearchFacade;
import org.hikikomori.community.search.model.SortType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(SearchController.class)
class SearchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SearchFacade searchFacade;

    @Test
    @DisplayName("GET /api/posts/search?q=자유&type=POST&sort=RELEVANCE - 200 및 페이지 JSON 구조")
    void searchByPostUpperCase() throws Exception {
        // given
        SearchDto.Response response = new SearchDto.Response(
                "POST", UUID.randomUUID(), "자유 게시판", "닉네임1", "DAILY", "자유에 관한…", 3L, 0L, LocalDateTime.now()
        );
        given(searchFacade.search(eq("자유"), isNull(), eq(SearchDto.Type.POST), eq(SortType.RELEVANCE), any(Pageable.class)))
                .willReturn(new PageImpl<>(List.of(response)));

        // when & then
        mockMvc.perform(get("/api/posts/search")
                        .param("query", "자유")
                        .param("type", "POST")
                        .param("sort", "RELEVANCE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].type").value("POST"))
                .andExpect(jsonPath("$.content[0].title").value("자유 게시판"));
    }

    @Test
    @DisplayName("GET /api/posts/search?q=자유&tag=DAILY - tag 필터 200")
    void searchWithTag() throws Exception {
        // given
        given(searchFacade.search(eq("자유"), eq("DAILY"), any(), any(), any(Pageable.class)))
                .willReturn(new PageImpl<>(List.of()));

        // when & then
        mockMvc.perform(get("/api/posts/search")
                        .param("query", "자유")
                        .param("tag", "DAILY"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/posts/search?q=자유&type=INVALID - 잘못된 type enum → 400")
    void searchWithInvalidType() throws Exception {
        mockMvc.perform(get("/api/posts/search")
                        .param("query", "자유")
                        .param("type", "INVALID_TYPE"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/posts/search?q= - 빈 q → 200 빈 Page")
    void searchEmptyQuery() throws Exception {
        // given
        given(searchFacade.search(eq(""), isNull(), any(), any(), any(Pageable.class)))
                .willReturn(Page.empty());

        // when & then
        mockMvc.perform(get("/api/posts/search")
                        .param("query", ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(0));
    }

    @Test
    @DisplayName("GET /api/posts/search?q=test - 기본값 ALL, RELEVANCE 적용")
    void searchDefaults() throws Exception {
        // given
        given(searchFacade.search(eq("test"), isNull(), eq(SearchDto.Type.ALL), eq(SortType.RELEVANCE), any(Pageable.class)))
                .willReturn(new PageImpl<>(List.of()));

        // when & then
        mockMvc.perform(get("/api/posts/search")
                        .param("query", "test"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/posts/search?q=test - size 미지정 시 Pageable size=6")
    void searchDefaultPageSizeIsSix() throws Exception {
        // given
        given(searchFacade.search(eq("test"), isNull(), any(), any(), any(Pageable.class)))
                .willReturn(new PageImpl<>(List.of()));

        // when
        mockMvc.perform(get("/api/posts/search")
                        .param("query", "test"))
                .andExpect(status().isOk());

        // then
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(searchFacade).search(eq("test"), isNull(), any(), any(), pageableCaptor.capture());
        assertThat(pageableCaptor.getValue().getPageSize()).isEqualTo(6);
    }

    @Test
    @DisplayName("GET /api/posts/search/suggest?q=자유 - 200 + JSON 배열")
    void suggestReturnsArray() throws Exception {
        given(searchFacade.suggest(eq("자유"), anyInt()))
                .willReturn(List.of("자유에 대한 성찰", "사회와 자유"));

        mockMvc.perform(get("/api/posts/search/suggest")
                        .param("query", "자유"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }

}
