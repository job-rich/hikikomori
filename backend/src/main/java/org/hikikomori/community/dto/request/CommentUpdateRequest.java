package org.hikikomori.community.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CommentUpdateRequest(
        @NotNull Long userId,
        @NotBlank String content
) {}
