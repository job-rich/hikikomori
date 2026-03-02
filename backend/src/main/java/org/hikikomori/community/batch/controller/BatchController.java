package org.hikikomori.community.batch.controller;

import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.hikikomori.community.batch.controller.data.PurgeResponse;
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
    public ResponseEntity<PurgeResponse> runPurgeJob(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate
    ) {
        PurgeResponse response = batchJobLauncher.runManual(startDate, endDate.plusDays(1));

        return ResponseEntity.ok(response);
    }
}
