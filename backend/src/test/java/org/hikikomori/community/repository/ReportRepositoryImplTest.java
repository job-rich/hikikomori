package org.hikikomori.community.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.util.UUID;
import org.hikikomori.community.domain.Report;
import org.hikikomori.community.domain.ReportReason;
import org.hikikomori.community.domain.ReportTargetType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReportRepositoryImplTest {

    @Mock
    ReportJpaRepository jpaRepository;

    @InjectMocks
    ReportRepositoryImpl repository;

    @Test
    @DisplayName("신고를 저장하면 JpaRepository.save 에 위임한다")
    void 신고를_저장한다() {
        // given
        Report report = Report.builder()
                .reporterId(1L).reporterIp("1.1.1.1").targetUserId(2L)
                .targetType(ReportTargetType.POST).targetId(UUID.randomUUID())
                .reason(ReportReason.SPAM).build();
        given(jpaRepository.save(report)).willReturn(report);

        // when
        Report saved = repository.save(report);

        // then
        assertThat(saved).isSameAs(report);
        verify(jpaRepository).save(report);
    }

    @Test
    @DisplayName("신고자 ID와 IP가 모두 같은 중복 신고인지 확인한다")
    void 중복신고_여부를_확인한다() {
        // given
        UUID targetId = UUID.randomUUID();
        given(jpaRepository.existsByReporterIdAndReporterIpAndTargetTypeAndTargetId(
                1L, "1.1.1.1", ReportTargetType.POST, targetId)).willReturn(true);

        // when
        boolean exists = repository.existsReport(1L, "1.1.1.1", ReportTargetType.POST, targetId);

        // then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("작성자가 받은 신고 수를 센다")
    void 받은_신고수() {
        org.mockito.BDDMockito.given(jpaRepository.countByTargetUserId(2L)).willReturn(3L);
        assertThat(repository.countByTargetUser(2L)).isEqualTo(3L);
    }
}
