package org.hikikomori.community.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.hikikomori.community.dto.ReportDto;
import org.hikikomori.community.facade.ReportFacade;
import org.hikikomori.community.util.IpAddressUtil;
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
        // getRemoteAddr만 신뢰한다. 클라가 위조 가능한 X-Forwarded-For는 직접 파싱하지 않으며,
        // 신뢰 프록시 뒤에서는 server.forward-headers-strategy 설정이 진짜 클라 IP로 해석한다.
        // IPv6(IPv4-mapped·루프백)는 IPv4로 정규화해 중복 집계를 막는다.
        String ip = IpAddressUtil.normalize(httpRequest.getRemoteAddr());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reportFacade.report(userId, request, ip));
    }
}
