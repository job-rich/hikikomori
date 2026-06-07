package org.hikikomori.community.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import org.hikikomori.community.domain.VoteTargetType;
import org.hikikomori.community.domain.VoteValue;

public class VoteDto {

    public record CreateRequest(
            @NotNull Long voterId,
            @NotNull VoteTargetType targetType,
            @NotNull UUID targetId,
            @NotNull VoteValue value
    ) {}

    // value: 토글 후 내 표 상태(취소면 null), score: 대상 콘텐츠 현재 순추천
    public record Response(VoteValue value, long score) {
        public static Response of(VoteValue value, long score) {
            return new Response(value, score);
        }
    }
}
