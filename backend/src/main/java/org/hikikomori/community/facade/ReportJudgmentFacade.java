package org.hikikomori.community.facade;

import lombok.RequiredArgsConstructor;
import org.hikikomori.community.config.ReportPolicyProperties;
import org.hikikomori.community.domain.Ban;
import org.hikikomori.community.domain.Comment;
import org.hikikomori.community.domain.Post;
import org.hikikomori.community.event.ReportCreatedEvent;
import org.hikikomori.community.repository.BanRepositoryImpl;
import org.hikikomori.community.repository.CommentRepositoryImpl;
import org.hikikomori.community.repository.PostRepositoryImpl;
import org.hikikomori.community.repository.ReportRepositoryImpl;
import org.hikikomori.community.service.ReportService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReportJudgmentFacade {

    private final ReportService reportService;
    private final ReportRepositoryImpl reportRepository;
    private final BanRepositoryImpl banRepository;
    private final PostRepositoryImpl postRepository;
    private final CommentRepositoryImpl commentRepository;
    private final ReportPolicyProperties properties;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void judge(ReportCreatedEvent event) {
        long reporters = reportRepository.countDistinctReporters(event.targetType(), event.targetId());
        if (!reportService.shouldHide(reporters, properties.hideThreshold())) {
            return;
        }
        boolean newlyHidden = hideTarget(event);
        if (!newlyHidden) {
            return; // 이미 숨김 처리된 콘텐츠 — 작성자 누적 변화 없음, 밴 재집계 불필요
        }

        long hiddenContents = reportRepository.countHiddenContents(
                event.targetUserId(), properties.hideThreshold());
        if (reportService.shouldBan(hiddenContents, properties.banThreshold())
                && !banRepository.isBanned(event.targetUserId())) {
            banRepository.save(Ban.builder()
                    .userId(event.targetUserId())
                    .reason("신고 누적")
                    .build());
        }
    }

    private boolean hideTarget(ReportCreatedEvent event) {
        switch (event.targetType()) {
            case POST -> {
                Post post = postRepository.getById(event.targetId());
                if (post.isHidden()) {
                    return false;
                }
                post.hide();
                postRepository.save(post);
                return true;
            }
            case COMMENT -> {
                Comment comment = commentRepository.getById(event.targetId());
                if (comment.isHidden()) {
                    return false;
                }
                comment.hide();
                commentRepository.save(comment);
                return true;
            }
        }
        return false;
    }
}
