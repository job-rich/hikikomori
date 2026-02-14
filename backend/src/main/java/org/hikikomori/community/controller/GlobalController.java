package org.hikikomori.community.controller;

import lombok.RequiredArgsConstructor;
import org.hikikomori.community.service.BatchService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/global")
@RequiredArgsConstructor
public class GlobalController {

    private final BatchService batchService;

    @PostMapping("/cleanup")
    public ResponseEntity<String> runCleanupJob() {
        Long executionId = batchService.runCleanupJob();

        return ResponseEntity.ok("정리 배치 작업이 실행되었습니다. (executionId: " + executionId + ")");
    }
}
