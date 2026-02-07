package org.hikikomori.community.controller.data;

import org.hikikomori.community.domain.Post;

public record PostResponse(Long id, String title, String content, String createdAt) {

    public static PostResponse from(Post post) {
        return new PostResponse(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getCreatedAt().toString()
        );
    }
}
