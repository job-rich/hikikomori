package org.hikikomori.community.batch.tasklet;

import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hikikomori.community.repository.CommentRepository;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.StepContribution;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CommentCleanupTasklet implements Tasklet {

    private final CommentRepository commentRepository;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        LocalDate today = LocalDate.now();
        int deletedCount = commentRepository.deleteByCreatedAtBefore(today.atStartOfDay());

        log.info("댓글 삭제 완료: {}건 (기준일: {} 미만)", deletedCount, today);

        return RepeatStatus.FINISHED;
    }
}
