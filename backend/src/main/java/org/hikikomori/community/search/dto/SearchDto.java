package org.hikikomori.community.search.dto;

import java.time.LocalDateTime;
import java.util.UUID;
import org.hikikomori.community.search.model.SearchHit;

public class SearchDto {

    public enum Type { ALL, POST, COMMENT, USER }

    public record Response(
            String type,
            UUID id,
            String title,
            String nickName,
            String tag,
            String snippet,
            long commentCount,
            long postCount,
            LocalDateTime createdAt
    ) {
        public static Response from(SearchHit hit, String snippet) {
            return new Response(
                    hit.type().name(),
                    hit.id(),
                    hit.title(),
                    hit.nickName(),
                    hit.tag() != null ? hit.tag().name() : null,
                    snippet,
                    hit.commentCount(),
                    hit.postCount(),
                    hit.createdAt()
            );
        }
    }
}
