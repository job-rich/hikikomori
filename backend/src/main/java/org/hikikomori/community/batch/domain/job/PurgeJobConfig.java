package org.hikikomori.community.batch.domain.job;

import lombok.RequiredArgsConstructor;
import org.hikikomori.community.batch.domain.tasklet.CommentPurgeTasklet;
import org.hikikomori.community.batch.domain.tasklet.PostPurgeTasklet;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class PurgeJobConfig {

    private final JobRepository jobRepository;
    private final CommentPurgeTasklet commentPurgeTasklet;
    private final PostPurgeTasklet postPurgeTasklet;

    @Bean
    public Job purgeJob() {
        return new JobBuilder("purgeJob", jobRepository)
                .start(commentPurgeStep())
                .next(postPurgeStep())
                .build();
    }

    @Bean
    public Step commentPurgeStep() {
        return new StepBuilder("commentPurgeStep", jobRepository)
                .tasklet(commentPurgeTasklet)
                .build();
    }

    @Bean
    public Step postPurgeStep() {
        return new StepBuilder("postPurgeStep", jobRepository)
                .tasklet(postPurgeTasklet)
                .build();
    }
}
