package org.hikikomori.community.search.engine;

import java.util.List;
import org.hikikomori.community.search.model.SearchCriteria;
import org.hikikomori.community.search.model.SearchHit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 검색 엔진 포트. 정렬 순서만 보장하며 score는 비노출.
 * 구현체 교체 가능 (pg_trgm → ES 등).
 */
public interface SearchEngine {

    Page<SearchHit> searchPosts(SearchCriteria criteria, Pageable pageable);

    Page<SearchHit> searchComments(SearchCriteria criteria, Pageable pageable);

    Page<SearchHit> searchUsers(SearchCriteria criteria, Pageable pageable);

    /**
     * 자동완성: q에 매칭되는 제목·닉네임 후보 문자열(distinct, 관련도순, 최대 limit).
     * 정렬 순서만 보장, 엔진 교체(ES) 가능.
     */
    List<String> suggest(String query, int limit);
}
