package org.hikikomori.community.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;
import org.hikikomori.community.domain.VoteValue;
import org.hikikomori.community.dto.VoteDto;
import org.hikikomori.community.facade.VoteFacade;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(VoteController.class)
class VoteControllerTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean VoteFacade voteFacade;

    @Test
    @DisplayName("POST /api/votes/{userId} 추천 시 200")
    void 추천_200() throws Exception {
        UUID t = UUID.randomUUID();
        given(voteFacade.vote(eq(2L), any(VoteDto.CreateRequest.class)))
                .willReturn(VoteDto.Response.of(VoteValue.UP, 1L));

        String body = """
                { "voterId":1, "targetType":"POST", "targetId":"%s", "value":"UP" }
                """.formatted(t);

        mockMvc.perform(post("/api/votes/2").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk());
    }
}
