package org.hikikomori.community.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.hikikomori.community.domain.Ban;
import org.hikikomori.community.exception.BannedUserException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class BanServiceTest {

    BanService service = new BanService();

    @Test
    @DisplayName("밴된 사용자면 예외를 던진다")
    void 밴이면_예외() {
        assertThatThrownBy(() -> service.checkNotBanned(true))
                .isInstanceOf(BannedUserException.class);
    }

    @Test
    @DisplayName("밴되지 않은 사용자면 통과한다")
    void 밴아니면_통과() {
        assertThatCode(() -> service.checkNotBanned(false)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("밴 엔티티를 생성한다")
    void 밴을_생성한다() {
        Ban ban = service.buildBan(7L, "신고 누적");
        assertThat(ban.getUserId()).isEqualTo(7L);
        assertThat(ban.getReason()).isEqualTo("신고 누적");
    }
}
