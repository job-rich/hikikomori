package org.hikikomori.community.batch.domain.tasklet;

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
public class CommentPurgeTasklet implements Tasklet {

    private final CommentRepository commentRepository;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        var jobParams = chunkContext.getStepContext().getStepExecution().getJobParameters();
        LocalDate startDate = LocalDate.parse(jobParams.getString("startDate"));
        LocalDate endDate = LocalDate.parse(jobParams.getString("endDate"));

        long deletedCount = commentRepository.deleteByCreatedAtBetween(startDate.atStartOfDay(), endDate.atStartOfDay());

        contribution.incrementWriteCount(deletedCount);
        log.info("댓글 퍼지 완료: {}건 (기간: {} ~ {})", deletedCount, startDate, endDate);

        return RepeatStatus.FINISHED;
    }
}
