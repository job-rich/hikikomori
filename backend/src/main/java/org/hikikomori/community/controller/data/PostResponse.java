package org.hikikomori.community.controller.data;

import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import org.hikikomori.community.domain.Post;

@Getter
@Builder
public class PostResponse {

    private final UUID id;
    private final String title;
    private final String content;
    private final String createdAt;

    public static PostResponse from(Post post) {
        return PostResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .createdAt(post.getCreatedAt().toString())
                .build();
    }
}
