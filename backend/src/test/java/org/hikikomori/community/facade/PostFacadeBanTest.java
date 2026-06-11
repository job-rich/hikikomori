package org.hikikomori.community.facade;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.hikikomori.community.domain.PostTag;
import org.hikikomori.community.dto.PostDto;
import org.hikikomori.community.exception.BannedUserException;
import org.hikikomori.community.repository.BanRepositoryImpl;
import org.hikikomori.community.repository.CommentRepositoryImpl;
import org.hikikomori.community.repository.PostLikeRepositoryImpl;
import org.hikikomori.community.repository.PostRepositoryImpl;
import org.hikikomori.community.service.BanService;
import org.hikikomori.community.service.CommentService;
import org.hikikomori.community.service.PostLikeService;
import org.hikikomori.community.service.PostService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PostFacadeBanTest {

    @Mock PostRepositoryImpl postRepository;
    @Mock CommentRepositoryImpl commentRepository;
    @Mock BanRepositoryImpl banRepository;
    @Mock PostLikeRepositoryImpl postLikeRepository;

    PostService postService = new PostService();
    CommentService commentService = new CommentService();
    PostLikeService postLikeService = new PostLikeService();
    BanService banService = new BanService();

    PostFacade facade() {
        return new PostFacade(
                postService, commentService, postLikeService, banService,
                postRepository, commentRepository, banRepository, postLikeRepository);
    }

    @Test
    @DisplayName("밴된 사용자는 게시글을 작성할 수 없다")
    void 밴_사용자_게시글_차단() {
        given(banRepository.isBanned(1L)).willReturn(true);
        PostDto.CreateRequest req = new PostDto.CreateRequest("t", "c", PostTag.ETC, 1L, "n");

        assertThatThrownBy(() -> facade().createPost(req))
                .isInstanceOf(BannedUserException.class);
        verify(postRepository, never()).save(any());
    }
}
