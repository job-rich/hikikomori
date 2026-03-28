package org.hikikomori.community.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PostUpdateRequest(
        @NotNull Long userId,
        @NotBlank String title,
        @NotBlank String content,
        @NotBlank String tag
) {}
