package org.hikikomori.community.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.hikikomori.community.exception.DuplicateReportException;
import org.hikikomori.community.exception.SelfReportException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ReportServiceTest {

    ReportService service = new ReportService();

    @Test
    @DisplayName("신고 대상 작성자가 경로 userId와 다르면 거부한다")
    void 대상작성자_불일치_거부() {
        assertThatThrownBy(() -> service.checkTargetAuthor(99L, 2L))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatCode(() -> service.checkTargetAuthor(2L, 2L)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("이미 신고한 콘텐츠면 중복으로 거부한다")
    void 중복신고_거부() {
        assertThatThrownBy(() -> service.checkNotDuplicate(true))
                .isInstanceOf(DuplicateReportException.class);
        assertThatCode(() -> service.checkNotDuplicate(false)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("신고자와 대상 작성자가 같으면 자기신고로 거부한다")
    void 자기신고를_거부한다() {
        assertThatThrownBy(() -> service.checkNotSelfReport(5L, 5L))
                .isInstanceOf(SelfReportException.class);
    }

    @Test
    @DisplayName("신고자와 대상 작성자가 다르면 통과한다")
    void 타인신고는_통과한다() {
        service.checkNotSelfReport(5L, 6L); // 예외 없음
    }

    @Test
    @DisplayName("신고자 수가 임계값 이상이면 숨김 대상이다")
    void 숨김_임계값_판정() {
        assertThat(service.shouldHide(5, 5)).isTrue();
        assertThat(service.shouldHide(6, 5)).isTrue();
        assertThat(service.shouldHide(4, 5)).isFalse();
    }

    @Test
    @DisplayName("숨김 콘텐츠 수가 임계값 이상이면 밴 대상이다")
    void 밴_임계값_판정() {
        assertThat(service.shouldBan(5, 5)).isTrue();
        assertThat(service.shouldBan(4, 5)).isFalse();
    }
}
