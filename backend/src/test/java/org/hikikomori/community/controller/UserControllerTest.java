package org.hikikomori.community.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.hikikomori.community.dto.UserDto;
import org.hikikomori.community.facade.UserFacade;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean UserFacade userFacade;

    @Test
    @DisplayName("GET /api/users/{userId}/profile 200 + power")
    void 프로필_200() throws Exception {
        given(userFacade.getProfile(2L))
                .willReturn(new UserDto.ProfileResponse(2L, "니체", 30L, 5L, 1L, 12L, false));

        mockMvc.perform(get("/api/users/2/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.power").value(30));
    }
}
