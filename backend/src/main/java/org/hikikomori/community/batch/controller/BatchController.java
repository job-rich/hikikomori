package org.hikikomori.community.batch.controller;

import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.hikikomori.community.batch.launcher.BatchJobLauncher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/batch")
@RequiredArgsConstructor
public class BatchController {

    private final BatchJobLauncher batchJobLauncher;

    @PostMapping("/purge")
    public ResponseEntity<String> runPurgeJob(
            @RequestParam LocalDate startAt,
            @RequestParam LocalDate endAt) {
        Long executionId = batchJobLauncher.runManual(startAt, endAt.plusDays(1));

        return ResponseEntity.ok("퍼지 배치 작업이 실행되었습니다. (executionId: " + executionId + ")");
    }
}
