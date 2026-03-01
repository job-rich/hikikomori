package org.hikikomori.community.batch.domain.tasklet;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
public class CommentPurgeTasklet implements Tasklet {

    private final CommentRepository commentRepository;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        var jobParams = chunkContext.getStepContext().getStepExecution().getJobParameters();
        LocalDateTime startAt = LocalDate.parse(jobParams.getString("startAt")).atStartOfDay();
        LocalDateTime endAt = LocalDate.parse(jobParams.getString("endAt")).atStartOfDay();

        int deletedCount = commentRepository.deleteByCreatedAtBetween(startAt, endAt);

        log.info("댓글 퍼지 완료: {}건 (기간: {} ~ {})", deletedCount, startAt, endAt);

        return RepeatStatus.FINISHED;
    }
}
