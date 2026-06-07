package org.hikikomori.community.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hikikomori.community.facade.UserFacade;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserBannedListener {

    private final UserFacade userFacade;

    @Async("reportJudgmentExecutor")
    @TransactionalEventListener
    public void onUserBanned(UserBannedEvent event) {
        try {
            userFacade.markBanned(event.userId());
        } catch (Exception e) {
            log.warn("User.banned 동기화 실패: userId={}", event.userId(), e);
        }
    }
}
