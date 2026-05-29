package org.hikikomori.community.event;

import java.util.UUID;
import org.hikikomori.community.domain.ReportTargetType;

public record ReportCreatedEvent(
        ReportTargetType targetType,
        UUID targetId,
        Long targetUserId
) {
}
