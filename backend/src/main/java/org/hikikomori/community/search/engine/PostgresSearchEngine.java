package org.hikikomori.community.search.engine;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.hikikomori.community.domain.PostTag;
import org.hikikomori.community.search.model.HitType;
import org.hikikomori.community.search.model.SearchCriteria;
import org.hikikomori.community.search.model.SearchHit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * pg_trgm 기반 SearchEngine 구현체. Docker/Testcontainers 없이는 similarity() 사용 불가.
 */
@Component
@RequiredArgsConstructor
public class PostgresSearchEngine implements SearchEngine {

    private static final java.util.regex.Pattern CHOSUNG_ONLY = java.util.regex.Pattern.compile(
            "^[ㄱㄲㄴㄷㄸㄹㅁㅂㅃㅅㅆㅇㅈㅉㅊㅋㅌㅍㅎ\\s]+$");

    private static boolean isChosungQuery(String query) {
        return query != null && !query.isBlank() && CHOSUNG_ONLY.matcher(query).matches();
    }

    private static String foldChosung(String query) {
        return query.replace('ㄲ', 'ㄱ').replace('ㄸ', 'ㄷ').replace('ㅃ', 'ㅂ')
                .replace('ㅆ', 'ㅅ').replace('ㅉ', 'ㅈ');
    }

    private final NamedParameterJdbcTemplate jdbc;

    @Override
    public Page<SearchHit> searchPosts(SearchCriteria criteria, Pageable pageable) {
        String rawQuery = criteria.query();
        boolean isChosung = isChosungQuery(rawQuery);
        String normalizedQuery = isChosung ? foldChosung(rawQuery) : rawQuery;
        var params = new MapSqlParameterSource("query", normalizedQuery);

        String where = isChosung
                ? "p.hidden_at IS NULL AND (to_chosung(p.title) LIKE '%' || :query || '%' OR to_chosung(p.nick_name) LIKE '%' || :query || '%')"
                : "p.hidden_at IS NULL AND (p.title ILIKE '%' || :query || '%' OR p.content ILIKE '%' || :query || '%' OR p.nick_name ILIKE '%' || :query || '%')";
        if (criteria.tag() != null) {
            where += " AND p.tag = :tag";
            params.addValue("tag", criteria.tag().name());
        }

        String orderBy = switch (criteria.sort()) {
            case RELEVANCE -> isChosung
                    ? "p.created_at DESC, p.id"
                    : "GREATEST(similarity(p.title, :query)*3, similarity(p.content, :query), similarity(p.nick_name, :query)*2) DESC, p.created_at DESC, p.id";
            case LATEST -> "p.created_at DESC, p.id";
            case COMMENTS -> "comment_count DESC, p.created_at DESC, p.id";
        };

        String pagination = paginate(pageable, params);

        String dataSql = """
                SELECT p.id, p.title, p.content, p.nick_name, p.tag, p.created_at,
                       (SELECT COUNT(*) FROM comment c WHERE c.post_id = p.id) AS comment_count
                FROM post p
                WHERE %s
                ORDER BY %s
                %s
                """.formatted(where, orderBy, pagination);

        String countSql = "SELECT COUNT(*) FROM post p WHERE " + where;

        List<SearchHit> hits = jdbc.query(dataSql, params, (rs, i) -> new SearchHit(
                HitType.POST,
                rs.getObject("id", UUID.class),
                rs.getString("title"),
                rs.getString("content"),
                rs.getString("nick_name"),
                PostTag.valueOf(rs.getString("tag")),
                rs.getLong("comment_count"),
                0L,
                rs.getObject("created_at", LocalDateTime.class)
        ));
        Long total = jdbc.queryForObject(countSql, params, Long.class);
        return new PageImpl<>(hits, pageable, total != null ? total : 0L);
    }

    @Override
    public Page<SearchHit> searchComments(SearchCriteria criteria, Pageable pageable) {
        String rawQuery = criteria.query();
        boolean isChosung = isChosungQuery(rawQuery);
        String normalizedQuery = isChosung ? foldChosung(rawQuery) : rawQuery;
        var params = new MapSqlParameterSource("query", normalizedQuery);

        String where = isChosung
                ? "c.deleted_at IS NULL AND c.hidden_at IS NULL AND (to_chosung(c.content) LIKE '%' || :query || '%' OR to_chosung(c.nick_name) LIKE '%' || :query || '%')"
                : "c.deleted_at IS NULL AND c.hidden_at IS NULL AND (c.content ILIKE '%' || :query || '%' OR c.nick_name ILIKE '%' || :query || '%')";

        String orderBy = switch (criteria.sort()) {
            case RELEVANCE -> isChosung
                    ? "c.created_at DESC, c.id"
                    : "GREATEST(similarity(c.content, :query), similarity(c.nick_name, :query)*2) DESC, c.created_at DESC, c.id";
            case LATEST, COMMENTS -> "c.created_at DESC, c.id";
        };

        String pagination = paginate(pageable, params);

        String dataSql = """
                SELECT c.id, c.content, c.nick_name, c.created_at
                FROM comment c
                WHERE %s
                ORDER BY %s
                %s
                """.formatted(where, orderBy, pagination);

        String countSql = "SELECT COUNT(*) FROM comment c WHERE " + where;

        List<SearchHit> hits = jdbc.query(dataSql, params, (rs, i) -> new SearchHit(
                HitType.COMMENT,
                rs.getObject("id", UUID.class),
                null,
                rs.getString("content"),
                rs.getString("nick_name"),
                null,
                0L,
                0L,
                rs.getObject("created_at", LocalDateTime.class)
        ));
        Long total = jdbc.queryForObject(countSql, params, Long.class);
        return new PageImpl<>(hits, pageable, total != null ? total : 0L);
    }

    @Override
    public Page<SearchHit> searchUsers(SearchCriteria criteria, Pageable pageable) {
        String rawQuery = criteria.query();
        boolean isChosung = isChosungQuery(rawQuery);
        String normalizedQuery = isChosung ? foldChosung(rawQuery) : rawQuery;
        var params = new MapSqlParameterSource("query", normalizedQuery);

        String where = isChosung
                ? "hidden_at IS NULL AND to_chosung(nick_name) LIKE '%' || :query || '%'"
                : "hidden_at IS NULL AND nick_name ILIKE '%' || :query || '%'";
        String pagination = paginate(pageable, params);

        String scoreExpr = isChosung
                ? "MAX(similarity(to_chosung(nick_name), :query))"
                : "MAX(similarity(nick_name, :query))";

        String dataSql = """
                SELECT nick_name, COUNT(*) AS post_count, MAX(created_at) AS last_at,
                       %s AS score
                FROM post
                WHERE %s
                GROUP BY nick_name
                ORDER BY score DESC, post_count DESC, nick_name
                %s
                """.formatted(scoreExpr, where, pagination);

        String countSql = "SELECT COUNT(DISTINCT nick_name) FROM post WHERE " + where;

        List<SearchHit> hits = jdbc.query(dataSql, params, (rs, i) -> new SearchHit(
                HitType.USER,
                null,
                null,
                null,
                rs.getString("nick_name"),
                null,
                0L,
                rs.getLong("post_count"),
                rs.getObject("last_at", LocalDateTime.class)
        ));
        Long total = jdbc.queryForObject(countSql, params, Long.class);
        return new PageImpl<>(hits, pageable, total != null ? total : 0L);
    }

    @Override
    public List<String> suggest(String query, int limit) {
        if (query == null || query.isBlank()) return List.of();

        boolean isChosung = isChosungQuery(query);
        String normalizedQuery = isChosung ? foldChosung(query) : query;
        var params = new MapSqlParameterSource("query", normalizedQuery).addValue("limit", limit);

        String titleSql = isChosung ? """
                SELECT s, cnt FROM (
                  SELECT title AS s, COUNT(*) AS cnt
                  FROM post
                  WHERE hidden_at IS NULL AND to_chosung(title) LIKE :query || '%'
                  GROUP BY title
                ) t
                ORDER BY (CASE WHEN s ILIKE :query || '%' THEN 0 ELSE 1 END), cnt DESC, char_length(s) ASC, s
                LIMIT :limit
                """ : """
                SELECT s, cnt FROM (
                  SELECT title AS s, COUNT(*) AS cnt
                  FROM post
                  WHERE hidden_at IS NULL AND to_jamo(title) LIKE to_jamo(:query) || '%'
                  GROUP BY title
                ) t
                ORDER BY (CASE WHEN s ILIKE :query || '%' THEN 0 ELSE 1 END), cnt DESC, char_length(s) ASC, s
                LIMIT :limit
                """;

        String nickSql = isChosung ? """
                SELECT s, cnt FROM (
                  SELECT nick_name AS s, COUNT(*) AS cnt
                  FROM post
                  WHERE hidden_at IS NULL AND to_chosung(nick_name) LIKE :query || '%'
                  GROUP BY nick_name
                ) t
                ORDER BY (CASE WHEN s ILIKE :query || '%' THEN 0 ELSE 1 END), cnt DESC, char_length(s) ASC, s
                LIMIT :limit
                """ : """
                SELECT s, cnt FROM (
                  SELECT nick_name AS s, COUNT(*) AS cnt
                  FROM post
                  WHERE hidden_at IS NULL AND to_jamo(nick_name) LIKE to_jamo(:query) || '%'
                  GROUP BY nick_name
                ) t
                ORDER BY (CASE WHEN s ILIKE :query || '%' THEN 0 ELSE 1 END), cnt DESC, char_length(s) ASC, s
                LIMIT :limit
                """;

        record Candidate(String value, long cnt) {}

        List<Candidate> titles = jdbc.query(titleSql, params,
                (rs, i) -> new Candidate(rs.getString("s"), rs.getLong("cnt")));
        List<Candidate> nicks = jdbc.query(nickSql, params,
                (rs, i) -> new Candidate(rs.getString("s"), rs.getLong("cnt")));

        List<Candidate> merged = new ArrayList<>();
        merged.addAll(titles);
        merged.addAll(nicks);
        // 정렬: ① 접두 일치 ASC (0=일치, 1=불일치) → ② 인기(cnt) DESC → ③ 길이 ASC → ④ 값 ASC
        Comparator<Candidate> byPrefixTier = Comparator.comparingInt(
                c -> c.value().toLowerCase().startsWith(query.toLowerCase()) ? 0 : 1);
        Comparator<Candidate> byCntDesc = Comparator.comparingLong(Candidate::cnt).reversed();
        Comparator<Candidate> byLength = Comparator.comparingInt(c -> c.value().length());
        Comparator<Candidate> byValue = Comparator.comparing(Candidate::value);
        merged.sort(byPrefixTier.thenComparing(byCntDesc).thenComparing(byLength).thenComparing(byValue));

        LinkedHashSet<String> deduped = new LinkedHashSet<>();
        for (Candidate c : merged) {
            deduped.add(c.value());
            if (deduped.size() >= limit) break;
        }
        return new ArrayList<>(deduped);
    }

    /**
     * 페이지네이션 절을 만든다. unpaged(전체 조회, 예: 통합검색 병합용)면 LIMIT/OFFSET 없이 전체 반환.
     * Unpaged.getPageSize()는 UnsupportedOperationException을 던지므로 반드시 isUnpaged 분기 필요.
     */
    private static String paginate(Pageable pageable, MapSqlParameterSource params) {
        if (pageable.isUnpaged()) {
            return "";
        }
        params.addValue("limit", pageable.getPageSize()).addValue("offset", pageable.getOffset());
        return "LIMIT :limit OFFSET :offset";
    }
}
