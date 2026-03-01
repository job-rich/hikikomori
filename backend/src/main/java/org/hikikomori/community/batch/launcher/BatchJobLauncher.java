package org.hikikomori.community.batch.launcher;

import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BatchJobLauncher {

    private final JobOperator jobOperator;
    private final Job purgeJob;

    public Long runManual(LocalDate startAt, LocalDate endAt) {
        return launch("MANUAL", startAt, endAt);
    }

    public Long runAuto() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        LocalDate today = LocalDate.now();
        return launch("AUTO", yesterday, today);
    }

    private Long launch(String triggerType, LocalDate startAt, LocalDate endAt) {
        try {
            JobParameters params = new JobParametersBuilder()
                    .addLong("timestamp", System.currentTimeMillis())
                    .addString("triggerType", triggerType)
                    .addString("startAt", startAt.toString())
                    .addString("endAt", endAt.toString())
                    .toJobParameters();

            JobExecution execution = jobOperator.start(purgeJob, params);
            log.info("퍼지 배치 작업 실행 완료 (executionId: {}, triggerType: {}, startAt: {}, endAt: {})",
                    execution.getId(), triggerType, startAt, endAt);

            return execution.getId();
        } catch (Exception e) {
            log.error("퍼지 배치 작업 실행 실패 (triggerType: {})", triggerType, e);
            throw new RuntimeException("배치 작업 실행 실패: " + e.getMessage(), e);
        }
    }
}
