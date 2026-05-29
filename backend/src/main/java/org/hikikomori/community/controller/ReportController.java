package org.hikikomori.community.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.hikikomori.community.dto.ReportDto;
import org.hikikomori.community.facade.ReportFacade;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportFacade reportFacade;

    @PostMapping("/{userId}")
    public ResponseEntity<ReportDto.Response> report(
            @PathVariable Long userId,
            @Valid @RequestBody ReportDto.CreateRequest request,
            HttpServletRequest httpRequest) {
        String ip = resolveClientIp(httpRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reportFacade.report(userId, request, ip));
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
