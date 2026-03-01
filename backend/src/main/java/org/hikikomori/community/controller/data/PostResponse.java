package org.hikikomori.community.controller.data;

import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import org.hikikomori.community.domain.Post;

@Getter
@Builder
public class PostResponse {

    private final UUID id;
    private final Long userId;
    private final String nickName;
    private final String title;
    private final String content;
    private final String tag;
    private final String createdAt;

    public static PostResponse from(Post post) {
        return PostResponse.builder()
                .id(post.getId())
                .userId(post.getUserId())
                .nickName(post.getNickName())
                .title(post.getTitle())
                .content(post.getContent())
                .tag(post.getTag())
                .createdAt(post.getCreatedAt().toString())
                .build();
    }
}
