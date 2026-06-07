package org.hikikomori.community.controller;

import lombok.RequiredArgsConstructor;
import org.hikikomori.community.dto.UserDto;
import org.hikikomori.community.facade.UserFacade;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserFacade userFacade;

    @GetMapping("/{userId}/profile")
    public ResponseEntity<UserDto.ProfileResponse> profile(@PathVariable Long userId) {
        return ResponseEntity.ok(userFacade.getProfile(userId));
    }

    @GetMapping("/ranking")
    public ResponseEntity<Page<UserDto.RankingResponse>> ranking(@PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(userFacade.getRanking(pageable));
    }
}
