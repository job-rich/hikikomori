package org.hikikomori.community.batch.tasklet;

import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hikikomori.community.repository.PostRepository;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.StepContribution;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PostCleanupTasklet implements Tasklet {

    private final PostRepository postRepository;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        LocalDate today = LocalDate.now();
        int deletedCount = postRepository.deleteByCreatedAtBefore(today.atStartOfDay());

        log.info("게시글 삭제 완료: {}건 (기준일: {} 미만)", deletedCount, today);

        return RepeatStatus.FINISHED;
    }
}
