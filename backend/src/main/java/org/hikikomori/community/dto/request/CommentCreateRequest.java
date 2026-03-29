package org.hikikomori.community.dto.request;

import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

public record CommentCreateRequest(
        @NotBlank String content,
        UUID parentId,
        Long userId,
        String nickName
) {}
