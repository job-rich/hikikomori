package org.hikikomori.community.batch.launcher;

import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hikikomori.community.batch.controller.data.PurgeResponse;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BatchJobLauncher {

    private final JobOperator jobOperator;
    private final Job purgeJob;

    public PurgeResponse runManual(LocalDate startDate, LocalDate endDate) {
        return launch("MANUAL", startDate, endDate);
    }

    public PurgeResponse runAuto() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        LocalDate today = LocalDate.now();
        return launch("AUTO", yesterday, today);
    }

    private PurgeResponse launch(String triggerType, LocalDate startDate, LocalDate endDate) {
        try {
            JobParameters params = new JobParametersBuilder()
                    .addLong("timestamp", System.currentTimeMillis())
                    .addString("triggerType", triggerType)
                    .addString("startDate", startDate.toString())
                    .addString("endDate", endDate.toString())
                    .toJobParameters();

            JobExecution execution = jobOperator.start(purgeJob, params);

            long deletedCommentCount = getDeletedCount(execution, "commentPurgeStep");
            long deletedPostCount = getDeletedCount(execution, "postPurgeStep");

            log.info("퍼지 배치 작업 실행 완료 (executionId: {}, triggerType: {}, 댓글: {}건, 게시글: {}건)",
                    execution.getId(), triggerType, deletedCommentCount, deletedPostCount);

            return PurgeResponse.builder()
                    .executionId(execution.getId())
                    .deletedCommentCount(deletedCommentCount)
                    .deletedPostCount(deletedPostCount)
                    .totalDeletedCount(deletedCommentCount + deletedPostCount)
                    .build();
        } catch (Exception e) {
            log.error("퍼지 배치 작업 실행 실패 (triggerType: {})", triggerType, e);
            throw new RuntimeException("배치 작업 실행 실패: " + e.getMessage(), e);
        }
    }

    private long getDeletedCount(JobExecution execution, String stepName) {
        return execution.getStepExecutions().stream()
                .filter(stepExecution -> stepName.equals(stepExecution.getStepName()))
                .findFirst()
                .map(StepExecution::getWriteCount)
                .orElse(0L);
    }
}
