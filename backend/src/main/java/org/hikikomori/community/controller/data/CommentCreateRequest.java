package org.hikikomori.community.controller.data;

import jakarta.validation.constraints.NotBlank;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CommentCreateRequest {

    @NotBlank
    private String content;

    private UUID parentId;

    private Long userId;

    private String nickName;

    @Builder
    private CommentCreateRequest(String content, UUID parentId, Long userId, String nickName) {
        this.content = content;
        this.parentId = parentId;
        this.userId = userId;
        this.nickName = nickName;
    }
}
