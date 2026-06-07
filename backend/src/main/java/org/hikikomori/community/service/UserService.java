package org.hikikomori.community.service;

import java.util.Optional;
import org.hikikomori.community.domain.User;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    /** 기존 유저면 닉네임 갱신, 없으면 새 User 빌드. (저장은 Facade 담당) */
    public User upsert(Optional<User> existing, Long userId, String nickName) {
        return existing
                .map(u -> { u.updateNickName(nickName); return u; })
                .orElseGet(() -> User.builder().userId(userId).nickName(nickName).build());
    }

    /** 기존 유저면 그대로, 없으면 익명 User 빌드한 뒤 밴 표시. (저장은 Facade 담당) */
    public User markBanned(Optional<User> existing, Long userId) {
        User user = existing.orElseGet(() -> User.builder().userId(userId).nickName("익명").build());
        user.markBanned();
        return user;
    }
}
