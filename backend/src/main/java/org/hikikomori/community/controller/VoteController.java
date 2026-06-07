package org.hikikomori.community.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.hikikomori.community.dto.VoteDto;
import org.hikikomori.community.facade.VoteFacade;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/votes")
@RequiredArgsConstructor
public class VoteController {

    private final VoteFacade voteFacade;

    @PostMapping("/{userId}")
    public ResponseEntity<VoteDto.Response> vote(
            @PathVariable Long userId,
            @Valid @RequestBody VoteDto.CreateRequest request) {
        return ResponseEntity.ok(voteFacade.vote(userId, request));
    }
}
