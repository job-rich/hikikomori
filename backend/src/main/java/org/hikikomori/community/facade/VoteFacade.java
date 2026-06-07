package org.hikikomori.community.facade;

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

        for (VoteService.VoteAction action : voteService.resolveActions(up, down, request.value())) {
            voteRepository.save(voteService.buildVote(targetUserId, request, action));
        }

        VoteValue myVote = voteService.resultVote(up, down, request.value());
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
