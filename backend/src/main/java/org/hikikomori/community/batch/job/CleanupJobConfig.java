package org.hikikomori.community.batch.job;

import lombok.RequiredArgsConstructor;
import org.hikikomori.community.batch.tasklet.CommentCleanupTasklet;
import org.hikikomori.community.batch.tasklet.PostCleanupTasklet;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
public class CleanupJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final CommentCleanupTasklet commentCleanupTasklet;
    private final PostCleanupTasklet postCleanupTasklet;

    @Bean
    public Job cleanupJob() {
        return new JobBuilder("cleanupJob", jobRepository)
                .start(commentCleanupStep())
                .next(postCleanupStep())
                .build();
    }

    @Bean
    public Step commentCleanupStep() {
        return new StepBuilder("commentCleanupStep", jobRepository)
                .tasklet(commentCleanupTasklet, transactionManager)
                .build();
    }

    @Bean
    public Step postCleanupStep() {
        return new StepBuilder("postCleanupStep", jobRepository)
                .tasklet(postCleanupTasklet, transactionManager)
                .build();
    }
}
