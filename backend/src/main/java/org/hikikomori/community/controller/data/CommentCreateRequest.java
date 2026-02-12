package org.hikikomori.community.controller.data;

import jakarta.validation.constraints.NotBlank;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CommentCreateRequest {

    @NotBlank
    private String content;

    private UUID parentId;
}
