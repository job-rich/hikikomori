package org.hikikomori.community.controller.data;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PostCreateRequest {

    @NotBlank
    private String title;

    @NotBlank
    private String content;

    private Long userId;

    private String nickName;
}
