package org.hikikomori.community.service;

import org.hikikomori.community.domain.Post;
import org.hikikomori.community.dto.PostDto;
import org.springframework.stereotype.Service;

@Service
public class PostService {

    public Post buildPost(PostDto.CreateRequest request) {
        return Post.builder()
                .userId(request.userId())
                .nickName(request.nickName())
                .title(request.title())
                .content(request.content())
                .tag(request.tag())
                .build();
    }

    public void checkOwnership(Post post, Long userId, String operation) {
        if (!post.getUserId().equals(userId)) {
            throw new IllegalArgumentException("본인의 게시글만 " + operation + "할 수 있습니다");
        }
    }

    public void applyUpdate(Post post, PostDto.UpdateRequest request) {
        post.update(request.title(), request.content(), request.tag());
    }
}
