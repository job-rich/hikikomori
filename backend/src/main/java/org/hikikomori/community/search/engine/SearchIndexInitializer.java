package org.hikikomori.community.search.engine;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SearchIndexInitializer implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        createExtension();
        createChosungFunction();
        createJamoFunction();
        createIndex("idx_post_title_trgm", "post", "title");
        createIndex("idx_post_content_trgm", "post", "content");
        createIndex("idx_post_nick_name_trgm", "post", "nick_name");
        createIndex("idx_comment_content_trgm", "comment", "content");
        createIndex("idx_comment_nick_name_trgm", "comment", "nick_name");
        createExpressionIndex("idx_post_title_chosung_trgm",
                "post USING gin (to_chosung(title) gin_trgm_ops)");
        createExpressionIndex("idx_post_nick_name_chosung_trgm",
                "post USING gin (to_chosung(nick_name) gin_trgm_ops)");
        createExpressionIndex("idx_comment_content_chosung_trgm",
                "comment USING gin (to_chosung(content) gin_trgm_ops)");
        createExpressionIndex("idx_comment_nick_name_chosung_trgm",
                "comment USING gin (to_chosung(nick_name) gin_trgm_ops)");
        createExpressionIndex("idx_post_title_jamo_trgm",
                "post USING gin (to_jamo(title) gin_trgm_ops)");
        createExpressionIndex("idx_post_nick_name_jamo_trgm",
                "post USING gin (to_jamo(nick_name) gin_trgm_ops)");
    }

    private void createChosungFunction() {
        String sql = """
                CREATE OR REPLACE FUNCTION to_chosung(input text)
                RETURNS text LANGUAGE plpgsql IMMUTABLE PARALLEL SAFE AS $$
                DECLARE
                    result text := ''; ch text; code int; cho int; i int;
                    folded text[] := ARRAY['ㄱ','ㄱ','ㄴ','ㄷ','ㄷ','ㄹ','ㅁ','ㅂ','ㅂ','ㅅ','ㅅ','ㅇ','ㅈ','ㅈ','ㅊ','ㅋ','ㅌ','ㅍ','ㅎ'];
                BEGIN
                    IF input IS NULL THEN RETURN NULL; END IF;
                    FOR i IN 1 .. char_length(input) LOOP
                        ch := substr(input, i, 1); code := ascii(ch);
                        IF code >= 44032 AND code <= 55203 THEN
                            cho := (code - 44032) / 588;
                            result := result || folded[cho + 1];
                        ELSE result := result || ch; END IF;
                    END LOOP;
                    RETURN result;
                END; $$;
                """;
        try {
            jdbcTemplate.execute(sql);
            log.info("to_chosung 함수 생성 완료");
        } catch (Exception e) {
            log.warn("to_chosung 함수 생성 실패: {}", e.getMessage());
        }
    }

    private void createJamoFunction() {
        String sql = """
                CREATE OR REPLACE FUNCTION to_jamo(input text)
                RETURNS text LANGUAGE plpgsql IMMUTABLE PARALLEL SAFE AS $$
                DECLARE
                    result text := ''; ch text; code int; idx int; cho int; jung int; jong int; i int;
                    cho_arr  text[] := ARRAY['ㄱ','ㄲ','ㄴ','ㄷ','ㄸ','ㄹ','ㅁ','ㅂ','ㅃ','ㅅ','ㅆ','ㅇ','ㅈ','ㅉ','ㅊ','ㅋ','ㅌ','ㅍ','ㅎ'];
                    jung_arr text[] := ARRAY['ㅏ','ㅐ','ㅑ','ㅒ','ㅓ','ㅔ','ㅕ','ㅖ','ㅗ','ㅘ','ㅙ','ㅚ','ㅛ','ㅜ','ㅝ','ㅞ','ㅟ','ㅠ','ㅡ','ㅢ','ㅣ'];
                    jong_arr text[] := ARRAY['','ㄱ','ㄲ','ㄳ','ㄴ','ㄵ','ㄶ','ㄷ','ㄹ','ㄺ','ㄻ','ㄼ','ㄽ','ㄾ','ㄿ','ㅀ','ㅁ','ㅂ','ㅄ','ㅅ','ㅆ','ㅇ','ㅈ','ㅊ','ㅋ','ㅌ','ㅍ','ㅎ'];
                BEGIN
                    IF input IS NULL THEN RETURN NULL; END IF;
                    FOR i IN 1 .. char_length(input) LOOP
                        ch := substr(input, i, 1); code := ascii(ch);
                        IF code >= 44032 AND code <= 55203 THEN
                            idx := code - 44032;
                            cho := idx / 588; jung := (idx % 588) / 28; jong := idx % 28;
                            result := result || cho_arr[cho+1] || jung_arr[jung+1] || jong_arr[jong+1];
                        ELSE result := result || ch; END IF;
                    END LOOP;
                    RETURN result;
                END; $$;
                """;
        try {
            jdbcTemplate.execute(sql);
            log.info("to_jamo 함수 생성 완료");
        } catch (Exception e) {
            log.warn("to_jamo 함수 생성 실패: {}", e.getMessage());
        }
    }

    private void createExpressionIndex(String indexName, String tableAndExpr) {
        String sql = String.format(
                "CREATE INDEX IF NOT EXISTS %s ON %s",
                indexName, tableAndExpr
        );
        try {
            jdbcTemplate.execute(sql);
            log.info("표현식 GIN 인덱스 생성 완료: {}", indexName);
        } catch (Exception e) {
            log.warn("표현식 GIN 인덱스 생성 실패: {} — {}", indexName, e.getMessage());
        }
    }

    private void createExtension() {
        try {
            jdbcTemplate.execute("CREATE EXTENSION IF NOT EXISTS pg_trgm");
            log.info("pg_trgm extension 준비 완료");
        } catch (Exception e) {
            log.warn("pg_trgm extension 생성 실패 (superuser 권한 필요 또는 비-Postgres 환경) — 부팅 계속: {}", e.getMessage());
        }
    }

    private void createIndex(String indexName, String table, String column) {
        String sql = String.format(
                "CREATE INDEX IF NOT EXISTS %s ON %s USING gin (%s gin_trgm_ops)",
                indexName, table, column
        );
        try {
            jdbcTemplate.execute(sql);
            log.info("GIN 인덱스 생성 완료: {}", indexName);
        } catch (Exception e) {
            log.warn("GIN 인덱스 생성 실패: {} — {}", indexName, e.getMessage());
        }
    }
}
