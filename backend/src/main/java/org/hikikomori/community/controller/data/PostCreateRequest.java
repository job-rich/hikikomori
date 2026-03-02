package org.hikikomori.community.controller.data;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PostCreateRequest {

    @NotBlank
    private String title;

    @NotBlank
    private String content;

    @NotBlank
    private String tag;

    @NotNull
    private Long userId;
    
    @NotBlank
    private String nickName;

    @Builder
    private PostCreateRequest(String title, String content, Long userId, String nickName) {
        this.title = title;
        this.content = content;
        this.userId = userId;
        this.nickName = nickName;
    }
}
