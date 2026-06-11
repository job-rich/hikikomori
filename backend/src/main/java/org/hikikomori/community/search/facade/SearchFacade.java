package org.hikikomori.community.search.facade;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.hikikomori.community.domain.PostTag;
import org.hikikomori.community.search.dto.SearchDto;
import org.hikikomori.community.search.engine.SearchEngine;
import org.hikikomori.community.search.model.HitType;
import org.hikikomori.community.search.model.SearchCriteria;
import org.hikikomori.community.search.model.SearchHit;
import org.hikikomori.community.search.model.SortType;
import org.hikikomori.community.search.service.SearchService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SearchFacade {

    private static final int SNIPPET_WINDOW = 60;

    private final SearchEngine searchEngine;
    private final SearchService searchService;

    public Page<SearchDto.Response> search(String query, String tag, SearchDto.Type type, SortType sort, Pageable pageable) {
        if (query == null || query.isBlank()) return Page.empty(pageable);

        PostTag postTag = resolveTag(tag);
        SearchCriteria criteria = SearchCriteria.of(query, postTag, sort);

        return switch (type) {
            case POST -> searchEngine.searchPosts(criteria, pageable).map(hit -> toResponse(hit, query));
            case COMMENT -> searchEngine.searchComments(criteria, pageable).map(hit -> toResponse(hit, query));
            case USER -> searchEngine.searchUsers(criteria, pageable).map(hit -> toResponse(hit, query));
            case ALL -> searchAll(criteria, query, pageable);
        };
    }

    public List<String> suggest(String query, int limit) {
        if (query == null || query.isBlank()) return List.of();
        return searchEngine.suggest(query.trim(), limit);
    }

    private Page<SearchDto.Response> searchAll(SearchCriteria criteria, String query, Pageable pageable) {
        List<SearchHit> posts = searchEngine.searchPosts(criteria, Pageable.unpaged()).getContent();
        List<SearchHit> comments = searchEngine.searchComments(criteria, Pageable.unpaged()).getContent();

        List<SearchHit> merged = new ArrayList<>();
        merged.addAll(posts);
        merged.addAll(comments);

        // 관련도 우선, 동률 시 POST > COMMENT > USER
        merged.sort(Comparator.comparingInt(h -> hitTypePriority(h.type())));

        int total = merged.size();
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), total);
        List<SearchHit> paged = start >= total ? List.of() : merged.subList(start, end);

        List<SearchDto.Response> responses = paged.stream()
                .map(hit -> toResponse(hit, query))
                .toList();

        return new PageImpl<>(responses, pageable, total);
    }

    private SearchDto.Response toResponse(SearchHit hit, String query) {
        String snippet = hit.type() != HitType.USER
                ? searchService.snippet(hit.content(), query, SNIPPET_WINDOW)
                : null;
        return SearchDto.Response.from(hit, snippet);
    }

    private static int hitTypePriority(HitType type) {
        return switch (type) {
            case POST -> 0;
            case COMMENT -> 1;
            case USER -> 2;
        };
    }

    private static PostTag resolveTag(String tag) {
        if (tag == null || tag.isBlank()) return null;
        return PostTag.valueOf(tag.toUpperCase());
    }
}
