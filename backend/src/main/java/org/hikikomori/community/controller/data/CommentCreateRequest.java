package org.hikikomori.community.controller.data;

import jakarta.validation.constraints.NotBlank;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CommentCreateRequest {

    @NotBlank
    private String content;

    private UUID parentId;

    private Long userId;

    private String nickName;
}
