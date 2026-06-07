package org.hikikomori.community.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import org.hikikomori.community.domain.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UserServiceTest {

    UserService service = new UserService();

    @Test
    @DisplayName("upsert: 기존 유저면 닉네임 갱신")
    void upsert_갱신() {
        User user = User.builder().userId(1L).nickName("old").build();

        User result = service.upsert(Optional.of(user), 1L, "new");

        assertThat(result).isSameAs(user);
        assertThat(result.getNickName()).isEqualTo("new");
    }

    @Test
    @DisplayName("upsert: 없는 유저면 생성")
    void upsert_생성() {
        User result = service.upsert(Optional.empty(), 1L, "n");

        assertThat(result.getUserId()).isEqualTo(1L);
        assertThat(result.getNickName()).isEqualTo("n");
    }

    @Test
    @DisplayName("markBanned: 유저를 밴 상태로 표시(없으면 생성)")
    void markBanned_생성() {
        User result = service.markBanned(Optional.empty(), 1L);

        assertThat(result.getUserId()).isEqualTo(1L);
        assertThat(result.isBanned()).isTrue();
    }

    @Test
    @DisplayName("markBanned: 기존 유저면 그대로 밴 표시")
    void markBanned_기존() {
        User user = User.builder().userId(1L).nickName("n").build();

        User result = service.markBanned(Optional.of(user), 1L);

        assertThat(result).isSameAs(user);
        assertThat(result.isBanned()).isTrue();
    }
}
