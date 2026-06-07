package org.hikikomori.community.facade;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.hikikomori.community.domain.Comment;
import org.hikikomori.community.domain.Post;
import org.hikikomori.community.domain.VoteValue;
import org.hikikomori.community.dto.VoteDto;
import org.hikikomori.community.repository.CommentRepositoryImpl;
import org.hikikomori.community.repository.PostRepositoryImpl;
import org.hikikomori.community.repository.VoteRepositoryImpl;
import org.hikikomori.community.service.VoteService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class VoteFacade {

    private final VoteService voteService;
    private final VoteRepositoryImpl voteRepository;
    private final PostRepositoryImpl postRepository;
    private final CommentRepositoryImpl commentRepository;

    @Transactional
    public VoteDto.Response vote(Long targetUserId, VoteDto.CreateRequest request) {
        Long actualAuthor = resolveTargetAuthor(request);
        voteService.checkTargetAuthor(actualAuthor, targetUserId);
        voteService.checkNotSelfVote(request.voterId(), targetUserId);

        boolean up = voteRepository.has(request.voterId(), request.targetType(), request.targetId(), VoteValue.UP);
        boolean down = voteRepository.has(request.voterId(), request.targetType(), request.targetId(), VoteValue.DOWN);

        List<VoteService.VoteAction> actions = voteService.resolveActions(up, down, request.value());
        actions.forEach(action -> voteRepository.save(voteService.buildVote(targetUserId, request, action)));

        // 토글 후 내 표: 요청 value를 +1로 행사한 액션이 있으면 그 value, 없으면(취소) null
        VoteValue myVote = actions.stream()
                .anyMatch(a -> a.value() == request.value() && a.delta() > 0) ? request.value() : null;
        long score = voteRepository.netByContent(request.targetType(), request.targetId());
        return VoteDto.Response.of(myVote, score);
    }

    private Long resolveTargetAuthor(VoteDto.CreateRequest request) {
        return switch (request.targetType()) {
            case POST -> {
                Post post = postRepository.getById(request.targetId());
                yield post.getUserId();
            }
            case COMMENT -> {
                Comment comment = commentRepository.getById(request.targetId());
                yield comment.getUserId();
            }
        };
    }
}
