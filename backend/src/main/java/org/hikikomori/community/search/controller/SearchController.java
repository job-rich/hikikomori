package org.hikikomori.community.search.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.hikikomori.community.search.dto.SearchDto;
import org.hikikomori.community.search.facade.SearchFacade;
import org.hikikomori.community.search.model.SortType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/posts/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchFacade searchFacade;

    @GetMapping
    public ResponseEntity<Page<SearchDto.Response>> search(
            @RequestParam String query,
            @RequestParam(required = false) String tag,
            @RequestParam(defaultValue = "ALL") SearchDto.Type type,
            @RequestParam(defaultValue = "RELEVANCE") SortType sort,
            @PageableDefault(size = 6) Pageable pageable
    ) {
        return ResponseEntity.ok(searchFacade.search(query, tag, type, sort, pageable));
    }

    @GetMapping("/suggest")
    public ResponseEntity<List<String>> suggest(
            @RequestParam String query,
            @RequestParam(defaultValue = "8") int limit) {
        return ResponseEntity.ok(searchFacade.suggest(query, limit));
    }
}
