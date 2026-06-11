package org.hikikomori.community.search.model;

import org.hikikomori.community.domain.PostTag;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SearchCriteriaTest {

    @Test
    @DisplayName("query는 trim되어 저장된다")
    void queryIsTrimmed() {
        SearchCriteria criteria = SearchCriteria.of("  hello world  ", null, null);

        assertThat(criteria.query()).isEqualTo("hello world");
    }

    @Test
    @DisplayName("query가 null이면 예외가 발생한다")
    void nullQueryThrows() {
        assertThatThrownBy(() -> SearchCriteria.of(null, null, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("query가 blank이면 예외가 발생한다")
    void blankQueryThrows() {
        assertThatThrownBy(() -> SearchCriteria.of("   ", null, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("sort가 null이면 기본값 RELEVANCE가 적용된다")
    void defaultSortIsRelevance() {
        SearchCriteria criteria = SearchCriteria.of("test", null, null);

        assertThat(criteria.sort()).isEqualTo(SortType.RELEVANCE);
    }

    @Test
    @DisplayName("sort를 명시하면 해당 값이 적용된다")
    void explicitSortIsApplied() {
        SearchCriteria criteria = SearchCriteria.of("test", null, SortType.LATEST);

        assertThat(criteria.sort()).isEqualTo(SortType.LATEST);
    }

    @Test
    @DisplayName("tag는 null 허용 — 전체 검색")
    void tagIsNullable() {
        SearchCriteria criteria = SearchCriteria.of("test", null, null);

        assertThat(criteria.tag()).isNull();
    }

    @Test
    @DisplayName("tag를 지정하면 해당 값이 저장된다")
    void tagIsStoredWhenProvided() {
        SearchCriteria criteria = SearchCriteria.of("test", PostTag.DAILY, null);

        assertThat(criteria.tag()).isEqualTo(PostTag.DAILY);
    }
}
