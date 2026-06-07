package org.hikikomori.community.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hikikomori.community.facade.ReportJudgmentFacade;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReportJudgmentListener {

    private final ReportJudgmentFacade judgmentFacade;

    @Async("reportJudgmentExecutor")
    @TransactionalEventListener
    public void onReportCreated(ReportCreatedEvent event) {
        try {
            judgmentFacade.judge(event);
        } catch (Exception e) {
            log.warn("신고 판정 실패 (다음 신고 시 재계산됨): target={}/{}",
                    event.targetType(), event.targetId(), e);
        }
    }
}
