package org.hikikomori.community.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PostCreateRequest(
        @NotBlank String title,
        @NotBlank String content,
        @NotBlank String tag,
        @NotNull Long userId,
        @NotBlank String nickName
) {}
