package org.hikikomori.community.service;

import org.hikikomori.community.domain.Post;
import org.hikikomori.community.service.vo.PostCreate;
import org.hikikomori.community.service.vo.PostUpdate;
import org.springframework.stereotype.Service;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    public Page<Post> findAll(Pageable pageable) {
        return postRepository.findAll(pageable);
    }

    public Page<Post> findByUserId(Long userId, Pageable pageable) {
        return postRepository.findByUserId(userId, pageable);
    }

    public Post findById(UUID id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다: " + id));
    }

    public Post create(PostCreateRequest request) {
        Post post = Post.builder()
                .userId(request.getUserId())
                .nickName(request.getNickName())
                .title(request.getTitle())
                .content(request.getContent())
                .tag(request.getTag())
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
