package org.hikikomori.community.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class BatchService {

    private final JobOperator jobOperator;
    private final Job cleanupJob;

    public Long runCleanupJob() {
        try {
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("timestamp", System.currentTimeMillis())
                    .toJobParameters();

            JobExecution execution = jobOperator.start(cleanupJob, jobParameters);
            log.info("정리 배치 작업 실행 완료 (executionId: {})", execution.getId());

            return execution.getId();
        } catch (Exception e) {
            log.error("정리 배치 작업 실행 실패", e);

            throw new RuntimeException("배치 작업 실행 실패: " + e.getMessage(), e);
        }
    }
}
