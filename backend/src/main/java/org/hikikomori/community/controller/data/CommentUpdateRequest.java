package org.hikikomori.community.controller.data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CommentUpdateRequest {

    @NotNull
    private Long userId;

    @NotBlank
    private String content;

    @Builder
    private CommentUpdateRequest(Long userId, String content) {
        this.userId = userId;
        this.content = content;
    }
}
