package org.hikikomori.community.search.model;

import java.time.LocalDateTime;
import java.util.UUID;
import org.hikikomori.community.domain.PostTag;

public record SearchHit(
        HitType type,
        UUID id,
        String title,
        String content,
        String nickName,
        PostTag tag,
        long commentCount,
        long postCount,
        LocalDateTime createdAt
) {}
