package org.hikikomori.community.search.model;

import org.hikikomori.community.domain.PostTag;

public record SearchCriteria(String query, PostTag tag, SortType sort) {

    public static SearchCriteria of(String query, PostTag tag, SortType sort) {
        if (query == null || query.isBlank()) {
            throw new IllegalArgumentException("검색어는 null 또는 공백일 수 없습니다");
        }
        return new SearchCriteria(
                query.trim(),
                tag,
                sort != null ? sort : SortType.RELEVANCE
        );
    }
}
