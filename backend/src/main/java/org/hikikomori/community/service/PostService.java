package org.hikikomori.community.service;

import org.hikikomori.community.domain.Post;
import org.hikikomori.community.service.vo.PostCreate;
import org.hikikomori.community.service.vo.PostUpdate;
import org.springframework.stereotype.Service;

@Service
public class PostService {

    public Post buildPost(PostCreate postCreate) {
        return Post.builder()
                .userId(postCreate.userId())
                .nickName(postCreate.nickName())
                .title(postCreate.title())
                .content(postCreate.content())
                .tag(postCreate.tag())
                .build();
    }

    public void validateOwnership(Post post, Long userId, String operation) {
        if (!post.getUserId().equals(userId)) {
            throw new IllegalArgumentException("본인의 게시글만 " + operation + "할 수 있습니다");
        }
    }

    public void applyUpdate(Post post, PostUpdate postUpdate) {
        post.update(postUpdate.title(), postUpdate.content(), postUpdate.tag());
    }
}
