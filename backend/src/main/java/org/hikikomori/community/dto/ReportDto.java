package org.hikikomori.community.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import org.hikikomori.community.domain.ReportReason;
import org.hikikomori.community.domain.ReportTargetType;

public class ReportDto {

    public record CreateRequest(
            @NotNull Long reporterId,
            @NotNull ReportTargetType targetType,
            @NotNull UUID targetId,
            @NotNull ReportReason reason,
            String description
    ) {}

    public record Response(UUID id) {
        public static Response of(UUID id) {
            return new Response(id);
        }
    }
}
