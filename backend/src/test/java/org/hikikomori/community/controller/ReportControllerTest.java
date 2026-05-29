package org.hikikomori.community.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;
import org.hikikomori.community.dto.ReportDto;
import org.hikikomori.community.facade.ReportFacade;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ReportController.class)
class ReportControllerTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean ReportFacade reportFacade;

    @Test
    @DisplayName("POST /api/reports/{userId} 신고 접수 시 201")
    void 신고접수_201() throws Exception {
        UUID targetId = UUID.randomUUID();
        given(reportFacade.report(eq(2L), any(ReportDto.CreateRequest.class), anyString()))
                .willReturn(ReportDto.Response.of(UUID.randomUUID()));

        String body = """
                {
                  "reporterId": 1,
                  "targetType": "POST",
                  "targetId": "%s",
                  "reason": "SPAM",
                  "description": "광고글"
                }
                """.formatted(targetId);

        mockMvc.perform(post("/api/reports/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("필수 필드 누락 시 400")
    void 검증실패_400() throws Exception {
        String body = """
                { "reporterId": 1, "targetType": "POST" }
                """;
        mockMvc.perform(post("/api/reports/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }
}
