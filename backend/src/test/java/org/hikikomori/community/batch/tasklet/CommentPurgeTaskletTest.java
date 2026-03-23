package org.hikikomori.community.batch.tasklet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.time.LocalDate;
import org.hikikomori.community.batch.domain.tasklet.CommentPurgeTasklet;
import org.hikikomori.community.repository.CommentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.JobInstance;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.scope.context.StepContext;
import org.springframework.batch.core.step.StepContribution;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;

@ExtendWith(MockitoExtension.class)
class CommentPurgeTaskletTest {

    @InjectMocks
    private CommentPurgeTasklet commentPurgeTasklet;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private StepContribution contribution;

    private final LocalDate startDate = LocalDate.of(2026, 3, 1);
    private final LocalDate endDate = LocalDate.of(2026, 3, 2);

    @Spy
    ChunkContext chunkContext = buildChunkContext();

    @Test
    @DisplayName("댓글 퍼지 - 삭제 건수를 writeCount에 기록하고 FINISHED 반환")
    void execute() {
        given(commentRepository.deleteByCreatedAtBetween(
                startDate.atStartOfDay(), endDate.atStartOfDay())).willReturn(5L);

        RepeatStatus status = commentPurgeTasklet.execute(contribution, chunkContext);

        assertThat(status).isEqualTo(RepeatStatus.FINISHED);
        verify(commentRepository).deleteByCreatedAtBetween(startDate.atStartOfDay(), endDate.atStartOfDay());
        verify(contribution).incrementWriteCount(5L);
    }

    @Test
    @DisplayName("댓글 퍼지 - 삭제 대상 없을 때 writeCount 0 기록")
    void executeWithNoData() {
        given(commentRepository.deleteByCreatedAtBetween(
                startDate.atStartOfDay(), endDate.atStartOfDay())).willReturn(0L);

        commentPurgeTasklet.execute(contribution, chunkContext);

        verify(contribution).incrementWriteCount(0L);
    }

    private ChunkContext buildChunkContext() {
        var jobParameters = new JobParametersBuilder()
                .addString("startDate", startDate.toString())
                .addString("endDate", endDate.toString())
                .toJobParameters();
        var jobExecution = new JobExecution(1L, new JobInstance(1L, "purgeJob"), jobParameters);
        return new ChunkContext(new StepContext(new StepExecution(1L, "commentPurgeStep", jobExecution)));
    }
}
