package org.hikikomori.community.controller.data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PostUpdateRequest {

    @NotNull
    private Long userId;

    @NotBlank
    private String title;

    @NotBlank
    private String content;

    @NotBlank
    private String tag;

    @Builder
    private PostUpdateRequest(Long userId, String title, String content, String tag) {
        this.userId = userId;
        this.title = title;
        this.content = content;
        this.tag = tag;
    }
}
