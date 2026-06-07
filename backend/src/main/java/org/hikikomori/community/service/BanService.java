package org.hikikomori.community.service;

import org.hikikomori.community.domain.Ban;
import org.hikikomori.community.exception.BannedUserException;
import org.springframework.stereotype.Service;

@Service
public class BanService {

    public void checkNotBanned(boolean banned) {
        if (banned) {
            throw new BannedUserException("신고 누적으로 작성이 제한된 사용자입니다");
        }
    }

    public Ban buildBan(Long userId, String reason) {
        return Ban.builder()
                .userId(userId)
                .reason(reason)
                .build();
    }
}
