package org.hikikomori.community.batch;

import lombok.RequiredArgsConstructor;
import org.hikikomori.community.service.BatchService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BatchScheduler {

    private final BatchService batchService;

    @Scheduled(cron = "0 0 0 * * *")
    public void runCleanupJob() {
        batchService.runCleanupJob();
    }
}
