package org.hikikomori.community.batch.scheduler;

import lombok.RequiredArgsConstructor;
import org.hikikomori.community.batch.launcher.BatchJobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BatchScheduler {

    private final BatchJobLauncher batchJobLauncher;

    @Scheduled(cron = "0 0 0 * * *")
    public void runPurgeJob() {
        batchJobLauncher.runAuto();
    }
}
